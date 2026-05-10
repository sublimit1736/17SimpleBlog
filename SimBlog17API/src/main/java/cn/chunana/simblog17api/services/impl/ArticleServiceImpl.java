package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.common.CacheNames;
import cn.chunana.simblog17api.dto.request.ArticleRequest;
import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.ArticleResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Article;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.mapper.ArticleMapper;
import cn.chunana.simblog17api.repository.ArticleRepository;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.ArticleService;
import cn.chunana.simblog17api.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository   articleRepository;
    private final UserRepository      userRepository;
    private final NotificationService notificationService;

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.HOME_LATEST, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_STATS, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT_TAGS, allEntries = true)
    })
    public Article createArticle(ArticleRequest articleRequest) {
        Article newArticle = ArticleMapper.toNewArticle(articleRequest);
        newArticle.setStatus(resolvePublishStatusByAuthorId(articleRequest.authorId()));
        return articleRepository.save(newArticle);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.HOME_STATS, allEntries = true)
    })
    public Article createDraft(ArticleRequest articleRequest) {
        Article draftArticle = ArticleMapper.toNewArticle(articleRequest);
        draftArticle.setStatus(Article.STATUS_DRAFT);
        return articleRepository.save(draftArticle);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.HOME_LATEST, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_STATS, allEntries = true)
    })
    public Optional<Article> hideArticle(Long id) {
        return articleRepository.findById(id)
                                .map(article -> {
                                    article.setStatus(Article.STATUS_HIDDEN);
                                    return articleRepository.save(article);
                                });
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.HOME_LATEST, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_STATS, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT_TAGS, allEntries = true)
    })
    public Optional<Article> publishArticle(Long id) {
        return articleRepository.findById(id)
                                .map(article -> {
                                    article.setStatus(resolvePublishStatusByAuthorId(article.getAuthorId()));
                                    return articleRepository.save(article);
                                });
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.HOME_LATEST, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_STATS, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT_TAGS, allEntries = true)
    })
    public Optional<Article> publishDraft(Long id) {
        return articleRepository.findById(id)
                                .map(article -> {
                                    if (article.getStatus() != Article.STATUS_DRAFT) {
                                        return article;
                                    }
                                    article.setStatus(resolvePublishStatusByAuthorId(article.getAuthorId()));
                                    return articleRepository.save(article);
                                });
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ARTICLE, key = "#id"),
            @CacheEvict(value = CacheNames.HOME_LATEST, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT_TAGS, allEntries = true)
    })
    public Optional<Article> updateArticle(Long id, ArticleRequest articleRequest) {
        return articleRepository.findById(id)
                                .map(updatedArticle -> {
                                    ArticleMapper.applyUpdatableFields(updatedArticle, articleRequest);
                                    return articleRepository.save(updatedArticle);
                                });
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ARTICLE, key = "#id"),
            @CacheEvict(value = CacheNames.HOME_STATS, allEntries = true)
    })
    public Optional<Article> updateDraft(Long id, ArticleRequest articleRequest) {
        return articleRepository.findById(id)
                                .filter(article -> article.getStatus() == Article.STATUS_DRAFT)
                                .map(draft -> {
                                    ArticleMapper.applyUpdatableFields(draft, articleRequest);
                                    return articleRepository.save(draft);
                                });
    }

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
    public PageResponse<ArticleResponse> getDraftArticlesByAuthorId(Long authorId, Pageable pageable) {
        return ArticleMapper.toFullPageResponse(
                articleRepository.findByAuthorIdAndStatus(authorId, Article.STATUS_DRAFT, pageable),
                userRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleMetaResponse> getPendingArticles(Pageable pageable) {
        return ArticleMapper.toMetaPageResponse(
                articleRepository.findByStatusOrderByPublishedTimeDesc(Article.STATUS_PENDING, pageable),
                userRepository);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ARTICLE, key = "#id"),
            @CacheEvict(value = CacheNames.HOME_LATEST, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_STATS, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_HOT_TAGS, allEntries = true)
    })
    public Optional<Article> moderateArticleStatus(Long id, Integer status) {
        if (status == null || status < Article.STATUS_DRAFT || status > Article.STATUS_PENDING) {
            return Optional.empty();
        }
        return articleRepository.findById(id)
                                .map(article -> {
                                    article.setStatus(status);
                                    Article saved = articleRepository.save(article);
                                    notificationService.createModerationNotification(
                                            saved.getAuthorId(),
                                            "ARTICLE",
                                    saved.getId(),
                                            "文章审核结果",
                                            buildArticleModerationMessage(status)
                                                                                    );
                                    return saved;
                                });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> searchArticlesByTitle(String key, Pageable pageable) {
        return ArticleMapper.toFullPageResponse(
                articleRepository.findByTitleContainingAndStatus(key, Article.STATUS_PUBLISHED, pageable),
                userRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> searchArticlesByTag(String tag, Pageable pageable) {
        return ArticleMapper.toFullPageResponse(
                articleRepository.findByTagsContainingAndStatus(tag, Article.STATUS_PUBLISHED, pageable),
                userRepository);
    }

    @Async
    @Override
    public void increaseViewCountsAsync(Long id) {
        articleRepository.increaseViewCount(id);
    }

    private int resolvePublishStatusByAuthorId(Long authorId) {
        Optional<User> userOpt = userRepository.findById(authorId);
        if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.ADMIN) {
            return Article.STATUS_PUBLISHED;
        }
        return Article.STATUS_PENDING;
    }

    private String buildArticleModerationMessage(Integer status) {
        return switch (status) {
            case Article.STATUS_PUBLISHED -> "你的文章已审核通过并发布";
            case Article.STATUS_HIDDEN -> "你的文章因审核原因被隐藏";
            case Article.STATUS_ARCHIVED -> "你的文章已归档";
            case Article.STATUS_DELETED -> "你的文章未通过审核（已删除）";
            case Article.STATUS_PENDING -> "你的文章进入待审核状态";
            default -> "你的文章审核状态已更新";
        };
    }
}
