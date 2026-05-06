package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.dto.response.ArticleInteractionResponse;
import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Article;
import cn.chunana.simblog17api.entities.ArticleFavorite;
import cn.chunana.simblog17api.entities.ArticleLike;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.mapper.ArticleMapper;
import cn.chunana.simblog17api.repository.ArticleFavoriteRepository;
import cn.chunana.simblog17api.repository.ArticleLikeRepository;
import cn.chunana.simblog17api.repository.ArticleRepository;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.ArticleInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleInteractionServiceImpl implements ArticleInteractionService {

    private final ArticleLikeRepository     articleLikeRepository;
    private final ArticleFavoriteRepository articleFavoriteRepository;
    private final ArticleRepository         articleRepository;
    private final UserRepository            userRepository;

    @Override
    public boolean toggleLike(Long articleId, Long userId) {
        return articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                                    .map(like -> {
                                        articleLikeRepository.delete(like);
                                        return false; // 已取消点赞
                                    })
                                    .orElseGet(() -> {
                                        articleLikeRepository.save(
                                                ArticleLike.builder()
                                                           .articleId(articleId)
                                                           .userId(userId)
                                                           .build());
                                        return true; // 已点赞
                                    });
    }

    @Override
    public boolean toggleFavorite(Long articleId, Long userId) {
        return articleFavoriteRepository.findByArticleIdAndUserId(articleId, userId)
                                        .map(fav -> {
                                            articleFavoriteRepository.delete(fav);
                                            return false; // 已取消收藏
                                        })
                                        .orElseGet(() -> {
                                            articleFavoriteRepository.save(
                                                    ArticleFavorite.builder()
                                                                   .articleId(articleId)
                                                                   .userId(userId)
                                                                   .build());
                                            return true; // 已收藏
                                        });
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleInteractionResponse getInteractionStatus(Long articleId, Long userId) {
        long likeCount     = articleLikeRepository.countByArticleId(articleId);
        long favoriteCount = articleFavoriteRepository.countByArticleId(articleId);

        boolean liked   = userId != null && articleLikeRepository.existsByArticleIdAndUserId(articleId, userId);
        boolean favored = userId != null && articleFavoriteRepository.existsByArticleIdAndUserId(articleId, userId);

        return ArticleInteractionResponse.builder()
                                         .likeCount(likeCount)
                                         .likedByCurrentUser(liked)
                                         .favoriteCount(favoriteCount)
                                         .favoritedByCurrentUser(favored)
                                         .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleMetaResponse> getMyLikedArticles(Long userId, Pageable pageable) {
        Page<ArticleLike> likePage = articleLikeRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
        return toArticleMetaPage(likePage.map(ArticleLike::getArticleId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleMetaResponse> getMyFavoritedArticles(Long userId, Pageable pageable) {
        Page<ArticleFavorite> favoritePage = articleFavoriteRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
        return toArticleMetaPage(favoritePage.map(ArticleFavorite::getArticleId));
    }

    private PageResponse<ArticleMetaResponse> toArticleMetaPage(Page<Long> articleIdPage) {
        List<Long> articleIds = articleIdPage.getContent();
        if (articleIds.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(),
                                      articleIdPage.getTotalElements(),
                                      articleIdPage.getTotalPages(),
                                      articleIdPage.getNumber(),
                                      articleIdPage.getSize());
        }

        List<Article>      articles   = articleRepository.findAllById(articleIds);
        Map<Long, Article> articleMap = new LinkedHashMap<>();
        for (Article article : articles) {
            if (article.getId() != null) {
                articleMap.put(article.getId().longValue(), article);
            }
        }

        List<Article> orderedArticles = articleIds.stream()
                .map(articleMap::get)
                .filter(a -> a != null && a.getStatus() == Article.STATUS_PUBLISHED)
                .toList();

        List<Long> authorIds = orderedArticles.stream()
                .map(Article::getAuthorId)
                .distinct()
                .toList();
        Map<Long, User> userMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<ArticleMetaResponse> metas = orderedArticles.stream()
                .map(a -> {
                    User author = userMap.get(a.getAuthorId());
                    return ArticleMapper.toArticleMetaResponse(a,
                            author != null ? author.getUsername() : null,
                            author != null ? author.getAvatarUrl() : null);
                })
                .toList();

        return new PageResponse<>(metas,
                                  articleIdPage.getTotalElements(),
                                  articleIdPage.getTotalPages(),
                                  articleIdPage.getNumber(),
                                  articleIdPage.getSize());
    }
}
