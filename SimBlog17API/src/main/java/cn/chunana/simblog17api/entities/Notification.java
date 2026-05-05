package cn.chunana.simblog17api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ   = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String type;

    @Column(nullable = false, length = 32)
    private String targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private Integer status;

    @Column(nullable = false)
    private LocalDateTime createTime;

    private LocalDateTime readTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (status == null) {
            status = STATUS_UNREAD;
        }
    }
}

