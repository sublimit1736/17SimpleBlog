package cn.chunana.simblog17api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Schema(description = "评论响应")
public record CommentResponse(
        @Schema(description = "评论 ID") Long id,
        @Schema(description = "所属文章 ID") Long articleId,
        @Schema(description = "作者用户 ID") Long authorId,
        @Schema(description = "父评论 ID，null 为顶层评论") Long parentCommentId,
        @Schema(description = "评论内容") String content,
        @Schema(description = "创建时间") LocalDateTime createTime,
        @Schema(description = "状态：0-待审核，1-已通过，2-已驳回，3-已删除") Integer status
) implements Serializable {
}

