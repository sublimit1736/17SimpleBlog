package cn.chunana.simblog17api.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评论实体")
public class Comment {

    public static final int STATUS_PENDING  = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;
    public static final int STATUS_DELETED  = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "所属文章 ID")
    private Long articleId;

    @Column(nullable = false)
    @Schema(description = "评论作者用户 ID")
    private Long authorId;

    /**
     * null 表示顶层评论；非 null 表示对某条评论的回复
     */
    @Schema(description = "父评论 ID，null 为顶层评论")
    private Long parentCommentId;

    @Column(nullable = false, length = 2000)
    @Schema(description = "评论内容")
    private String content;

    @Column(nullable = false)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Column(nullable = false)
    @Schema(description = "状态：0-待审核，1-已通过，2-已驳回，3-已删除")
    private Integer status;

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
        if (status == null) {
            status = STATUS_PENDING;
        }
    }
}

