package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.common.CacheNames;
import cn.chunana.simblog17api.common.CacheKeys;
import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.CommentResponse;
import cn.chunana.simblog17api.dto.response.HomeHotTagEntry;
import cn.chunana.simblog17api.dto.response.HomeSiteStatsResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Article;
import cn.chunana.simblog17api.entities.Comment;
import cn.chunana.simblog17api.mapper.ArticleMapper;
import cn.chunana.simblog17api.mapper.CommentMapper;
import cn.chunana.simblog17api.repository.ArticleRepository;
import cn.chunana.simblog17api.repository.CommentRepository;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    @Cacheable(value = CacheNames.HOME_LATEST, key = CacheKeys.HOME_PAGEABLE)
    public PageResponse<ArticleMetaResponse> getLatestArticles(Pageable pageable) {
        return PageResponse.from(
                articleRepository.findByStatusOrderByPublishedTimeDesc(Article.STATUS_PUBLISHED, pageable)
                        .map(ArticleMapper::toArticleMetaResponse));
    }

    @Override
    @Cacheable(value = CacheNames.HOME_HOT, key = CacheKeys.HOME_DAYS_PAGEABLE)
    public PageResponse<ArticleMetaResponse> getHotArticles(int days, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return PageResponse.from(
                articleRepository.findHotArticles(since, pageable)
                        .map(ArticleMapper::toArticleMetaResponse));
    }

    @Override
    @Cacheable(value = CacheNames.HOME_STATS, key = "'global'")
    public HomeSiteStatsResponse getSiteStats() {
        long totalUsers = userRepository.count();
        long totalArticles = articleRepository.countByStatus(Article.STATUS_PUBLISHED);
        long totalComments = commentRepository.countByStatus(Comment.STATUS_APPROVED);
        long totalViews = articleRepository.sumViewCount();

        return HomeSiteStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalArticles(totalArticles)
                .totalComments(totalComments)
                .totalViews(totalViews)
                .build();
    }

    @Override
    @Cacheable(value = CacheNames.HOME_HOT_TAGS, key = "#limit")
    public List<HomeHotTagEntry> getHotTags(int limit) {
        List<String> rawTags = articleRepository.findAllPublishedTags();

        Map<String, Long> tagCounts = rawTags.stream()
                .flatMap(tags -> Arrays.stream(tags.split(",")))
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return tagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .map(e -> HomeHotTagEntry.builder().tag(e.getKey()).count(e.getValue()).build())
                .toList();
    }

    @Override
    @Cacheable(value = CacheNames.HOME_RECENT_COMMENTS, key = CacheKeys.HOME_PAGEABLE)
    public PageResponse<CommentResponse> getRecentComments(Pageable pageable) {
        return PageResponse.from(
                commentRepository.findByStatusOrderByCreateTimeDesc(Comment.STATUS_APPROVED, pageable)
                        .map(CommentMapper::toCommentResponse));
    }
}
