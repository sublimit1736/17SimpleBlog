import client from './client';
import type {ApiResponse, Comment, PageResponse} from '../types';

export const commentsApi = {
    create: (data: { articleId: number; content: string; parentCommentId?: number }) =>
        client.post<ApiResponse<Comment>>('/comments', data),

    getByArticle: (articleId: number, page = 0, size = 20) =>
        client.get<ApiResponse<PageResponse<Comment>>>(`/comments/by_article/${articleId}`, {
            params: {page, size},
        }),

    delete: (id: number) =>
        client.delete<ApiResponse<void>>(`/comments/${id}`),
};
