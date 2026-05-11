package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.entities.MediaAsset;
import cn.chunana.simblog17api.repository.MediaAssetRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Serves article-scoped images.
 * Each image is validated to belong to the requested article before being served,
 * enforcing the per-article reference namespace: images from one article cannot
 * be accessed via another article's URL.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
@Tag(name = "文章图片", description = "文章专属图片访问接口（跨文章引用无效）")
public class ArticleImageCtrl {

    private final MediaAssetRepository mediaAssetRepository;

    @Value("${app.media.storage-path:uploads}")
    private String storagePath;

    /**
     * Serve an image that belongs to the given article's namespace.
     * Returns 404 if the image does not exist or does not belong to this article.
     */
    @GetMapping("/{articleId}/image/{fileName:.+}")
    @Operation(
            summary = "访问文章图片",
            description = "仅当图片归属于指定文章时才返回图片内容，跨文章引用返回 404"
    )
    public ResponseEntity<?> getArticleImage(
            @PathVariable Long articleId,
            @PathVariable String fileName) {

        // Validate that the requested file belongs to this article
        Optional<MediaAsset> assetOpt = mediaAssetRepository.findByArticleIdAndStoredFileName(articleId, fileName);
        if (assetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path root       = Path.of(storagePath).toAbsolutePath().normalize();
            Path articleDir = root.resolve("articles").resolve(String.valueOf(articleId));
            Path target     = articleDir.resolve(fileName).normalize();

            // Double-check path traversal safety
            if (!target.startsWith(articleDir) || !Files.exists(target)) {
                return ResponseEntity.notFound().build();
            }

            UrlResource resource    = new UrlResource(target.toUri());
            String      contentType = Files.probeContentType(target);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                                 .contentType(MediaType.parseMediaType(contentType))
                                 .body(resource);
        }
        catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
