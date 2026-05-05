package cn.chunana.simblog17api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.io.Serializable;

@Builder
@Schema(description = "User login/register request")
public record UserAccessRequest(
        @Schema(description = "用户名", example = "demo_user")
        @NotBlank(message = "username must not be blank")
        @Size(min = 3, max = 32, message = "username length must be between 3 and 32")
        String username,

        @Schema(description = "密码", example = "secret123")
        @NotBlank(message = "password must not be blank")
        @Size(max = 128, message = "password length must be at most 128")
        String password
) implements Serializable {
}
