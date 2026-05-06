package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.dto.request.CommentRequest;
import cn.chunana.simblog17api.dto.response.ApiStatusResponse;
import cn.chunana.simblog17api.dto.response.CommentResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Comment;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.mapper.CommentMapper;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.CommentService;
import cn.chunana.simblog17api.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
@Tag(name = "评论", description = "文章评论与回复接口")
@Slf4j
public class CommentCtrl {

    private final CommentService  commentService;
    private final UserRepository  userRepository;

    @PostMapping
    @Operation(summary = "发表评论", description = "parentCommentId 为 null 时是文章顶层评论，非 null 时是对某条评论的回复")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<CommentResponse> createComment(
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {

        Long authorId = SecurityUtils.getCurrentUserId(authentication);
        if (authorId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        Comment comment = commentService.createComment(request, authorId);
        log.info("comment.create id={} authorId={} articleId={}", comment.getId(), authorId, request.articleId());
        User author = userRepository.findById(authorId).orElse(null);
        return ApiStatusResponse.ok(CommentMapper.toCommentResponse(comment,
                author != null ? author.getUsername() : null,
                author != null ? author.getAvatarUrl() : null));
    }

    @GetMapping("/by_article/{articleId}")
    @Operation(summary = "获取文章评论列表（分页）")
    public ApiStatusResponse<PageResponse<CommentResponse>> getCommentsByArticle(
            @PathVariable Long articleId,
            @PageableDefault(size = 20, sort = "createTime") Pageable pageable) {

        return ApiStatusResponse.ok(
                commentService.getCommentsByArticleId(articleId, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论", description = "评论作者或管理员可删除")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<CommentResponse> deleteComment(
            @PathVariable Long id,
            Authentication authentication) {

        Long    currentUserId = SecurityUtils.getCurrentUserId(authentication);
        boolean isAdmin       = SecurityUtils.isAdmin(authentication);

        // 未认证且非放行环境（dev 无 token）时也拒绝删除他人评论
        if (!SecurityUtils.isAuthenticated(authentication)) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        return commentService.deleteComment(id, currentUserId, isAdmin)
                             .map(CommentMapper::toCommentResponse)
                             .map(commentResponse -> {
                                 log.info("comment.delete id={} operatorUid={} admin={}", id, currentUserId, isAdmin);
                                 return ApiStatusResponse.ok(commentResponse);
                             })
                             .orElseGet(() -> ApiStatusResponse.fail(Status.ACCESS_DENIED));
    }
}




