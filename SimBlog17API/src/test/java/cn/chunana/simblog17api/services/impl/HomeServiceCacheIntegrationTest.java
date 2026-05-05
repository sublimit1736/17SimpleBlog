package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.common.CacheNames;
import cn.chunana.simblog17api.dto.request.ArticleRequest;
import cn.chunana.simblog17api.entities.Article;
import cn.chunana.simblog17api.repository.ArticleRepository;
import cn.chunana.simblog17api.repository.CommentRepository;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.ArticleService;
import cn.chunana.simblog17api.services.HomeService;
import cn.chunana.simblog17api.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HomeServiceCacheIntegrationTest.TestConfig.class)
class HomeServiceCacheIntegrationTest {

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }

        @Bean
        ArticleRepository articleRepository() {
            return Mockito.mock(ArticleRepository.class);
        }

        @Bean
        CommentRepository commentRepository() {
            return Mockito.mock(CommentRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        NotificationService notificationService() {
            return Mockito.mock(NotificationService.class);
        }

        @Bean
        HomeService homeService(ArticleRepository articleRepository,
                                CommentRepository commentRepository,
                                UserRepository userRepository) {
            return new HomeServiceImpl(articleRepository, commentRepository, userRepository);
        }

        @Bean
        ArticleService articleService(ArticleRepository articleRepository,
                                      UserRepository userRepository,
                                      NotificationService notificationService) {
            return new ArticleServiceImpl(articleRepository, userRepository, notificationService);
        }
    }

    @jakarta.annotation.Resource
    private HomeService homeService;

    @jakarta.annotation.Resource
    private ArticleService articleService;

    @jakarta.annotation.Resource
    private ArticleRepository articleRepository;

    @jakarta.annotation.Resource
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(articleRepository);
        if (cacheManager.getCache(CacheNames.HOME_LATEST) != null) {
            cacheManager.getCache(CacheNames.HOME_LATEST).clear();
        }
    }

    @Test
    void getLatestArticlesShouldUseCacheOnSecondCall() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("publishedTime"));
        when(articleRepository.findByStatusOrderByPublishedTimeDesc(eq(Article.STATUS_PUBLISHED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(buildArticle(1))));

        homeService.getLatestArticles(pageable);
        homeService.getLatestArticles(pageable);

        verify(articleRepository, times(1))
                .findByStatusOrderByPublishedTimeDesc(eq(Article.STATUS_PUBLISHED), any(Pageable.class));
    }

    @Test
    void createArticleShouldEvictLatestCache() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("publishedTime"));
        when(articleRepository.findByStatusOrderByPublishedTimeDesc(eq(Article.STATUS_PUBLISHED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(buildArticle(2))));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        homeService.getLatestArticles(pageable);
        verify(articleRepository, times(1))
                .findByStatusOrderByPublishedTimeDesc(eq(Article.STATUS_PUBLISHED), any(Pageable.class));

        articleService.createArticle(ArticleRequest.builder()
                                                   .title("cache evict")
                                                   .content("cache evict content")
                                                   .contentType(Article.CONTENT_TYPE_MARKDOWN)
                                                   .authorId(1001L)
                                                   .tags("spring")
                                                   .build());

        homeService.getLatestArticles(pageable);

        verify(articleRepository, times(2))
                .findByStatusOrderByPublishedTimeDesc(eq(Article.STATUS_PUBLISHED), any(Pageable.class));
    }

    private Article buildArticle(int id) {
        LocalDateTime now = LocalDateTime.of(2026, 5, 4, 12, 0, 0);
        return Article.builder()
                .id(id)
                .title("demo-" + id)
                .content("content")
                .preview("preview")
                .authorId(1001L)
                .publishedTime(now)
                .updatedTime(now)
                .viewCount(1)
                .tags("java")
                .status(Article.STATUS_PUBLISHED)
                .build();
    }
}


