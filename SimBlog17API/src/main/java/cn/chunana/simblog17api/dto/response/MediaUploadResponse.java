package cn.chunana.simblog17api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

@Builder
@Schema(description = "图片上传响应")
public record MediaUploadResponse(
        @Schema(description = "媒体记录 ID") Long id,
        @Schema(description = "图片可访问 URL") String url,
        @Schema(description = "原始文件名") String originalFileName,
        @Schema(description = "存储文件名") String storedFileName,
        @Schema(description = "MIME 类型") String contentType,
        @Schema(description = "文件大小（字节）") Long sizeBytes
) implements Serializable {
}

