package cn.chunana.simblog17api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

@Schema(description = "Update username request")
public record UpdateUsernameRequest(
        @Schema(description = "新用户名", example = "new_name")
        @NotBlank(message = "username must not be blank")
        @Size(min = 3, max = 32, message = "username length must be between 3 and 32")
        String username
) implements Serializable {
}

