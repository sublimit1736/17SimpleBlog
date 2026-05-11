package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.dto.request.AdminCommentStatusRequest;
import cn.chunana.simblog17api.dto.response.ApiStatusResponse;
import cn.chunana.simblog17api.dto.response.CommentResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.dto.response.UserAccessResponse;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.mapper.CommentMapper;
import cn.chunana.simblog17api.mapper.UserAccessMapper;
import cn.chunana.simblog17api.repository.CommentRepository;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.CommentService;
import cn.chunana.simblog17api.services.impl.MediaCleanupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "管理员", description = "用户组管理（仅 ADMIN 角色可访问）")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminCtrl {

    private final UserRepository    userRepository;
    private final CommentService    commentService;
    private final CommentRepository commentRepository;
    private final MediaCleanupService mediaCleanupService;

    @PutMapping("/users/{userId}/promote")
    @Operation(summary = "提升为管理员")
    public ApiStatusResponse<UserAccessResponse> promoteToAdmin(@PathVariable Long userId) {
        return userRepository.findById(userId)
                             .map(user -> {
                                 user.setRole(User.UserRole.ADMIN);
                                 userRepository.save(user);
                                 log.info("admin.user.promote uid={}", userId);
                                 return ApiStatusResponse.ok(UserAccessMapper.toUserAccessResponse(user));
                             })
                             .orElseGet(() -> ApiStatusResponse.fail(Status.USER_NOT_FOUND));
    }

    @PutMapping("/users/{userId}/demote")
    @Operation(summary = "降级为普通用户")
    public ApiStatusResponse<UserAccessResponse> demoteToUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                             .map(user -> {
                                 user.setRole(User.UserRole.USER);
                                 userRepository.save(user);
                                 log.info("admin.user.demote uid={}", userId);
                                 return ApiStatusResponse.ok(UserAccessMapper.toUserAccessResponse(user));
                             })
                             .orElseGet(() -> ApiStatusResponse.fail(Status.USER_NOT_FOUND));
    }

    @GetMapping("/comments")
    @Operation(summary = "评论管理列表")
    public ApiStatusResponse<PageResponse<CommentResponse>> getComments(
            @RequestParam(defaultValue = "0") int status,
            @PageableDefault(size = 20, sort = "createTime") Pageable pageable) {

        return ApiStatusResponse.ok(
                CommentMapper.toCommentPageResponse(
                        commentRepository.findByStatusOrderByCreateTimeDesc(status, pageable),
                        userRepository));
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "管理员删除评论")
    public ApiStatusResponse<CommentResponse> deleteCommentAsAdmin(@PathVariable Long id) {
        return commentService.deleteComment(id, null, true)
                             .map(c -> {
                                 cn.chunana.simblog17api.entities.User author = c.getAuthorId() != null
                                         ? userRepository.findById(c.getAuthorId()).orElse(null)
                                         : null;
                                 log.info("admin.comment.delete id={}", id);
                                 return ApiStatusResponse.ok(CommentMapper.toCommentResponse(c,
                                         author != null ? author.getUsername() : null,
                                         author != null ? author.getAvatarUrl() : null));
                             })
                             .orElseGet(() -> ApiStatusResponse.fail(Status.COMMENT_NOT_FOUND));
    }

    @PutMapping("/comments/{id}/status")
    @Operation(summary = "管理员更新评论审核状态")
    public ApiStatusResponse<CommentResponse> moderateCommentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminCommentStatusRequest request) {

        return commentService.moderateCommentStatus(id, request.status())
                             .map(c -> {
                                 cn.chunana.simblog17api.entities.User author = c.getAuthorId() != null
                                         ? userRepository.findById(c.getAuthorId()).orElse(null)
                                         : null;
                                 log.info("admin.comment.status_update id={} status={}", id, request.status());
                                 return ApiStatusResponse.ok(CommentMapper.toCommentResponse(c,
                                         author != null ? author.getUsername() : null,
                                         author != null ? author.getAvatarUrl() : null));
                             })
                             .orElseGet(() -> ApiStatusResponse.fail(Status.COMMENT_NOT_FOUND));
    }

    @PostMapping("/media/cleanup")
    @Operation(summary = "清理孤儿图片文件")
    public ApiStatusResponse<Long> cleanupOrphanMedia(
            @RequestParam(defaultValue = "7") int olderThanDays) {

        long deletedCount = mediaCleanupService.cleanupOrphanedMedia(olderThanDays);
        log.info("admin.media.cleanup olderThanDays={} deletedCount={}", olderThanDays, deletedCount);
        return ApiStatusResponse.ok(deletedCount);
    }
}
