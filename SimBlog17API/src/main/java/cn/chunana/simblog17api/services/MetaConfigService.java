package cn.chunana.simblog17api.services;

import cn.chunana.simblog17api.dto.response.UploadPermissionsResponse;
import cn.chunana.simblog17api.entities.User;

/**
 * 站长口令（owner token）校验服务 + 站点运营配置。
 */
public interface MetaConfigService {

    // ── Owner token ──────────────────────────────────────────────────────────

    boolean verifyOwnerToken(String plainToken);

    boolean isOwnerTokenConfigured();

    void saveOwnerToken(String plainToken);

    // ── Upload permissions ────────────────────────────────────────────────────

    /** 获取当前投稿权限配置（含黑名单列表）。 */
    UploadPermissionsResponse getUploadPermissions();

    /** 设置普通用户/管理员的上传开关。 */
    void setUploadPermissions(boolean userAllowed, boolean adminAllowed);

    /** 将用户加入上传黑名单。 */
    void addToUploadBlacklist(Long userId);

    /** 将用户从上传黑名单中移除。 */
    void removeFromUploadBlacklist(Long userId);

    /**
     * 综合判断某用户是否有权上传文章：先检查所属用户组的开关，再检查黑名单。
     *
     * @param userId 用户 ID
     * @param role   用户角色
     * @return true 表示允许上传
     */
    boolean canUserUpload(Long userId, User.UserRole role);

    // ── User role management ───────────────────────────────────────────────────

    /**
     * 通过站长口令将指定用户设置为管理员或普通用户。
     *
     * @param userId  目标用户 ID
     * @param role    目标角色
     * @throws java.util.NoSuchElementException if user not found
     */
    void setUserRole(Long userId, User.UserRole role);
}
