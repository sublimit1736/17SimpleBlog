package cn.chunana.simblog17api.repository;

import cn.chunana.simblog17api.entities.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {

    Optional<MediaAsset> findByStoredFileName(String storedFileName);

    List<MediaAsset> findByCreateTimeBefore(LocalDateTime threshold);

    List<MediaAsset> findByArticleId(Long articleId);

    Optional<MediaAsset> findByArticleIdAndStoredFileName(Long articleId, String storedFileName);

    Optional<MediaAsset> findByArticleIdAndOriginalFileName(Long articleId, String originalFileName);
}


