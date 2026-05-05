package cn.chunana.simblog17api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Schema(description = "站内通知响应")
public record NotificationResponse(
        @Schema(description = "通知 ID") Long id,
        @Schema(description = "通知类型") String type,
        @Schema(description = "目标对象类型") String targetType,
        @Schema(description = "目标对象 ID") Long targetId,
        @Schema(description = "标题") String title,
        @Schema(description = "内容") String message,
        @Schema(description = "状态：0-未读，1-已读") Integer status,
        @Schema(description = "创建时间") LocalDateTime createTime,
        @Schema(description = "已读时间") LocalDateTime readTime
) implements Serializable {
}

