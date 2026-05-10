package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.common.CacheNames;
import cn.chunana.simblog17api.dto.request.CommentRequest;
import cn.chunana.simblog17api.dto.response.CommentResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Comment;
import cn.chunana.simblog17api.mapper.CommentMapper;
import cn.chunana.simblog17api.repository.CommentRepository;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.CommentService;
import cn.chunana.simblog17api.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository   commentRepository;
    private final NotificationService notificationService;
    private final UserRepository      userRepository;

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.HOME_STATS, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_RECENT_COMMENTS, allEntries = true)
    })
    public Comment createComment(CommentRequest request, Long authorId) {
        Comment comment = CommentMapper.toNewComment(request, authorId);
        comment.setStatus(Comment.STATUS_PENDING);
        return commentRepository.save(comment);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.HOME_STATS, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_RECENT_COMMENTS, allEntries = true)
    })
    public Optional<Comment> deleteComment(Long commentId, Long currentUserId, boolean isAdmin) {
        return commentRepository.findById(commentId)
                                .filter(comment -> isAdmin || comment.getAuthorId().equals(currentUserId))
                                .map(comment -> {
                                    comment.setStatus(Comment.STATUS_DELETED);
                                    Comment saved = commentRepository.save(comment);
                                    if (isAdmin) {
                                        notificationService.createModerationNotification(
                                                saved.getAuthorId(),
                                                "COMMENT",
                                                saved.getId(),
                                                "评论审核结果",
                                                "你的评论未通过审核（已删除）"
                                                                                        );
                                    }
                                    return saved;
                                });
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.HOME_STATS, allEntries = true),
            @CacheEvict(value = CacheNames.HOME_RECENT_COMMENTS, allEntries = true)
    })
    public Optional<Comment> moderateCommentStatus(Long commentId, Integer status) {
        if (status == null || status < Comment.STATUS_PENDING || status > Comment.STATUS_DELETED) {
            return Optional.empty();
        }

        return commentRepository.findById(commentId)
                                .map(comment -> {
                                    comment.setStatus(status);
                                    Comment saved = commentRepository.save(comment);

                                    if (status == Comment.STATUS_APPROVED) {
                                        notificationService.createModerationNotification(
                                                saved.getAuthorId(),
                                                "COMMENT",
                                                saved.getId(),
                                                "评论审核结果",
                                                "你的评论已审核通过"
                                                                                        );
                                    }
                                    else if (status == Comment.STATUS_REJECTED) {
                                        notificationService.createModerationNotification(
                                                saved.getAuthorId(),
                                                "COMMENT",
                                                saved.getId(),
                                                "评论审核结果",
                                                "你的评论未通过审核"
                                                                                        );
                                    }
                                    return saved;
                                });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getCommentsByArticleId(Long articleId, Pageable pageable) {
        return CommentMapper.toCommentPageResponse(
                commentRepository.findByArticleIdAndStatusOrderByCreateTimeAsc(
                        articleId, Comment.STATUS_APPROVED, pageable),
                userRepository);
    }
}




