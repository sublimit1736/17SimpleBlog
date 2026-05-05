package cn.chunana.simblog17api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@Schema(description = "Admin article status update request")
public record AdminArticleStatusRequest(
        @Schema(description = "目标文章状态：0-草稿，1-发布，2-归档，3-隐藏，4-删除，5-待审核", example = "1")
        @NotNull(message = "status must not be null")
        @Min(value = 0, message = "status must be >= 0")
        @Max(value = 5, message = "status must be <= 5")
        Integer status
) implements Serializable {
}

