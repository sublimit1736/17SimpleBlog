package cn.chunana.simblog17api.dto.response;

import cn.chunana.simblog17api.entities.Article;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link cn.chunana.simblog17api.entities.Article} without content fields.
 */
@Builder
@Schema(description = "Article response without content and preview")
public record ArticleMetaResponse(
        @Schema(description = "Article id", example = "1")
        Integer id,

        @Schema(description = "Article title", example = "My first article")
        String title,

        @Schema(description = "Article content type", example = "MARKDOWN")
        String contentType,

        @Schema(description = "Author user id", example = "1001")
        Long authorId,

        @Schema(description = "Published time", example = "2026-04-11T11:00:00")
        LocalDateTime publishedTime,

        @Schema(description = "Last updated time", example = "2026-04-11T11:05:00")
        LocalDateTime updatedTime,

        @Schema(description = "View count", example = "256")
        Integer viewCount,

        @Schema(description = "Tags separated by comma", example = "java,spring")
        String tags,

        @Schema(description = "Status code", example = "1")
        Integer status
) implements Serializable {
}

