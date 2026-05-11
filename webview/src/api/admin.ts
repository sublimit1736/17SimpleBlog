import client from './client';
import type {ApiResponse, Comment, PageResponse, User} from '../types';

export const adminApi = {
    promoteUser: (userId: number) =>
        client.put<ApiResponse<void>>(`/admin/users/${userId}/promote`),

    demoteUser: (userId: number) =>
        client.put<ApiResponse<void>>(`/admin/users/${userId}/demote`),

    getAllComments: (status?: number, page = 0, size = 20) =>
        client.get<ApiResponse<PageResponse<Comment>>>('/admin/comments', {params: {status, page, size}}),

    deleteComment: (id: number) =>
        client.delete<ApiResponse<void>>(`/admin/comments/${id}`),

    updateCommentStatus: (id: number, status: number) =>
        client.put<ApiResponse<void>>(`/admin/comments/${id}/status`, {status}),

    cleanupMedia: (olderThanDays = 7) =>
        client.post<ApiResponse<void>>('/admin/media/cleanup', null, {params: {olderThanDays}}),

    searchUsers: (keyword: string, page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<User>>>('/user/auth/search/by_username', {
            params: {keyword, page, size},
        }),
};
