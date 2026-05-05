package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Article;
import cn.chunana.simblog17api.exception.GlobalExceptionHandler;
import cn.chunana.simblog17api.services.ArticleService;
import cn.chunana.simblog17api.utils.JwtUtils;
import cn.chunana.simblog17api.utils.TokenSecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleCtrl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ArticleCtrlTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleService articleService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private TokenSecurityService tokenSecurityService;

    @Test
    void viewArticleShouldReturnArticleResponse() throws Exception {
        Article article = buildArticle(1, "title", "content", "preview");
        when(articleService.getArticleById(1L)).thenReturn(Optional.of(article));

        mockMvc.perform(get("/api/articles/view/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.statusCode").value(Status.SUCCESS.getCode()))
               .andExpect(jsonPath("$.data.id").value(1))
               .andExpect(jsonPath("$.data.content").value("content"))
               .andExpect(jsonPath("$.data.preview").value("preview"));

        verify(articleService).increaseViewCountsAsync(1L);
    }

    @Test
    void getAllArticlesShouldReturnMetaResponsePage() throws Exception {
        ArticleMetaResponse article = ArticleMetaResponse.builder()
                                                         .id(2)
                                                         .title("meta")
                                                         .contentType(Article.CONTENT_TYPE_MARKDOWN)
                                                         .authorId(1001L)
                                                         .publishedTime(LocalDateTime.of(2026, 4, 11, 12, 0, 0))
                                                         .updatedTime(LocalDateTime.of(2026, 4, 11, 12, 0, 0))
                                                         .viewCount(10)
                                                         .tags("java")
                                                         .status(Article.STATUS_PUBLISHED)
                                                         .build();
        PageResponse<ArticleMetaResponse> page = new PageResponse<>(List.of(article), 1, 1, 0, 20);
        when(articleService.getAllArticles(org.mockito.ArgumentMatchers.any())).thenReturn(page);

        mockMvc.perform(get("/api/articles/all"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.statusCode").value(Status.SUCCESS.getCode()))
               .andExpect(jsonPath("$.data.content[0].id").value(2))
               .andExpect(jsonPath("$.data.content[0].title").value("meta"))
               .andExpect(jsonPath("$.data.content[0].content").doesNotExist())
               .andExpect(jsonPath("$.data.content[0].preview").doesNotExist());
    }

    @Test
    void createArticleShouldReturnUnauthorizedWhenAuthenticationUnavailable() throws Exception {
        mockMvc.perform(post("/api/articles/new")
                                .with(authentication(new TestingAuthenticationToken("1001", "N/A", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                   "title": "new",
                                                   "content": "body",
                                                   "contentType": "MARKDOWN",
                                                   "authorId": 1001,
                                                   "tags": ["java"]
                                                 }
                                                 """))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.statusCode").value(Status.UNAUTHORIZED.getCode()));
    }

    private Article buildArticle(Integer id, String title, String content, String preview) {
        LocalDateTime now = LocalDateTime.of(2026, 4, 11, 12, 0, 0);
        return Article.builder()
                      .id(id)
                      .title(title)
                      .content(content)
                      .contentType(Article.CONTENT_TYPE_MARKDOWN)
                      .preview(preview)
                      .authorId(1001L)
                      .publishedTime(now)
                      .updatedTime(now)
                      .viewCount(10)
                      .tags("java")
                      .status(Article.STATUS_PUBLISHED)
                      .build();
    }
}

