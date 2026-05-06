package cn.chunana.simblog17api.services;

import cn.chunana.simblog17api.dto.request.ArticleRequest;
import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.ArticleResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Article;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ArticleService {
    Article createArticle(ArticleRequest articleRequest);

    Article createDraft(ArticleRequest articleRequest);

    Optional<Article> hideArticle(Long id);

    Optional<Article> publishArticle(Long id);

    Optional<Article> publishDraft(Long id);

    Optional<Article> updateArticle(Long id, ArticleRequest articleRequest);

    Optional<Article> updateDraft(Long id, ArticleRequest articleRequest);

    Optional<Article> deleteArticle(Long id);

    Optional<Article> getArticleById(Long id);

    PageResponse<ArticleMetaResponse> getAllArticles(Pageable pageable);

    PageResponse<ArticleResponse> getArticlesByAuthorId(Long authorId, Pageable pageable);

    PageResponse<ArticleResponse> getDraftArticlesByAuthorId(Long authorId, Pageable pageable);

    PageResponse<ArticleMetaResponse> getPendingArticles(Pageable pageable);

    Optional<Article> moderateArticleStatus(Long id, Integer status);

    PageResponse<ArticleResponse> searchArticlesByTitle(String key, Pageable pageable);

    PageResponse<ArticleResponse> searchArticlesByTag(String key, Pageable pageable);

    void increaseViewCountsAsync(Long id);

}
