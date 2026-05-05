package cn.chunana.simblog17api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uc_user_id_username", columnNames = {"id", "username"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    public enum UserRole {
        USER,
        ADMIN
    }

    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String avatarUrl;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    private LocalDateTime createTime;
}

