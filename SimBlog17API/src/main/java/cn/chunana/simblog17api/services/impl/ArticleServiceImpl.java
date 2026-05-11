package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.common.CacheNames;
import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.ArticleResponse;
import cn.chunana.simblog17api.dto.response.MediaUploadResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Article;
import cn.chunana.simblog17api.mapper.ArticleMapper;
import cn.chunana.simblog17api.repository.ArticleRepository;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.ArticleService;
import cn.chunana.simblog17api.services.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository    userRepository;
    private final MediaService      mediaService;

    // Upload ─────────────────────────────────────────────────────────────────

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.HOME_LATEST, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_STATS, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT_TAGS, allEntries = true)
    })
    public Article uploadArticle(String title, String tags, String contentType,
                                 MultipartFile contentFile, List<MultipartFile> imageFiles,
                                 Long authorId) {
        // 1. Read content file
        String rawContent = readTextFile(contentFile);

        // 2. Create article (we need the generated ID for image namespace)
        String normalizedTags = normalizeTags(tags);
        Article article = Article.builder()
                                 .title(title.strip())
                                 .content(rawContent)
                                 .contentType(contentType)
                                 .preview(ArticleMapper.buildPreview(rawContent))
                                 .authorId(authorId)
                                 .tags(normalizedTags)
                                 .status(Article.STATUS_PUBLISHED)
                                 .viewCount(0)
                                 .build();
        article = articleRepository.save(article);
        Long articleId = article.getId();

        // 3. Upload images into the article's namespace
        List<MediaUploadResponse> uploaded = new ArrayList<>();
        if (imageFiles != null) {
            for (MultipartFile img : imageFiles) {
                if (img != null && !img.isEmpty()) {
                    try {
                        uploaded.add(mediaService.uploadArticleImage(img, articleId, authorId));
                    } catch (Exception ex) {
                        log.warn("article.image.upload.fail articleId={} file={} err={}",
                                 articleId, img.getOriginalFilename(), ex.getMessage());
                    }
                }
            }
        }

        // 4. Rewrite image references in content
        String processedContent = rewriteImageRefs(rawContent, uploaded, articleId);
        if (!processedContent.equals(rawContent)) {
            article.setContent(processedContent);
            article.setPreview(ArticleMapper.buildPreview(processedContent));
            article = articleRepository.save(article);
        }

        return article;
    }

    // Read ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<Article> getArticleById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleMetaResponse> getAllArticles(Pageable pageable) {
        return ArticleMapper.toMetaPageResponse(
                articleRepository.findByStatusOrderByPublishedTimeDesc(Article.STATUS_PUBLISHED, pageable),
                userRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getArticlesByAuthorId(Long authorId, Pageable pageable) {
        return ArticleMapper.toFullPageResponse(
                articleRepository.findByAuthorIdAndStatus(authorId, Article.STATUS_PUBLISHED, pageable),
                userRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleMetaResponse> searchArticlesByTitle(String key, Pageable pageable) {
        return ArticleMapper.toMetaPageResponse(
                articleRepository.findByTitleContainingAndStatus(key, Article.STATUS_PUBLISHED, pageable),
                userRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleMetaResponse> searchArticlesByTag(String tag, Pageable pageable) {
        return ArticleMapper.toMetaPageResponse(
                articleRepository.findByTagsContainingAndStatus(tag, Article.STATUS_PUBLISHED, pageable),
                userRepository);
    }

    // Delete ─────────────────────────────────────────────────────────────────

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ARTICLE, key = "#id"),
            @CacheEvict(value = CacheNames.HOME_LATEST, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_STATS, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT_TAGS, allEntries = true)
    })
    public Optional<Article> deleteArticle(Long id) {
        return articleRepository.findById(id)
                                .map(article -> {
                                    article.setStatus(Article.STATUS_DELETED);
                                    return articleRepository.save(article);
                                });
    }

    // View count ─────────────────────────────────────────────────────────────

    @Async
    @Override
    public void increaseViewCountsAsync(Long id) {
        articleRepository.increaseViewCount(id);
    }

    // Helpers ────────────────────────────────────────────────────────────────

    private String readTextFile(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to read content file: " + ex.getMessage(), ex);
        }
    }

    private static String normalizeTags(String tags) {
        if (tags == null || tags.isBlank()) return null;
        return java.util.Arrays.stream(tags.split(","))
                               .map(String::strip)
                               .filter(t -> !t.isEmpty())
                               .collect(java.util.stream.Collectors.joining(","));
    }

    /**
     * For Markdown content: replaces bare filename references
     * (e.g. {@code ![alt](photo.jpg)} or {@code ![alt](./photo.jpg)})
     * with the scoped article-image API URL.
     * Only filenames that were actually uploaded are replaced; all other image
     * references (external URLs, filenames not in the upload set) are left as-is.
     */
    private static String rewriteImageRefs(String content,
                                           List<MediaUploadResponse> uploaded,
                                           Long articleId) {
        if (content == null || uploaded.isEmpty()) return content;

        String result = content;
        for (MediaUploadResponse asset : uploaded) {
            String original = asset.originalFileName();
            String stored   = asset.storedFileName();
            if (original == null || stored == null) continue;
            String scopedUrl = "/api/articles/" + articleId + "/image/" + stored;
            result = result
                    .replace("](" + original + ")", "](" + scopedUrl + ")")
                    .replace("](./\\" + original + ")", "](" + scopedUrl + ")")
                    .replace("](./" + original + ")", "](" + scopedUrl + ")");
        }
        return result;
    }
}
