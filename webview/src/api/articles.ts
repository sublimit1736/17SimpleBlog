import client from './client';
import type {ApiResponse, Article, Interaction, PageResponse} from '../types';

export const articlesApi = {
    getById: (id: number) =>
        client.get<ApiResponse<Article>>(`/articles/view/${id}`),

    getAll: (page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Article>>>('/articles/all', {params: {page, size}}),

    getByAuthor: (authorId: number, page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Article>>>(`/articles/by_author/${authorId}`, {params: {page, size}}),

    searchByTitle: (keyword: string, page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Article>>>('/articles/search/by_title', {params: {keyword, page, size}}),

    searchByTags: (keyword: string, page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Article>>>('/articles/search/by_tags', {params: {keyword, page, size}}),

    /**
     * Upload a new article from a multipart FormData.
     * Required fields: title, contentType, content (File).
     * Optional fields: tags (string), images (File[]).
     */
    upload: (formData: FormData) =>
        client.post<ApiResponse<Article>>('/articles/upload', formData, {
            headers: {'Content-Type': 'multipart/form-data'},
        }),

    delete: (id: number) =>
        client.delete<ApiResponse<void>>(`/articles/delete/${id}`),

    toggleLike: (id: number) =>
        client.post<ApiResponse<boolean>>(`/articles/${id}/like`),

    toggleFavorite: (id: number) =>
        client.post<ApiResponse<boolean>>(`/articles/${id}/favorite`),

    getInteractions: (id: number) =>
        client.get<ApiResponse<Interaction>>(`/articles/${id}/interactions`),

    getLikedArticles: (uid: number, page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Article>>>(`/articles/profile/${uid}/likes`, {params: {page, size}}),

    getFavoritedArticles: (uid: number, page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Article>>>(`/articles/profile/${uid}/favorites`, {params: {page, size}}),
};
