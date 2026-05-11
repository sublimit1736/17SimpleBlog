package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.dto.response.MediaUploadResponse;
import cn.chunana.simblog17api.entities.MediaAsset;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.repository.MediaAssetRepository;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaServiceImpl implements MediaService {

    private final MediaAssetRepository mediaAssetRepository;
    private final UserRepository       userRepository;

    @Value("${app.media.storage-path:uploads}")
    private String storagePath;

    @Value("${app.media.max-size-bytes:5242880}")
    private long maxSizeBytes;

    @Override
    public MediaUploadResponse uploadImage(MultipartFile file, Long uploaderId) {
        validateImage(file);

        Path   root           = ensureStorageDirectory();
        String storedFileName = buildStoredFileName(file.getOriginalFilename());
        Path   target         = root.resolve(storedFileName);

        copyFile(file, target);

        String fileUrl = "/api/media/files/" + storedFileName;

        MediaAsset mediaAsset = mediaAssetRepository.save(
                MediaAsset.builder()
                          .uploaderId(uploaderId)
                          .originalFileName(safeOriginalName(file.getOriginalFilename()))
                          .storedFileName(storedFileName)
                          .contentType(file.getContentType())
                          .sizeBytes(file.getSize())
                          .fileUrl(fileUrl)
                          .build());

        return toMediaUploadResponse(mediaAsset);
    }

    @Override
    public MediaUploadResponse uploadAvatar(MultipartFile file, Long targetUserId, Long currentUserId, boolean isAdmin) {
        if (currentUserId == null || (!isAdmin && !currentUserId.equals(targetUserId))) {
            throw new IllegalArgumentException("No permission to update avatar");
        }

        User user = userRepository.findById(targetUserId)
                                  .orElseThrow(() -> new IllegalArgumentException("User not found"));

        MediaUploadResponse uploaded = uploadImage(file, currentUserId);
        user.setAvatarUrl(uploaded.url());
        userRepository.save(user);

        return uploaded;
    }

    @Override
    public MediaUploadResponse uploadArticleImage(MultipartFile file, Long articleId, Long uploaderId) {
        validateImage(file);

        Path   articleDir     = ensureArticleDirectory(articleId);
        String storedFileName = buildStoredFileName(file.getOriginalFilename());
        Path   target         = articleDir.resolve(storedFileName);

        copyFile(file, target);

        String fileUrl = "/api/articles/" + articleId + "/image/" + storedFileName;

        MediaAsset mediaAsset = mediaAssetRepository.save(
                MediaAsset.builder()
                          .uploaderId(uploaderId)
                          .articleId(articleId)
                          .originalFileName(safeOriginalName(file.getOriginalFilename()))
                          .storedFileName(storedFileName)
                          .contentType(file.getContentType())
                          .sizeBytes(file.getSize())
                          .fileUrl(fileUrl)
                          .build());

        return toMediaUploadResponse(mediaAsset);
    }

    private Path ensureArticleDirectory(Long articleId) {
        try {
            // articleId is a Long; converting to string produces only digits, preventing path traversal.
            String safeId   = String.valueOf(articleId.longValue());
            Path   root     = Path.of(storagePath).toAbsolutePath().normalize();
            Path   articleDir = root.resolve("articles").resolve(safeId).normalize();
            if (!articleDir.startsWith(root)) {
                throw new IllegalStateException("Computed article path escapes storage root");
            }
            Files.createDirectories(articleDir);
            return articleDir;
        }
        catch (IOException exception) {
            throw new IllegalStateException("Failed to create article media directory", exception);
        }
    }
        return MediaUploadResponse.builder()
                                  .id(mediaAsset.getId())
                                  .url(mediaAsset.getFileUrl())
                                  .originalFileName(mediaAsset.getOriginalFileName())
                                  .storedFileName(mediaAsset.getStoredFileName())
                                  .contentType(mediaAsset.getContentType())
                                  .sizeBytes(mediaAsset.getSizeBytes())
                                  .build();
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("Image file exceeds max allowed size");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    private Path ensureStorageDirectory() {
        try {
            Path root = Path.of(storagePath).toAbsolutePath().normalize();
            Files.createDirectories(root);
            return root;
        }
        catch (IOException exception) {
            throw new IllegalStateException("Failed to initialize media storage", exception);
        }
    }

    private String buildStoredFileName(String originalName) {
        String extension = "";
        String safeName  = safeOriginalName(originalName);
        int    dot       = safeName.lastIndexOf('.');
        if (dot >= 0 && dot < safeName.length() - 1) {
            extension = safeName.substring(dot);
        }
        return UUID.randomUUID() + extension;
    }

    private String safeOriginalName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "image";
        }
        return Path.of(originalName).getFileName().toString();
    }

    private void copyFile(MultipartFile file, Path target) {
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException exception) {
            throw new IllegalStateException("Failed to store uploaded image", exception);
        }
    }
}


