package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.dto.response.ApiStatusResponse;
import cn.chunana.simblog17api.dto.response.NotificationResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.services.NotificationService;
import cn.chunana.simblog17api.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "通知", description = "用户审核状态通知接口")
public class NotificationCtrl {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "我的通知列表")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<PageResponse<NotificationResponse>> getMyNotifications(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createTime") Pageable pageable) {

        Long userId = SecurityUtils.getCurrentUserId(authentication);
        if (userId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        return ApiStatusResponse.ok(notificationService.getMyNotifications(userId, pageable));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读通知数量")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<Long> getUnreadCount(Authentication authentication) {
        Long userId = SecurityUtils.getCurrentUserId(authentication);
        if (userId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }
        return ApiStatusResponse.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记单条通知为已读")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<NotificationResponse> markRead(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = SecurityUtils.getCurrentUserId(authentication);
        if (userId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        return notificationService.markAsRead(userId, id)
                                  .map(ApiStatusResponse::ok)
                                  .orElseGet(() -> ApiStatusResponse.fail(Status.RESOURCE_NOT_FOUND));
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记为已读")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<Long> markAllRead(Authentication authentication) {
        Long userId = SecurityUtils.getCurrentUserId(authentication);
        if (userId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }
        return ApiStatusResponse.ok(notificationService.markAllAsRead(userId));
    }
}
