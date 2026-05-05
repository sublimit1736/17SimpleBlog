package cn.chunana.simblog17api.dto.response;

import cn.chunana.simblog17api.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Schema(description = "用户认证响应")
public record UserAccessResponse(
        @Schema(description = "用户 ID") Long id,
        @Schema(description = "用户名") String username,
        @Schema(description = "头像图片 URL") String avatarUrl,
        @Schema(description = "用户角色") User.UserRole role,
        @Schema(description = "注册时间") LocalDateTime createTime,
        @Schema(description = "访问令牌（access token）") String accessToken,
        @Schema(description = "刷新令牌（refresh token）") String refreshToken
) implements Serializable {
}