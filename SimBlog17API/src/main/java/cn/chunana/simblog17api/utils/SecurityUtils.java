package cn.chunana.simblog17api.utils;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** 返回 JWT subject 中存储的 userId，匿名时返回 null。 */
    public static Long getCurrentUserId(Authentication auth) {
        if (!isAuthenticated(auth)) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    /** 判断当前请求是否携带有效认证（非匿名）。 */
    public static boolean isAuthenticated(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

    /** 判断当前用户是否具有 ADMIN 角色。 */
    public static boolean isAdmin(Authentication auth) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        return auth.getAuthorities().stream()
                   .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}

