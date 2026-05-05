package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.dto.response.ApiStatusResponse;
import cn.chunana.simblog17api.dto.response.MediaUploadResponse;
import cn.chunana.simblog17api.services.MediaService;
import cn.chunana.simblog17api.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
@Tag(name = "媒体", description = "图片上传与访问接口")
public class MediaCtrl {

    private final MediaService mediaService;

    @Value("${app.media.storage-path:uploads}")
    private String storagePath;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传图片", description = "上传后返回图片 URL，可用于纯文本/Markdown/HTML 内容中引用")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<MediaUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        Long currentUserId = SecurityUtils.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        return ApiStatusResponse.ok(mediaService.uploadImage(file, currentUserId));
    }

    @GetMapping("/files/{fileName:.+}")
    @Operation(summary = "访问图片文件", description = "根据文件名返回图片内容")
    public ResponseEntity<?> getFile(@PathVariable String fileName) {
        try {
            Path root   = Path.of(storagePath).toAbsolutePath().normalize();
            Path target = root.resolve(fileName).normalize();
            if (!target.startsWith(root) || !Files.exists(target)) {
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
        catch (IOException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

