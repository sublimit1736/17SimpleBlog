package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.dto.response.ApiStatusResponse;
import cn.chunana.simblog17api.dto.response.ArticleInteractionResponse;
import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.services.ArticleInteractionService;
import cn.chunana.simblog17api.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
@Tag(name = "文章互动", description = "点赞与收藏接口")
@Slf4j
public class ArticleInteractionCtrl {

    private final ArticleInteractionService interactionService;

    @PostMapping("/{id}/like")
    @Operation(summary = "切换点赞", description = "已点赞则取消；未点赞则添加。需要登录。")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<Boolean> toggleLike(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = SecurityUtils.getCurrentUserId(authentication);
        if (userId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }
        boolean liked = interactionService.toggleLike(id, userId);
        log.info("article.like.toggle articleId={} userId={} liked={}", id, userId, liked);
        return ApiStatusResponse.ok(liked);
    }

    @PostMapping("/{id}/favorite")
    @Operation(summary = "切换收藏", description = "已收藏则取消；未收藏则添加。需要登录。")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<Boolean> toggleFavorite(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = SecurityUtils.getCurrentUserId(authentication);
        if (userId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }
        boolean favorited = interactionService.toggleFavorite(id, userId);
        log.info("article.favorite.toggle articleId={} userId={} favorited={}", id, userId, favorited);
        return ApiStatusResponse.ok(favorited);
    }

    @GetMapping("/{id}/interactions")
    @Operation(summary = "获取互动状态", description = "返回点赞数、收藏数及当前用户的状态（未登录时 likedByCurrentUser / favoritedByCurrentUser 均为 false）")
    public ApiStatusResponse<ArticleInteractionResponse> getInteractions(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = SecurityUtils.getCurrentUserId(authentication);
        return ApiStatusResponse.ok(interactionService.getInteractionStatus(id, userId));
    }

    @GetMapping("/profile/{uid}/likes")
    @Operation(summary = "用户点赞文章", description = "分页返回指定用户点赞过的文章（按点赞时间倒序）")
    public ApiStatusResponse<PageResponse<ArticleMetaResponse>> getMyLikedArticles(
            @PathVariable Long uid,
            @PageableDefault(size = 10) Pageable pageable) {

        return ApiStatusResponse.ok(interactionService.getMyLikedArticles(uid, pageable));
    }

    @GetMapping("/profile/{uid}/favorites")
    @Operation(summary = "用户收藏文章", description = "分页返回指定用户收藏过的文章（按收藏时间倒序）")
    public ApiStatusResponse<PageResponse<ArticleMetaResponse>> getMyFavoritedArticles(
            @PathVariable Long uid,
            @PageableDefault(size = 10) Pageable pageable) {

        return ApiStatusResponse.ok(interactionService.getMyFavoritedArticles(uid, pageable));
    }
}

