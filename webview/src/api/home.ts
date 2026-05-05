import client from './client';
import type {ApiResponse, Article, Comment, HotTag, PageResponse, SiteStats} from '../types';

export const homeApi = {
    getLatest: (page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Article>>>('/home/latest', {params: {page, size}}),

    getHot: (days = 7, page = 0, size = 10) =>
        client.get<ApiResponse<PageResponse<Article>>>('/home/hot', {params: {days, page, size}}),

    getStats: () =>
        client.get<ApiResponse<SiteStats>>('/home/stats'),

    getHotTags: (limit = 20) =>
        client.get<ApiResponse<HotTag[]>>('/home/hot-tags', {params: {limit}}),

    getRecentComments: (page = 0, size = 5) =>
        client.get<ApiResponse<PageResponse<Comment>>>('/home/recent-comments', {params: {page, size}}),
};
