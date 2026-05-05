package cn.chunana.simblog17api.services;

import cn.chunana.simblog17api.dto.response.MediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {

    MediaUploadResponse uploadImage(MultipartFile file, Long uploaderId);

    MediaUploadResponse uploadAvatar(MultipartFile file, Long targetUserId, Long currentUserId, boolean isAdmin);
}

