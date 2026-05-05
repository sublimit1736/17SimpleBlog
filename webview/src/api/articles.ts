import client from './client';
import type {ApiResponse, Article, PageResponse} from '../types';

export const articlesApi = {
    create: (data: { title: string; content: string; contentType: string; authorId: number; tags: string[] }) =>
        client.post<ApiResponse<Article>>('/articles/new', data),

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

    createDraft: (data: { title: string; content: string; contentType: string; authorId: number; tags: string[] }) =>
        client.post<ApiResponse<Article>>('/articles/draft', data),

    getDrafts: (uid: number, page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Article>>>(`/articles/profile/${uid}/drafts`, {params: {page, size}}),

    updateDraft: (id: number, data: Partial<Article>) =>
        client.put<ApiResponse<Article>>(`/articles/draft/${id}`, data),

    search: (keyword: string, page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Article>>>('/articles/search/by_title', {params: {keyword, page, size}}),

    saveDraft: (data: { title: string; content: string; summary?: string; contentType: string; tags: string[] }) =>
        client.post<ApiResponse<Article>>('/articles/draft', data),

    publishNew: (data: { title: string; content: string; summary?: string; contentType: string; tags: string[] }) =>
        client.post<ApiResponse<Article>>('/articles/new', data),

    publishDraft: (id: number) =>
        client.post<ApiResponse<Article>>(`/articles/draft/${id}/publish`),

    update: (id: number, data: Partial<Article>) =>
        client.put<ApiResponse<Article>>(`/articles/update/${id}`, data),

    hide: (id: number) =>
        client.put<ApiResponse<void>>(`/articles/hide/${id}`),

    publish: (id: number) =>
        client.put<ApiResponse<void>>(`/articles/publish/${id}`),

    delete: (id: number) =>
        client.delete<ApiResponse<void>>(`/articles/delete/${id}`),

    toggleLike: (id: number) =>
        client.post<ApiResponse<boolean>>(`/articles/${id}/like`),

    toggleFavorite: (id: number) =>
        client.post<ApiResponse<boolean>>(`/articles/${id}/favorite`),

    getInteractions: (id: number) =>
        client.get<ApiResponse<{
            likeCount: number;
            favoriteCount: number;
            liked: boolean;
            favorited: boolean
        }>>(`/articles/${id}/interactions`),

    getLikedArticles: (uid: number, page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Article>>>(`/articles/profile/${uid}/likes`, {params: {page, size}}),

    getFavoritedArticles: (uid: number, page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Article>>>(`/articles/profile/${uid}/favorites`, {params: {page, size}}),
};
