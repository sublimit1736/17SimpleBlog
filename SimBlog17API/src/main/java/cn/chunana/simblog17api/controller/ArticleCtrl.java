package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.dto.request.ArticleRequest;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
@Slf4j
public class ArticleCtrl {
    private final ArticleService articleService;
    private final UserRepository userRepository;

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<ArticleResponse> createArticle(@Valid @RequestBody ArticleRequest articleRequest,
                                                            Authentication authentication) {
        Long currentUserId = SecurityUtils.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        Article article = articleService.createArticle(withAuthorId(articleRequest, currentUserId));
        log.info("article.create id={} authorId={}", article.getId(), currentUserId);
        return ApiStatusResponse.ok(enrichArticleResponse(article));
    }

    @GetMapping("/view/{id}")
    public ApiStatusResponse<ArticleResponse> viewArticle(@PathVariable Long id) {
        Optional<Article> queryArticle = articleService.getArticleById(id);
        return queryArticle.map(article -> {
            articleService.increaseViewCountsAsync(id);
            return ApiStatusResponse.ok(enrichArticleResponse(article));
        }).orElseGet(() -> ApiStatusResponse.fail(Status.ARTICLE_NOT_FOUND));
    }

    @GetMapping("/all")
    public ApiStatusResponse<PageResponse<ArticleMetaResponse>> getAllArticles(
            @PageableDefault(sort = "publishedTime") Pageable pageable) {
        return ApiStatusResponse.ok(articleService.getAllArticles(pageable));
    }

    @GetMapping("/by_author/{id}")
    public ApiStatusResponse<PageResponse<ArticleResponse>> getArticlesByAuthorId(
            @PathVariable Long id,
            @PageableDefault(sort = "publishedTime") Pageable pageable) {
        return ApiStatusResponse.ok(articleService.getArticlesByAuthorId(id, pageable));
    }

    @GetMapping("/profile/{uid}/drafts")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<PageResponse<ArticleResponse>> getDraftsByAuthorId(
            @PathVariable Long uid,
            Authentication authentication,
            @PageableDefault(sort = "updatedTime") Pageable pageable) {

        Long currentUserId = SecurityUtils.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        boolean isAdmin = SecurityUtils.isAdmin(authentication);
        if (!isAdmin && !currentUserId.equals(uid)) {
            return ApiStatusResponse.fail(Status.ACCESS_DENIED);
        }

        return ApiStatusResponse.ok(articleService.getDraftArticlesByAuthorId(uid, pageable));
    }

    @GetMapping("/search/by_title")
    public ApiStatusResponse<PageResponse<ArticleResponse>> getArticlesByTitle(
            @RequestParam String keyword,
            @PageableDefault(sort = "publishedTime") Pageable pageable) {
        return ApiStatusResponse.ok(articleService.searchArticlesByTitle(keyword, pageable));
    }

    @GetMapping("/search/by_tags")
    public ApiStatusResponse<PageResponse<ArticleResponse>> getArticlesByTags(
            @RequestParam String keyword,
            @PageableDefault(sort = "publishedTime") Pageable pageable) {
        return ApiStatusResponse.ok(articleService.searchArticlesByTag(keyword, pageable));
    }

    @PostMapping("/draft")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<ArticleResponse> createDraft(
            @Valid @RequestBody ArticleRequest articleRequest,
            Authentication authentication) {

        Long currentUserId = SecurityUtils.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        Article article = articleService.createDraft(withAuthorId(articleRequest, currentUserId));
        log.info("article.draft.create id={} authorId={}", article.getId(), currentUserId);
        return ApiStatusResponse.ok(enrichArticleResponse(article));
    }

