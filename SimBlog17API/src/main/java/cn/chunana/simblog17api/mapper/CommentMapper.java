package cn.chunana.simblog17api.mapper;

import cn.chunana.simblog17api.dto.request.CommentRequest;
import cn.chunana.simblog17api.dto.response.CommentResponse;
import cn.chunana.simblog17api.entities.Comment;

public final class CommentMapper {

    private CommentMapper() {
    }

    public static Comment toNewComment(CommentRequest request, Long authorId) {
        return Comment.builder()
                      .articleId(request.articleId())
                      .authorId(authorId)
                      .parentCommentId(request.parentCommentId())
                      .content(request.content())
                      .build();
    }

    public static CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
                              .id(comment.getId())
                              .articleId(comment.getArticleId())
                              .authorId(comment.getAuthorId())
                              .parentCommentId(comment.getParentCommentId())
                              .content(comment.getContent())
                              .createTime(comment.getCreateTime())
                              .status(comment.getStatus())
                              .build();
    }
}

