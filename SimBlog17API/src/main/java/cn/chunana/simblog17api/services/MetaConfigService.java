package cn.chunana.simblog17api.services;

/**
 * 站长口令（owner token）校验服务。
 */
public interface MetaConfigService {

    /**
     * 校验明文口令与数据库中存储的 BCrypt 哈希是否匹配。
     *
     * @param plainToken 前端传来的明文口令
     * @return true 表示匹配，false 表示不匹配或尚未配置
     */
    boolean verifyOwnerToken(String plainToken);

    /**
     * 是否已设置过站长口令（数据库中存在哈希值）。
     */
    boolean isOwnerTokenConfigured();

    /**
     * 保存（或更新）站长口令哈希。
     * 仅在已通过旧口令验证的情况下调用。
     *
     * @param plainToken 明文新口令
     */
    void saveOwnerToken(String plainToken);
}
