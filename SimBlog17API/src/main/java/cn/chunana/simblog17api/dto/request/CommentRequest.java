package cn.chunana.simblog17api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

@Schema(description = "创建评论请求")
public record CommentRequest(

        @Schema(description = "文章 ID", example = "1")
        @NotNull(message = "articleId must not be null")
        @Positive(message = "articleId must be positive")
        Long articleId,

        @Schema(description = "评论内容", example = "写得很好！")
        @NotBlank(message = "content must not be blank")
        @Size(max = 2000, message = "content length must be at most 2000")
        String content,

        @Schema(description = "父评论 ID，null 表示顶层评论，非 null 表示回复某条评论", example = "null")
        Long parentCommentId

) implements Serializable {
}

