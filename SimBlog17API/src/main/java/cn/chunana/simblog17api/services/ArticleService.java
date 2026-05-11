package cn.chunana.simblog17api.services;

import cn.chunana.simblog17api.dto.response.ArticleMetaResponse;
import cn.chunana.simblog17api.dto.response.ArticleResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ArticleService {

    /**
     * Create and immediately publish an article from an uploaded content file and optional image files.
     * Image files are stored in the article's own namespace and any relative filename references
     * inside the content are rewritten to the scoped serving URL automatically.
     */
    Article uploadArticle(String title, String tags, String contentType,
                          MultipartFile contentFile, List<MultipartFile> imageFiles,
                          Long authorId);

    Optional<Article> deleteArticle(Long id);

    Optional<Article> getArticleById(Long id);

    PageResponse<ArticleMetaResponse> getAllArticles(Pageable pageable);

    PageResponse<ArticleResponse> getArticlesByAuthorId(Long authorId, Pageable pageable);

    PageResponse<ArticleMetaResponse> searchArticlesByTitle(String key, Pageable pageable);

    PageResponse<ArticleMetaResponse> searchArticlesByTag(String key, Pageable pageable);

    void increaseViewCountsAsync(Long id);
}