    @PutMapping("/draft/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<ArticleResponse> updateDraft(@PathVariable Long id,
                                                          @Valid @RequestBody ArticleRequest articleRequest,
                                                          Authentication authentication) {
        Optional<Article> articleOpt = articleService.getArticleById(id);
        if (articleOpt.isEmpty()) {
            return ApiStatusResponse.fail(Status.ARTICLE_NOT_FOUND);
        }

        Long currentUserId = SecurityUtils.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        boolean isAdmin = SecurityUtils.isAdmin(authentication);
        if (!isAdmin && !articleOpt.get().getAuthorId().equals(currentUserId)) {
            return ApiStatusResponse.fail(Status.ACCESS_DENIED);
        }

        return articleService.updateDraft(id, articleRequest)
                             .map(this::enrichArticleResponse)
                             .map(articleResponse -> {
                                 log.info("article.draft.update id={} operatorUid={}", id, currentUserId);
                                 return ApiStatusResponse.ok(articleResponse);
                             })
                             .orElseGet(() -> ApiStatusResponse.fail(Status.INVALID_REQUEST));
    }

    @PostMapping("/draft/{id}/publish")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<ArticleResponse> publishDraft(@PathVariable Long id,
                                                           Authentication authentication) {
        Optional<Article> articleOpt = articleService.getArticleById(id);
        if (articleOpt.isEmpty()) {
            return ApiStatusResponse.fail(Status.ARTICLE_NOT_FOUND);
        }

        Long currentUserId = SecurityUtils.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        boolean isAdmin = SecurityUtils.isAdmin(authentication);
        if (!isAdmin && !articleOpt.get().getAuthorId().equals(currentUserId)) {
            return ApiStatusResponse.fail(Status.ACCESS_DENIED);
        }

        return articleService.publishDraft(id)
                             .map(this::enrichArticleResponse)
                             .map(articleResponse -> {
                                 log.info("article.draft.publish id={} operatorUid={}", id, currentUserId);
                                 return ApiStatusResponse.ok(articleResponse);
                             })
                             .orElseGet(() -> ApiStatusResponse.fail(Status.ARTICLE_NOT_FOUND));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<ArticleResponse> updateArticle(@PathVariable Long id,
                                                            @Valid @RequestBody ArticleRequest articleRequest) {
        return articleService.updateArticle(id, articleRequest)
                             .map(this::enrichArticleResponse)
                             .map(articleResponse -> {
                                 log.info("article.update id={}", id);
                                 return ApiStatusResponse.ok(articleResponse);
                             })
                             .orElseGet(() -> ApiStatusResponse.fail(Status.ARTICLE_NOT_FOUND));
    }

    @PutMapping("/hide/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<ArticleResponse> hideArticle(@PathVariable Long id) {
        return articleService.hideArticle(id)
                             .map(this::enrichArticleResponse)
                             .map(articleResponse -> {
                                 log.info("article.hide id={}", id);
                                 return ApiStatusResponse.ok(articleResponse);
                             })
                             .orElseGet(() -> ApiStatusResponse.fail(Status.ARTICLE_NOT_FOUND));
    }

    @PutMapping("/publish/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<ArticleResponse> publishArticle(@PathVariable Long id) {
        return articleService.publishArticle(id)
                             .map(this::enrichArticleResponse)
                             .map(articleResponse -> {
                                 log.info("article.publish id={}", id);
                                 return ApiStatusResponse.ok(articleResponse);
                             })
                             .orElseGet(() -> ApiStatusResponse.fail(Status.ARTICLE_NOT_FOUND));
    }

    /**
     * 删除文章：
     * <ul>
     *   <li>管理员（ROLE_ADMIN）可删除任意文章</li>
     *   <li>普通用户只能删除自己的文章</li>
     *   <li>未认证请求（开发环境下无 token）不做权限限制</li>
     * </ul>
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<ArticleResponse> deleteArticle(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<Article> articleOpt = articleService.getArticleById(id);
        if (articleOpt.isEmpty()) {
            return ApiStatusResponse.fail(Status.ARTICLE_NOT_FOUND);
        }

        // 仅对已认证请求做权限校验
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
                                 log.info("article.delete id={} operatorUid={}", id, SecurityUtils.getCurrentUserId(authentication));
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

    private ArticleRequest withAuthorId(ArticleRequest original, Long authorId) {
        return ArticleRequest.builder()
                             .title(original.title())
                             .content(original.content())
                             .contentType(original.contentType())
                             .authorId(authorId)
                             .tags(original.tags())
                             .build();
    }
}
