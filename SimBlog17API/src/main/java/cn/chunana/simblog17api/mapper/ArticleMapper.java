package cn.chunana.simblog17api.mapper;

import cn.chunana.simblog17api.dto.request.ArticleRequest;
import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.ArticleResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Article;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.utils.HtmlSanitizer;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ArticleMapper {

    private ArticleMapper() {
    }

    public static Article toNewArticle(ArticleRequest articleRequest) {
        String normalizedContent = normalizeContentByType(articleRequest.content(), articleRequest.contentType());
        return Article.builder()
                      .title(articleRequest.title())
                      .content(normalizedContent)
                      .contentType(articleRequest.contentType())
                      .preview(toPreview(normalizedContent, articleRequest.contentType()))
                      .authorId(articleRequest.authorId())
                      .tags(toTagsString(articleRequest.tags()))
                      .status(Article.STATUS_PUBLISHED)
                      .viewCount(0)
                      .build();
    }

    public static void applyUpdatableFields(Article article, ArticleRequest articleRequest) {
        String normalizedContent = normalizeContentByType(articleRequest.content(), articleRequest.contentType());
        article.setTitle(articleRequest.title());
        article.setContent(normalizedContent);
        article.setContentType(articleRequest.contentType());
        article.setPreview(toPreview(normalizedContent, articleRequest.contentType()));
        article.setTags(toTagsString(articleRequest.tags()));
    }

    public static ArticleResponse toArticleResponse(Article article) {
        return toArticleResponse(article, null, null);
    }

    public static ArticleResponse toArticleResponse(Article article, String authorName, String authorAvatarUrl) {
        return ArticleResponse.builder()
                              .id(article.getId())
                              .title(article.getTitle())
                              .content(article.getContent())
                              .contentType(article.getContentType())
                              .preview(article.getPreview())
                              .authorId(article.getAuthorId())
                              .authorName(authorName)
                              .authorAvatarUrl(authorAvatarUrl)
                              .publishedTime(article.getPublishedTime())
                              .updatedTime(article.getUpdatedTime())
                              .viewCount(article.getViewCount())
                              .tags(article.getTags())
                              .status(article.getStatus())
                              .build();
    }

    public static ArticleMetaResponse toArticleMetaResponse(Article article) {
        return toArticleMetaResponse(article, null, null);
    }

    public static ArticleMetaResponse toArticleMetaResponse(Article article, String authorName, String authorAvatarUrl) {
        return ArticleMetaResponse.builder()
                                  .id(article.getId())
                                  .title(article.getTitle())
                                  .contentType(article.getContentType())
                                  .authorId(article.getAuthorId())
                                  .authorName(authorName)
                                  .authorAvatarUrl(authorAvatarUrl)
                                  .publishedTime(article.getPublishedTime())
                                  .updatedTime(article.getUpdatedTime())
                                  .viewCount(article.getViewCount())
                                  .tags(article.getTags())
                                  .status(article.getStatus())
                                  .build();
    }

    /**
     * Converts a Page of Articles into a PageResponse of ArticleMetaResponses,
     * enriched with author info fetched in a single batch query.
     */
    public static PageResponse<ArticleMetaResponse> toMetaPageResponse(
            Page<Article> page, UserRepository userRepository) {
        List<Article> articles = page.getContent();
        Map<Long, User> userMap = fetchAuthorMap(articles, userRepository);
        Page<ArticleMetaResponse> mapped = page.map(a -> {
            User author = userMap.get(a.getAuthorId());
            return toArticleMetaResponse(a,
                    author != null ? author.getUsername() : null,
                    author != null ? author.getAvatarUrl() : null);
        });
        return PageResponse.from(mapped);
    }

    /**
     * Converts a Page of Articles into a PageResponse of ArticleResponses,
     * enriched with author info fetched in a single batch query.
     */
    public static PageResponse<ArticleResponse> toFullPageResponse(
            Page<Article> page, UserRepository userRepository) {
        List<Article> articles = page.getContent();
        Map<Long, User> userMap = fetchAuthorMap(articles, userRepository);
        Page<ArticleResponse> mapped = page.map(a -> {
            User author = userMap.get(a.getAuthorId());
            return toArticleResponse(a,
                    author != null ? author.getUsername() : null,
                    author != null ? author.getAvatarUrl() : null);
        });
        return PageResponse.from(mapped);
    }

    /**
     * Converts a list of Articles into a PageResponse of ArticleMetaResponses,
     * enriched with author info, preserving page metadata from the source page.
     */
    public static PageResponse<ArticleMetaResponse> toMetaPageResponseFromList(
            List<Article> articles, long totalElements, int totalPages, int pageNumber, int pageSize,
            UserRepository userRepository) {
        Map<Long, User> userMap = fetchAuthorMap(articles, userRepository);
        List<ArticleMetaResponse> metas = articles.stream()
                .map(a -> {
                    User author = userMap.get(a.getAuthorId());
                    return toArticleMetaResponse(a,
                            author != null ? author.getUsername() : null,
                            author != null ? author.getAvatarUrl() : null);
                })
                .toList();
        return new PageResponse<>(metas, totalElements, totalPages, pageNumber, pageSize);
    }

    private static Map<Long, User> fetchAuthorMap(List<Article> articles, UserRepository userRepository) {
        List<Long> authorIds = articles.stream()
                .map(Article::getAuthorId)
                .distinct()
                .toList();
        return userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private static String toPreview(String content, String contentType) {
        if (content == null) {
            return "";
        }

        if (contentType == null) {
            contentType = Article.CONTENT_TYPE_PLAIN_TEXT;
        }

        String normalized = Article.CONTENT_TYPE_HTML.equals(contentType)
                ? content.replaceAll("<[^>]+>", " ")
                : content;

        String compact       = normalized.trim().replaceAll("\\s+", " ");
        int    previewLength = Math.min(compact.length(), 120);
        return compact.substring(0, previewLength);
    }

    private static String normalizeContentByType(String content, String contentType) {
        if (Article.CONTENT_TYPE_HTML.equals(contentType)) {
            return HtmlSanitizer.sanitize(content);
        }
        return content;
    }

    private static String toTagsString(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.stream()
                   .filter(t -> t != null && !t.isBlank())
                   .map(t -> t.replace(",", ""))
                   .collect(java.util.stream.Collectors.joining(","));
    }
}
