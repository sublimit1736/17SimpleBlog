package cn.chunana.simblog17api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

@Schema(description = "Change password request")
public record ChangePasswordRequest(
        @Schema(description = "旧密码", example = "oldSecret123")
        @NotBlank(message = "oldPassword must not be blank")
        @Size(max = 128, message = "oldPassword length must be at most 128")
        String oldPassword,

        @Schema(description = "新密码", example = "newSecret456")
        @NotBlank(message = "newPassword must not be blank")
        @Size(max = 128, message = "newPassword length must be at most 128")
        String newPassword
) implements Serializable {
}

