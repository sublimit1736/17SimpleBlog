package cn.chunana.simblog17api.services;

import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.CommentResponse;
import cn.chunana.simblog17api.dto.response.HomeHotTagEntry;
import cn.chunana.simblog17api.dto.response.HomeSiteStatsResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HomeService {

    /** 最新已发布文章（按 publishedTime 倒序） */
    PageResponse<ArticleMetaResponse> getLatestArticles(Pageable pageable);

    /**
     * 时间窗口内的热门文章（按 viewCount 倒序）。
     *
     * @param days 向前追溯的天数，例如 7
     */
    PageResponse<ArticleMetaResponse> getHotArticles(int days, Pageable pageable);

    /** 站点统计摘要 */
    HomeSiteStatsResponse getSiteStats();

    /**
     * 热门标签排行。
     *
     * @param limit 返回数量上限
     */
    List<HomeHotTagEntry> getHotTags(int limit);

    /** 全站最新评论（按 createTime 倒序） */
    PageResponse<CommentResponse> getRecentComments(Pageable pageable);
}

