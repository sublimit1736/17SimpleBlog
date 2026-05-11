package cn.chunana.simblog17api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "更新投稿权限请求")
public record SetUploadPermissionsRequest(
        @Schema(description = "站长口令") @NotBlank @Size(max = 256) String token,
        @Schema(description = "是否允许普通用户上传") boolean userAllowed,
        @Schema(description = "是否允许管理员上传") boolean adminAllowed
) {
}
