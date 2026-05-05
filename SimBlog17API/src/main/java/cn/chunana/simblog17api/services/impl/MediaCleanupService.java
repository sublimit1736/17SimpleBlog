package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.entities.Article;
import cn.chunana.simblog17api.entities.MediaAsset;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.repository.ArticleRepository;
import cn.chunana.simblog17api.repository.MediaAssetRepository;
import cn.chunana.simblog17api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaCleanupService {

    private final MediaAssetRepository mediaAssetRepository;
    private final UserRepository       userRepository;
    private final ArticleRepository    articleRepository;

    @Value("${app.media.storage-path:uploads}")
    private String storagePath;

    @Value("${app.media.cleanup-enabled:true}")
    private boolean cleanupEnabled;

    @Value("${app.media.cleanup-older-than-days:7}")
    private int cleanupOlderThanDays;

    @Scheduled(cron = "${app.media.cleanup-cron:0 30 3 * * *}")
    public void scheduledCleanup() {
        if (!cleanupEnabled) {
            return;
        }
        long deletedCount = cleanupOrphanedMedia(cleanupOlderThanDays);
        log.info("media.cleanup.completed deletedCount={} olderThanDays={}", deletedCount, cleanupOlderThanDays);
    }

    @Transactional
    public long cleanupOrphanedMedia(int olderThanDays) {
        if (olderThanDays < 0) {
            throw new IllegalArgumentException("olderThanDays must be >= 0");
        }

        LocalDateTime    threshold  = LocalDateTime.now().minusDays(olderThanDays);
        List<MediaAsset> candidates = mediaAssetRepository.findByCreateTimeBefore(threshold);
        if (candidates.isEmpty()) {
            return 0;
        }

        Set<String> referencedUrls = collectReferencedUrls();
        Path        root           = Path.of(storagePath).toAbsolutePath().normalize();

        long deleted = 0;
        for (MediaAsset media : candidates) {
            if (referencedUrls.contains(media.getFileUrl())) {
                continue;
            }

            deleteFileQuietly(root.resolve(media.getStoredFileName()).normalize(), root);
            mediaAssetRepository.delete(media);
            deleted++;
        }
        return deleted;
    }

    private Set<String> collectReferencedUrls() {
        List<MediaAsset> allMedia = mediaAssetRepository.findAll();

        Set<String> referenced = userRepository.findAll().stream()
                                               .map(User::getAvatarUrl)
                                               .filter(Objects::nonNull)
                                               .filter(url -> !url.isBlank())
                                               .collect(Collectors.toSet());

        for (Article article : articleRepository.findAll()) {
            String content = article.getContent();
            if (content == null || content.isBlank()) {
                continue;
            }

            for (MediaAsset media : allMedia) {
                if (content.contains(media.getFileUrl())) {
                    referenced.add(media.getFileUrl());
                }
            }
        }

        return referenced;
    }

    private void deleteFileQuietly(Path target, Path root) {
        try {
            if (!target.startsWith(root)) {
                return;
            }
            Files.deleteIfExists(target);
        }
        catch (IOException exception) {
            log.warn("media.cleanup.file_delete_failed path={} reason={}", target, exception.getMessage());
        }
    }
}


