package cn.chunana.simblog17api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_favorites", uniqueConstraints = {
        @UniqueConstraint(name = "uc_article_fav_article_user", columnNames = {"articleId", "userId"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long articleId;

    @Column(nullable = false)
    private Long userId;

    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
    }
}

