package cn.chunana.simblog17api.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "文章实体")
@SQLRestriction("status <> 4")
public class Article {
    public static final String CONTENT_TYPE_PLAIN_TEXT = "PLAIN_TEXT";
    public static final String CONTENT_TYPE_MARKDOWN   = "MARKDOWN";
    public static final String CONTENT_TYPE_HTML       = "HTML";

    public static final int STATUS_DRAFT     = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_ARCHIVED  = 2;
    public static final int STATUS_HIDDEN    = 3;
    public static final int STATUS_DELETED   = 4;
    public static final int STATUS_PENDING   = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键 ID", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "文章标题", example = "我的第一篇文章")
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    @Schema(description = "文章内容")
    private String content;

    @Column(nullable = false, length = 20)
    @Schema(description = "内容类型：PLAIN_TEXT/MARKDOWN/HTML", example = "MARKDOWN")
    private String contentType;

    @Schema(description = "文章预览")
    private String preview;

    @Column(nullable = false)
    @Schema(description = "作者用户 ID", example = "1001")
    private Long authorId;

    @Column(nullable = false)
    @Schema(description = "发布时间（系统自动写入）", example = "2026-04-10T18:30:00")
    private LocalDateTime publishedTime;

    @Column(nullable = false)
    @Schema(description = "最后更新时间（系统自动维护）", example = "2026-04-10T18:35:00")
    private LocalDateTime updatedTime;

    @Schema(description = "浏览次数", example = "256")
    private Integer viewCount;

    @Schema(description = "文章标签，多个标签使用英文逗号分隔", example = "java,spring,backend")
    private String tags; // 逗号分隔的标签

    @Column(nullable = false)
    @Schema(description = "文章状态：0-草稿，1-已发布，2-已归档，3-已隐藏，4-已删除，5-待审核", example = "1")
    private Integer status; // 0 -- 草稿，1 -- 已发布，2 -- 已归档, 3 -- 已隐藏，4 -- 已标记删除，5 -- 待审核

    @PrePersist
    public void prePersist() {
        publishedTime = LocalDateTime.now();
        updatedTime   = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
