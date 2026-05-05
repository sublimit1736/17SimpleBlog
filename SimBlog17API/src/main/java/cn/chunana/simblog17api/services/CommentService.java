package cn.chunana.simblog17api.services;

import cn.chunana.simblog17api.dto.request.CommentRequest;
import cn.chunana.simblog17api.dto.response.CommentResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Comment;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CommentService {

    Comment createComment(CommentRequest request, Long authorId);

    /** 删除评论：拥有者或管理员可删除，否则返回空。 */
    Optional<Comment> deleteComment(Long commentId, Long currentUserId, boolean isAdmin);

    Optional<Comment> moderateCommentStatus(Long commentId, Integer status);

    PageResponse<CommentResponse> getCommentsByArticleId(Long articleId, Pageable pageable);
}

