// Unified API response
export interface ApiResponse<T> {
    statusCode: number;
    statusMessage: string;
    timeStamp: number;
    traceId: string;
    data: T;
}

// Paged response — matches backend PageResponse record fields
export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    pageNumber: number;
    pageSize: number;
}

// User
export type UserRole = 'USER' | 'ADMIN';

export interface User {
    id: number;
    username: string;
    avatarUrl?: string;
    role: UserRole;
    createTime?: string;
    accessToken?: string;
    refreshToken?: string;
}

export interface LoginResponse extends User {
    accessToken: string;
    refreshToken: string;
}

// Article
export const ArticleStatus = {
    DRAFT: 0,
    PUBLISHED: 1,
    ARCHIVED: 2,
    HIDDEN: 3,
    DELETED: 4,
    PENDING: 5,
} as const;
export type ArticleStatusValue = typeof ArticleStatus[keyof typeof ArticleStatus];

export type ContentType = 'PLAIN_TEXT' | 'MARKDOWN';

export interface Article {
    id: number;
    title: string;
    content?: string;          // only in ArticleResponse (detail), absent in ArticleMetaResponse
    preview?: string;
    contentType: ContentType;
    authorId: number;
    authorName?: string;
    authorAvatarUrl?: string;
    tags: string | null;       // comma-separated string from backend
    status: ArticleStatusValue;
    viewCount: number;
    publishedTime: string;
    updatedTime: string;
}

// Comment
export interface Comment {
    id: number;
    articleId: number;
    content: string;
    authorId: number;
    authorName?: string;
    authorAvatarUrl?: string;
    parentCommentId?: number;
    status: number;
    createTime: string;
    children?: Comment[];
}

// Notification — matches backend NotificationResponse record fields
export interface Notification {
    id: number;
    type: string;
    targetType?: string;
    targetId?: number;
    title?: string;
    message: string;
    status: number;            // 0 = unread, 1 = read
    createTime: string;
    readTime?: string;
}

// Site stats — matches backend HomeSiteStatsResponse
export interface SiteStats {
    totalUsers: number;
    totalArticles: number;
    totalComments: number;
    totalViews: number;
}

// Interaction — matches backend ArticleInteractionResponse
export interface Interaction {
    likeCount: number;
    favoriteCount: number;
    likedByCurrentUser: boolean;
    favoritedByCurrentUser: boolean;
}

// Hot tag — matches backend HomeHotTagEntry
export interface HotTag {
    tag: string;
    count: number;
}

// Media
export interface MediaUpload {
    id: number;
    url: string;
    originalFileName: string;
    fileSize?: number;
    mimeType?: string;
}

// Tokens
export interface TokenPair {
    accessToken: string;
    refreshToken: string;
}
