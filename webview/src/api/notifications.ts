import client from './client';
import type {ApiResponse, Notification, PageResponse} from '../types';

export const notificationsApi = {
    getAll: (page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Notification>>>('/notifications', {params: {page, size}}),

    getUnreadCount: () =>
        client.get<ApiResponse<number>>('/notifications/unread-count'),

    markRead: (id: number) =>
        client.put<ApiResponse<void>>(`/notifications/${id}/read`),

    markAllRead: () =>
        client.put<ApiResponse<void>>('/notifications/read-all'),
};
