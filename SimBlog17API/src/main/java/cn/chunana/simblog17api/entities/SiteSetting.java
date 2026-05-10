package cn.chunana.simblog17api.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * 通用站点配置键值表。
 * 当前使用的 key 包含：<br>
 * - {@code owner.token.hash} — BCrypt 哈希后的站长口令
 */
@Entity
@Table(name = "site_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteSetting {

    @Id
    @Column(name = "setting_key", nullable = false, unique = true, length = 128)
    private String key;

    @Column(name = "setting_value", nullable = false, length = 1024)
    private String value;
}
