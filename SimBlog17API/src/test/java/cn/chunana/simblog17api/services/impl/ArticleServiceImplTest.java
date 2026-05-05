package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.dto.request.ArticleRequest;
import cn.chunana.simblog17api.entities.Article;
import cn.chunana.simblog17api.repository.ArticleRepository;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;


    @InjectMocks
    private ArticleServiceImpl articleService;


    @Test
    void deleteArticleShouldMarkStatusAsDeleted() {
        Article article = Article.builder().id(1).status(Article.STATUS_PUBLISHED).build();
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.save(article)).thenReturn(article);

        Optional<Article> deleted = articleService.deleteArticle(1L);

        assertTrue(deleted.isPresent());
        assertEquals(Article.STATUS_DELETED, deleted.get().getStatus());
    }

    @Test
    void createArticleShouldRejectHtmlWithJavascript() {
        ArticleRequest request = ArticleRequest.builder()
                                              .title("unsafe")
                                              .content("<script>alert('x')</script>")
                                              .contentType(Article.CONTENT_TYPE_HTML)
                                              .authorId(1001L)
                                              .tags("java")
                                              .build();

        assertThrows(IllegalArgumentException.class, () -> articleService.createArticle(request));
    }
}

