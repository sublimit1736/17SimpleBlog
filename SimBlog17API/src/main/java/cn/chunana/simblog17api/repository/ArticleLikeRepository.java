package cn.chunana.simblog17api.repository;

import cn.chunana.simblog17api.entities.ArticleLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {

    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);

    long countByArticleId(Long articleId);

    Page<ArticleLike> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    boolean existsByArticleIdAndUserId(Long articleId, Long userId);
}

