package cn.chunana.simblog17api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "站长口令校验请求")
public record OwnerTokenRequest(
        @Schema(description = "站长口令明文", example = "my-secret-token")
        @NotBlank(message = "token must not be blank")
        @Size(max = 256, message = "token must be at most 256 characters")
        String token
) {
}
