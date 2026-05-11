package cn.chunana.simblog17api.mapper;

import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.ArticleResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Article;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.repository.UserRepository;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ArticleMapper {

    private ArticleMapper() {
    }

    public static ArticleResponse toArticleResponse(Article article) {
        return toArticleResponse(article, null, null);
    }

    public static ArticleResponse toArticleResponse(Article article, String authorName, String authorAvatarUrl) {
        return ArticleResponse.builder()
                              .id(article.getId())
                              .title(article.getTitle())
                              .content(article.getContent())
                              .contentType(article.getContentType())
                              .preview(article.getPreview())
                              .authorId(article.getAuthorId())
                              .authorName(authorName)
                              .authorAvatarUrl(authorAvatarUrl)
                              .publishedTime(article.getPublishedTime())
                              .updatedTime(article.getUpdatedTime())
                              .viewCount(article.getViewCount())
                              .tags(article.getTags())
                              .status(article.getStatus())
                              .build();
    }

    public static ArticleMetaResponse toArticleMetaResponse(Article article) {
        return toArticleMetaResponse(article, null, null);
    }

    public static ArticleMetaResponse toArticleMetaResponse(Article article, String authorName, String authorAvatarUrl) {
        return ArticleMetaResponse.builder()
                                  .id(article.getId())
                                  .title(article.getTitle())
                                  .contentType(article.getContentType())
                                  .authorId(article.getAuthorId())
                                  .authorName(authorName)
                                  .authorAvatarUrl(authorAvatarUrl)
                                  .publishedTime(article.getPublishedTime())
                                  .updatedTime(article.getUpdatedTime())
                                  .viewCount(article.getViewCount())
                                  .tags(article.getTags())
                                  .status(article.getStatus())
                                  .build();
    }

    /**
     * Converts a Page of Articles into a PageResponse of ArticleMetaResponses,
     * enriched with author info fetched in a single batch query.
     */
    public static PageResponse<ArticleMetaResponse> toMetaPageResponse(
            Page<Article> page, UserRepository userRepository) {
        List<Article> articles = page.getContent();
        Map<Long, User> userMap = fetchAuthorMap(articles, userRepository);
        Page<ArticleMetaResponse> mapped = page.map(a -> {
            User author = userMap.get(a.getAuthorId());
            return toArticleMetaResponse(a,
                    author != null ? author.getUsername() : null,
                    author != null ? author.getAvatarUrl() : null);
        });
        return PageResponse.from(mapped);
    }

    /**
     * Converts a Page of Articles into a PageResponse of ArticleResponses,
     * enriched with author info fetched in a single batch query.
     */
    public static PageResponse<ArticleResponse> toFullPageResponse(
            Page<Article> page, UserRepository userRepository) {
        List<Article> articles = page.getContent();
        Map<Long, User> userMap = fetchAuthorMap(articles, userRepository);
        Page<ArticleResponse> mapped = page.map(a -> {
            User author = userMap.get(a.getAuthorId());
            return toArticleResponse(a,
                    author != null ? author.getUsername() : null,
                    author != null ? author.getAvatarUrl() : null);
        });
        return PageResponse.from(mapped);
    }

    /**
     * Converts a list of Articles into a PageResponse of ArticleMetaResponses,
     * enriched with author info, preserving page metadata from the source page.
     */
    public static PageResponse<ArticleMetaResponse> toMetaPageResponseFromList(
            List<Article> articles, long totalElements, int totalPages, int pageNumber, int pageSize,
            UserRepository userRepository) {
        Map<Long, User> userMap = fetchAuthorMap(articles, userRepository);
        List<ArticleMetaResponse> metas = articles.stream()
                .map(a -> {
                    User author = userMap.get(a.getAuthorId());
                    return toArticleMetaResponse(a,
                            author != null ? author.getUsername() : null,
                            author != null ? author.getAvatarUrl() : null);
                })
                .toList();
        return new PageResponse<>(metas, totalElements, totalPages, pageNumber, pageSize);
    }

    /**
     * Build a plain-text preview (up to 120 characters) from raw content.
     */
    public static String buildPreview(String content) {
        if (content == null) return "";
        String compact = content.trim().replaceAll("\\s+", " ");
        int previewLength = Math.min(compact.length(), 120);
        return compact.substring(0, previewLength);
    }

    private static Map<Long, User> fetchAuthorMap(List<Article> articles, UserRepository userRepository) {
        List<Long> authorIds = articles.stream()
                .map(Article::getAuthorId)
                .distinct()
                .toList();
        return userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
