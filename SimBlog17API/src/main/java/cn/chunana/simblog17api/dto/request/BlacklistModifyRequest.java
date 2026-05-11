package cn.chunana.simblog17api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "黑名单修改请求（添加/移除）")
public record BlacklistModifyRequest(
        @Schema(description = "站长口令") @NotBlank @Size(max = 256) String token,
        @Schema(description = "用户 ID") @NotNull @Positive Long userId
) {
}
