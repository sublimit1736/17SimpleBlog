package cn.chunana.simblog17api.mapper;

import cn.chunana.simblog17api.dto.response.UserAccessResponse;
import cn.chunana.simblog17api.entities.User;

import java.time.LocalDateTime;

public final class UserAccessMapper {

    private UserAccessMapper() {
    }

    public static User toNewUser(String username, String encodedPassword, LocalDateTime createTime) {
        return User.builder()
                   .username(username)
                   .password(encodedPassword)
                   .role(User.UserRole.USER)
                   .createTime(createTime)
                   .build();
    }

    /**
     * 登录/刷新时使用，携带 access + refresh token
     */
    public static UserAccessResponse toUserAccessResponse(User user, String accessToken, String refreshToken) {
        return UserAccessResponse.builder()
                                 .id(user.getId())
                                 .username(user.getUsername())
                                 .avatarUrl(user.getAvatarUrl())
                                 .role(user.getRole())
                                 .createTime(user.getCreateTime())
                                 .accessToken(accessToken)
                                 .refreshToken(refreshToken)
                                 .build();
    }

    public static UserAccessResponse toUserAccessResponse(User user, String accessToken) {
        return toUserAccessResponse(user, accessToken, null);
    }

    /**
     * 注册或不需要 token 时使用
     */
    public static UserAccessResponse toUserAccessResponse(User user) {
        return toUserAccessResponse(user, null, null);
    }
}
