package cn.chunana.simblog17api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

@Builder
@Schema(description = "热门标签条目")
public record HomeHotTagEntry(
        @Schema(description = "标签名", example = "java") String tag,
        @Schema(description = "使用次数", example = "42") long count
) implements Serializable {
}

