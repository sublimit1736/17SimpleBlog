package cn.chunana.simblog17api.repository;

import cn.chunana.simblog17api.entities.ArticleFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleFavoriteRepository extends JpaRepository<ArticleFavorite, Long> {

    Optional<ArticleFavorite> findByArticleIdAndUserId(Long articleId, Long userId);

    long countByArticleId(Long articleId);

    Page<ArticleFavorite> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    boolean existsByArticleIdAndUserId(Long articleId, Long userId);
}

