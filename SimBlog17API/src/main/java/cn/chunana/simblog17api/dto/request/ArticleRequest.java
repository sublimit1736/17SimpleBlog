package cn.chunana.simblog17api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link cn.chunana.simblog17api.entities.Article}
 */
@Builder
@Schema(description = "Article create/update request")
public record ArticleRequest(
        @Schema(description = "Article title", example = "My first article")
        @NotBlank(message = "title must not be blank")
        @Size(max = 100, message = "title length must be at most 100")
        String title,

        @Schema(description = "Article content", example = "<p>Hello World</p>")
        @NotBlank(message = "content must not be blank")
        String content,

        @Schema(description = "Content type", example = "MARKDOWN")
        @NotNull(message = "contentType must not be null")
        @Pattern(regexp = "PLAIN_TEXT|MARKDOWN|HTML", message = "contentType must be one of PLAIN_TEXT, MARKDOWN, HTML")
        String contentType,

        @Schema(description = "Author user id", example = "1001")
        Long authorId,

        @Schema(description = "Article tags", example = "[\"java\",\"spring\"]")
        @Size(max = 20, message = "tags count must be at most 20")
        List<String> tags
) implements Serializable {
}