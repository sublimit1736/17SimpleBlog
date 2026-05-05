package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.dto.response.*;
import cn.chunana.simblog17api.services.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "首页", description = "首页聚合数据接口")
public class HomeCtrl {

    private final HomeService homeService;

    // ------------------------------------------------------------------ latest

    @GetMapping("/latest")
    @Operation(
            summary = "最新文章列表",
            description = "按发布时间倒序返回最新已发布文章，支持分页。默认每页 10 条。"
    )
    public ApiStatusResponse<PageResponse<ArticleMetaResponse>> getLatest(
            @PageableDefault(size = 10, sort = "publishedTime") Pageable pageable) {

        return ApiStatusResponse.ok(homeService.getLatestArticles(pageable));
    }

    // --------------------------------------------------------------------- hot

    @GetMapping("/hot")
    @Operation(
            summary = "热门文章列表",
            description = "按浏览量倒序，返回指定天数内最热的文章。days 默认 7，范围 1~365。"
    )
    public ApiStatusResponse<PageResponse<ArticleMetaResponse>> getHot(
            @Parameter(description = "向前追溯天数", example = "7")
            @RequestParam(defaultValue = "7")
            @Min(value = 1, message = "days 最小为 1")
            @Max(value = 365, message = "days 最大为 365")
            int days,

            @PageableDefault(size = 10) Pageable pageable) {

        return ApiStatusResponse.ok(homeService.getHotArticles(days, pageable));
    }

    // ------------------------------------------------------------------- stats

    @GetMapping("/stats")
    @Operation(
            summary = "站点统计",
            description = "返回注册用户数、已发布文章数、评论数、总浏览量。"
    )
    public ApiStatusResponse<HomeSiteStatsResponse> getStats() {
        return ApiStatusResponse.ok(homeService.getSiteStats());
    }

    // --------------------------------------------------------------- hot-tags

    @GetMapping("/hot-tags")
    @Operation(
            summary = "热门标签",
            description = "统计所有已发布文章的标签词频，按使用次数倒序返回 Top N。limit 默认 20，范围 1~100。"
    )
    public ApiStatusResponse<List<HomeHotTagEntry>> getHotTags(
            @Parameter(description = "返回数量上限", example = "20")
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "limit 最小为 1")
            @Max(value = 100, message = "limit 最大为 100")
            int limit) {

        return ApiStatusResponse.ok(homeService.getHotTags(limit));
    }

    // --------------------------------------------------------- recent-comments

    @GetMapping("/recent-comments")
    @Operation(
            summary = "最新评论",
            description = "按评论时间倒序返回全站最新评论，支持分页。默认每页 10 条。"
    )
    public ApiStatusResponse<PageResponse<CommentResponse>> getRecentComments(
            @PageableDefault(size = 10, sort = "createTime") Pageable pageable) {

        return ApiStatusResponse.ok(homeService.getRecentComments(pageable));
    }
}


