package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.dto.response.ApiStatusResponse;
import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.ArticleResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Article;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.mapper.ArticleMapper;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.ArticleService;
import cn.chunana.simblog17api.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
@Tag(name = "文章", description = "文章上传、浏览、搜索、删除接口")
@Slf4j
public class ArticleCtrl {

    private final ArticleService   articleService;
    private final UserRepository   userRepository;

    /**
     * Upload a new article from a plain-text or Markdown file, plus optional image files.
     * Each article gets its own image namespace; images can only be referenced within the
     * same article.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传文章", description = "上传文章正文文件（纯文本或 Markdown）及可选配图")
    public ApiStatusResponse<ArticleResponse> uploadArticle(
            @RequestParam("title")
            @NotBlank(message = "title must not be blank")
            @Size(max = 100, message = "title must be at most 100 characters")
            String title,

            @RequestParam(value = "tags", required = false)
            String tags,

            @RequestParam("contentType")
            @Pattern(regexp = "PLAIN_TEXT|MARKDOWN", message = "contentType must be PLAIN_TEXT or MARKDOWN")
            String contentType,

            @RequestParam("content")
            MultipartFile contentFile,

            @RequestParam(value = "images", required = false)
            List<MultipartFile> images,

            Authentication authentication) {

        Long currentUserId = SecurityUtils.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        if (contentFile == null || contentFile.isEmpty()) {
            return ApiStatusResponse.fail(Status.INVALID_REQUEST);
        }

        try {
            Article article = articleService.uploadArticle(
                    title, tags, contentType, contentFile, images, currentUserId);
            log.info("article.upload id={} authorId={}", article.getId(), currentUserId);
            return ApiStatusResponse.ok(enrichArticleResponse(article));
        } catch (IllegalArgumentException ex) {
            log.warn("article.upload.invalid authorId={} err={}", currentUserId, ex.getMessage());
            return ApiStatusResponse.fail(Status.INVALID_REQUEST);
        }
    }

    @GetMapping("/view/{id}")
    @Operation(summary = "查看文章详情")
    public ApiStatusResponse<ArticleResponse> viewArticle(@PathVariable Long id) {
        Optional<Article> queryArticle = articleService.getArticleById(id);
        return queryArticle.map(article -> {
            articleService.increaseViewCountsAsync(id);
            return ApiStatusResponse.ok(enrichArticleResponse(article));
        }).orElseGet(() -> ApiStatusResponse.fail(Status.ARTICLE_NOT_FOUND));
    }

    @GetMapping("/all")
    @Operation(summary = "文章列表（分页）")
    public ApiStatusResponse<PageResponse<ArticleMetaResponse>> getAllArticles(
            @PageableDefault(sort = "publishedTime") Pageable pageable) {
        return ApiStatusResponse.ok(articleService.getAllArticles(pageable));
    }

    @GetMapping("/by_author/{id}")
    @Operation(summary = "按作者获取文章列表")
    public ApiStatusResponse<PageResponse<ArticleResponse>> getArticlesByAuthorId(
            @PathVariable Long id,
            @PageableDefault(sort = "publishedTime") Pageable pageable) {
        return ApiStatusResponse.ok(articleService.getArticlesByAuthorId(id, pageable));
    }

    @GetMapping("/search/by_title")
    @Operation(summary = "按标题搜索文章")
    public ApiStatusResponse<PageResponse<ArticleMetaResponse>> getArticlesByTitle(
            @RequestParam String keyword,
            @PageableDefault(sort = "publishedTime") Pageable pageable) {
        return ApiStatusResponse.ok(articleService.searchArticlesByTitle(keyword, pageable));
    }

    @GetMapping("/search/by_tags")
    @Operation(summary = "按标签搜索文章")
    public ApiStatusResponse<PageResponse<ArticleMetaResponse>> getArticlesByTags(
            @RequestParam String keyword,
            @PageableDefault(sort = "publishedTime") Pageable pageable) {
        return ApiStatusResponse.ok(articleService.searchArticlesByTag(keyword, pageable));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除文章（作者或管理员）")
    public ApiStatusResponse<ArticleResponse> deleteArticle(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<Article> articleOpt = articleService.getArticleById(id);
        if (articleOpt.isEmpty()) {
            return ApiStatusResponse.fail(Status.ARTICLE_NOT_FOUND);
        }

        if (SecurityUtils.isAuthenticated(authentication)) {
            boolean isAdmin       = SecurityUtils.isAdmin(authentication);
            Long    currentUserId = SecurityUtils.getCurrentUserId(authentication);

            if (!isAdmin && !articleOpt.get().getAuthorId().equals(currentUserId)) {
                return ApiStatusResponse.fail(Status.ACCESS_DENIED);
            }
        }

        return articleService.deleteArticle(id)
                             .map(this::enrichArticleResponse)
                             .map(articleResponse -> {
                                 log.info("article.delete id={} operatorUid={}",
                                          id, SecurityUtils.getCurrentUserId(authentication));
                                 return ApiStatusResponse.ok(articleResponse);
                             })
                             .orElseGet(() -> ApiStatusResponse.fail(Status.ARTICLE_NOT_FOUND));
    }

    private ArticleResponse enrichArticleResponse(Article article) {
        User author = article.getAuthorId() != null
                ? userRepository.findById(article.getAuthorId()).orElse(null)
                : null;
        return ArticleMapper.toArticleResponse(article,
                author != null ? author.getUsername() : null,
                author != null ? author.getAvatarUrl() : null);
    }
}
