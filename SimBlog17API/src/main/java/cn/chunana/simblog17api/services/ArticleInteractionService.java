package cn.chunana.simblog17api.services;

import cn.chunana.simblog17api.dto.response.ArticleInteractionResponse;
import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ArticleInteractionService {

    /**
     * 切换点赞状态。
     *
     * @return true = 已点赞，false = 已取消点赞
     */
    boolean toggleLike(Long articleId, Long userId);

    /**
     * 切换收藏状态。
     *
     * @return true = 已收藏，false = 已取消收藏
     */
    boolean toggleFavorite(Long articleId, Long userId);

    /**
     * 获取文章的互动状态。userId 为 null 时 likedByCurrentUser / favoritedByCurrentUser 均为 false。
     */
    ArticleInteractionResponse getInteractionStatus(Long articleId, Long userId);

    PageResponse<ArticleMetaResponse> getMyLikedArticles(Long userId, Pageable pageable);

    PageResponse<ArticleMetaResponse> getMyFavoritedArticles(Long userId, Pageable pageable);
}

