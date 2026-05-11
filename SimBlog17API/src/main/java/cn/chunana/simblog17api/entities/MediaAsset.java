package cn.chunana.simblog17api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long uploaderId;

    /** Non-null when this asset belongs to a specific article's namespace. */
    @Column
    private Long articleId;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, unique = true, length = 255)
    private String storedFileName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(nullable = false, length = 512)
    private String fileUrl;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}

