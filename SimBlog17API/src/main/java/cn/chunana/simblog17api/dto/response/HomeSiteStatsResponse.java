package cn.chunana.simblog17api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

@Builder
@Schema(description = "站点统计数据")
public record HomeSiteStatsResponse(
        @Schema(description = "注册用户总数") long totalUsers,
        @Schema(description = "已发布文章总数") long totalArticles,
        @Schema(description = "评论总数") long totalComments,
        @Schema(description = "全站总浏览量") long totalViews
) implements Serializable {
}

