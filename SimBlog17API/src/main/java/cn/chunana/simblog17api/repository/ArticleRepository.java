package cn.chunana.simblog17api.repository;

import cn.chunana.simblog17api.entities.Article;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findById(@NonNull Long id);

    Page<Article> findByStatusOrderByPublishedTimeDesc(int status, Pageable pageable);

    Page<Article> findByTitleContainingAndStatus(String title, Integer status, Pageable pageable);

    Page<Article> findByAuthorIdAndStatus(Long authorId, Integer status, Pageable pageable);

    Page<Article> findByTagsContainingAndStatus(String tag, Integer status, Pageable pageable);

    @Query(value = "SELECT * FROM articles a WHERE a.status = :status AND a.title ~* :pattern",
           countQuery = "SELECT COUNT(*) FROM articles a WHERE a.status = :status AND a.title ~* :pattern",
           nativeQuery = true)
    Page<Article> findByTitleRegexAndStatus(@Param("pattern") String pattern,
                                            @Param("status") Integer status,
                                            Pageable pageable);

    @Query(value = "SELECT * FROM articles a WHERE a.status = :status AND a.tags ~* :pattern",
           countQuery = "SELECT COUNT(*) FROM articles a WHERE a.status = :status AND a.tags ~* :pattern",
           nativeQuery = true)
    Page<Article> findByTagsRegexAndStatus(@Param("pattern") String pattern,
                                           @Param("status") Integer status,
                                           Pageable pageable);

    /**
     * 时间窗口内按浏览量降序排列的热门文章
     */
    @Query("SELECT a FROM Article a WHERE a.status = 1 AND a.publishedTime >= :since ORDER BY a.viewCount DESC")
    Page<Article> findHotArticles(@Param("since") LocalDateTime since, Pageable pageable);

    long countByStatus(int status);

    /**
     * 所有已发布文章的总浏览量
     */
    @Query("SELECT COALESCE(SUM(a.viewCount), 0) FROM Article a WHERE a.status = 1")
    long sumViewCount();

    /**
     * 获取所有已发布文章的 tags 字段（用于统计热门标签）
     */
    @Query("SELECT a.tags FROM Article a WHERE a.status = 1 AND a.tags IS NOT NULL AND a.tags <> ''")
    List<String> findAllPublishedTags();

    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void increaseViewCount(@Param("id") Long id);
}
