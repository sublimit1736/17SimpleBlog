package cn.chunana.simblog17api.mapper;

import cn.chunana.simblog17api.dto.request.ArticleRequest;
import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.ArticleResponse;
import cn.chunana.simblog17api.entities.Article;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArticleMapperTest {

    @Test
    void toNewArticleShouldBuildPublishedArticleWithPreviewAndDefaults() {
        ArticleRequest request = ArticleRequest.builder()
                                              .title("title")
                                              .content("hello world")
                                              .contentType(Article.CONTENT_TYPE_PLAIN_TEXT)
                                              .authorId(1001L)
                                              .tags(List.of("java", "spring"))
                                              .build();

        Article article = ArticleMapper.toNewArticle(request);

        assertEquals("title", article.getTitle());
        assertEquals("hello world", article.getContent());
        assertEquals("hello world", article.getPreview());
        assertEquals(Article.CONTENT_TYPE_PLAIN_TEXT, article.getContentType());
        assertEquals(1001L, article.getAuthorId());
        assertEquals("java,spring", article.getTags());
        assertEquals(Article.STATUS_PUBLISHED, article.getStatus());
        assertEquals(0, article.getViewCount());
    }

    @Test
    void applyUpdatableFieldsShouldRefreshTitleContentPreviewAndTags() {
        Article article = Article.builder()
                                 .title("old")
                                 .content("old content")
                                 .preview("old preview")
                                 .tags("old")
                                 .build();
        ArticleRequest request = ArticleRequest.builder()
                                              .title("new")
                                              .content("intro<p>full body</p>")
                                              .contentType(Article.CONTENT_TYPE_HTML)
                                              .authorId(1001L)
                                              .tags(List.of("new"))
                                              .build();

        ArticleMapper.applyUpdatableFields(article, request);

        assertEquals("new", article.getTitle());
        assertTrue(article.getContent().contains("intro"));
        assertTrue(article.getContent().contains("<p>full body</p>"));
        assertEquals("intro full body", article.getPreview());
        assertEquals(Article.CONTENT_TYPE_HTML, article.getContentType());
        assertEquals("new", article.getTags());
    }

    @Test
    void toNewArticleShouldSanitizeHtmlContent() {
        ArticleRequest request = ArticleRequest.builder()
                                              .title("unsafe")
                                              .content("<p>ok</p><img src='https://cdn.example.com/a.png'>")
                                              .contentType(Article.CONTENT_TYPE_HTML)
                                              .authorId(1001L)
                                              .tags(List.of("security"))
                                              .build();

        Article article = ArticleMapper.toNewArticle(request);

        assertTrue(article.getContent().contains("<p>ok</p>"));
        assertTrue(article.getContent().contains("<img src=\"https://cdn.example.com/a.png\">"));
        assertEquals("ok", article.getPreview());
    }

    @Test
    void toNewArticleShouldRejectHtmlWithJavascript() {
        ArticleRequest request = ArticleRequest.builder()
                                              .title("unsafe")
                                              .content("<p>ok</p><script>alert('x')</script>")
                                              .contentType(Article.CONTENT_TYPE_HTML)
                                              .authorId(1001L)
                                              .tags(List.of("security"))
                                              .build();

        assertThrows(IllegalArgumentException.class, () -> ArticleMapper.toNewArticle(request));
    }

    @Test
    void toResponsesShouldMapExpectedFields() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 11, 12, 0);
        Article article = Article.builder()
                                 .id(1)
                                 .title("mapped")
                                 .content("content")
                                 .contentType(Article.CONTENT_TYPE_MARKDOWN)
                                 .preview("preview")
                                 .authorId(1001L)
                                 .publishedTime(now)
                                 .updatedTime(now)
                                 .viewCount(9)
                                 .tags("java")
                                 .status(Article.STATUS_PUBLISHED)
                                 .build();

        ArticleResponse response = ArticleMapper.toArticleResponse(article);
        ArticleMetaResponse metaResponse = ArticleMapper.toArticleMetaResponse(article);

        assertEquals(1, response.id());
        assertEquals("content", response.content());
        assertEquals(Article.CONTENT_TYPE_MARKDOWN, response.contentType());
        assertEquals(1, metaResponse.id());
        assertEquals(Article.CONTENT_TYPE_MARKDOWN, metaResponse.contentType());
        assertFalse(metaResponse.title().isBlank());
    }
}

