package cn.chunana.simblog17api.mapper;

import cn.chunana.simblog17api.dto.request.CommentRequest;
import cn.chunana.simblog17api.dto.response.CommentResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Comment;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.repository.UserRepository;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        return toCommentResponse(comment, null, null);
    }

    public static CommentResponse toCommentResponse(Comment comment, String authorName, String authorAvatarUrl) {
        return CommentResponse.builder()
                              .id(comment.getId())
                              .articleId(comment.getArticleId())
                              .authorId(comment.getAuthorId())
                              .authorName(authorName)
                              .authorAvatarUrl(authorAvatarUrl)
                              .parentCommentId(comment.getParentCommentId())
                              .content(comment.getContent())
                              .createTime(comment.getCreateTime())
                              .status(comment.getStatus())
                              .build();
    }

    /**
     * Converts a Page of Comments into a PageResponse of CommentResponses,
     * enriched with author info fetched in a single batch query.
     */
    public static PageResponse<CommentResponse> toCommentPageResponse(
            Page<Comment> page, UserRepository userRepository) {
        List<Comment> comments = page.getContent();
        List<Long> authorIds = comments.stream()
                .map(Comment::getAuthorId)
                .distinct()
                .toList();
        Map<Long, User> userMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Page<CommentResponse> mapped = page.map(c -> {
            User author = userMap.get(c.getAuthorId());
            return toCommentResponse(c,
                    author != null ? author.getUsername() : null,
                    author != null ? author.getAvatarUrl() : null);
        });
        return PageResponse.from(mapped);
    }
}

