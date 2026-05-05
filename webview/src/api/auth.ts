import client from './client';
import type {ApiResponse, LoginResponse, PageResponse, User} from '../types';

export const authApi = {
    login: (username: string, password: string) =>
        client.post<ApiResponse<LoginResponse>>('/user/auth/login', {username, password}),

    register: (username: string, password: string) =>
        client.post<ApiResponse<User>>('/user/auth/register', {username, password}),

    getProfile: (uid: number) =>
        client.get<ApiResponse<User>>(`/user/auth/profile/${uid}`),

    updateProfile: (uid: number, username: string) =>
        client.put<ApiResponse<User>>(`/user/auth/profile/${uid}`, {username}),

    updateAvatar: (uid: number, file: File) => {
        const form = new FormData();
        form.append('file', file);
        return client.put<ApiResponse<User>>(`/user/auth/profile/${uid}/avatar`, form);
    },

    changePassword: (oldPassword: string, newPassword: string) =>
        client.put<ApiResponse<void>>('/user/auth/password', {oldPassword, newPassword}),

    logout: (refreshToken: string) =>
        client.post<ApiResponse<void>>('/user/auth/logout', null, {
            headers: {'X-Refresh-Token': refreshToken},
        }),

    searchByUsername: (keyword: string, page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<User>>>('/user/auth/search/by_username', {
            params: {keyword, page, size},
        }),
};
