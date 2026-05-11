package cn.chunana.simblog17api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "投稿权限配置响应")
public record UploadPermissionsResponse(
        @Schema(description = "普通用户是否允许上传") boolean userAllowed,
        @Schema(description = "管理员是否允许上传") boolean adminAllowed,
        @Schema(description = "禁止上传的用户 ID 列表") List<Long> blacklist
) {
}
