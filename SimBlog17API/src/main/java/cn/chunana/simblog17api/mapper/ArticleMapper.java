package cn.chunana.simblog17api.mapper;

import cn.chunana.simblog17api.dto.request.ArticleRequest;
import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.ArticleResponse;
import cn.chunana.simblog17api.entities.Article;
import cn.chunana.simblog17api.utils.HtmlSanitizer;

public final class ArticleMapper {

    private ArticleMapper() {
    }

    public static Article toNewArticle(ArticleRequest articleRequest) {
        String normalizedContent = normalizeContentByType(articleRequest.content(), articleRequest.contentType());
        return Article.builder()
                      .title(articleRequest.title())
                      .content(normalizedContent)
                      .contentType(articleRequest.contentType())
                      .preview(toPreview(normalizedContent, articleRequest.contentType()))
                      .authorId(articleRequest.authorId())
                      .tags(articleRequest.tags())
                      .status(Article.STATUS_PUBLISHED)
                      .viewCount(0)
                      .build();
    }

    public static void applyUpdatableFields(Article article, ArticleRequest articleRequest) {
        String normalizedContent = normalizeContentByType(articleRequest.content(), articleRequest.contentType());
        article.setTitle(articleRequest.title());
        article.setContent(normalizedContent);
        article.setContentType(articleRequest.contentType());
        article.setPreview(toPreview(normalizedContent, articleRequest.contentType()));
        article.setTags(articleRequest.tags());
    }

    public static ArticleResponse toArticleResponse(Article article) {
        return ArticleResponse.builder()
                              .id(article.getId())
                              .title(article.getTitle())
                              .content(article.getContent())
                              .contentType(article.getContentType())
                              .preview(article.getPreview())
                              .authorId(article.getAuthorId())
                              .publishedTime(article.getPublishedTime())
                              .updatedTime(article.getUpdatedTime())
                              .viewCount(article.getViewCount())
                              .tags(article.getTags())
                              .status(article.getStatus())
                              .build();
    }

    public static ArticleMetaResponse toArticleMetaResponse(Article article) {
        return ArticleMetaResponse.builder()
                                  .id(article.getId())
                                  .title(article.getTitle())
                                  .contentType(article.getContentType())
                                  .authorId(article.getAuthorId())
                                  .publishedTime(article.getPublishedTime())
                                  .updatedTime(article.getUpdatedTime())
                                  .viewCount(article.getViewCount())
                                  .tags(article.getTags())
                                  .status(article.getStatus())
                                  .build();
    }

    private static String toPreview(String content, String contentType) {
        if (content == null) {
            return "";
        }

        if (contentType == null) {
            contentType = Article.CONTENT_TYPE_PLAIN_TEXT;
        }

        String normalized = Article.CONTENT_TYPE_HTML.equals(contentType)
                ? content.replaceAll("<[^>]+>", " ")
                : content;

        String compact = normalized.trim().replaceAll("\\s+", " ");
        int previewLength = Math.min(compact.length(), 120);
        return compact.substring(0, previewLength);
    }

    private static String normalizeContentByType(String content, String contentType) {
        if (Article.CONTENT_TYPE_HTML.equals(contentType)) {
            return HtmlSanitizer.sanitize(content);
        }
        return content;
    }
}


