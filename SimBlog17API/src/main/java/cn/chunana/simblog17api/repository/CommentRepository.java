package cn.chunana.simblog17api.repository;

import cn.chunana.simblog17api.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByArticleIdAndStatusOrderByCreateTimeAsc(Long articleId, Integer status, Pageable pageable);

    boolean existsByIdAndAuthorId(Long id, Long authorId);

    long countByStatus(int status);

    /** 全站最新评论（按创建时间倒序） */
    Page<Comment> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);
}
