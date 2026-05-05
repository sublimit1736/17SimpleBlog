package cn.chunana.simblog17api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

@Builder
@Schema(description = "文章互动（点赞 / 收藏）状态响应")
public record ArticleInteractionResponse(
        @Schema(description = "点赞数") long likeCount,
        @Schema(description = "当前用户是否已点赞") boolean likedByCurrentUser,
        @Schema(description = "收藏数") long favoriteCount,
        @Schema(description = "当前用户是否已收藏") boolean favoritedByCurrentUser
) implements Serializable {
}

