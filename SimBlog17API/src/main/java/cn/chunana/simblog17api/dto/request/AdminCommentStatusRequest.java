package cn.chunana.simblog17api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@Schema(description = "Admin comment status update request")
public record AdminCommentStatusRequest(
        @Schema(description = "目标评论状态：0-待审核，1-已通过，2-已驳回，3-已删除", example = "1")
        @NotNull(message = "status must not be null")
        @Min(value = 0, message = "status must be >= 0")
        @Max(value = 3, message = "status must be <= 3")
        Integer status
) implements Serializable {
}
