// Unified API response
export interface ApiResponse<T> {
  statusCode: number;
  statusMessage: string;
  timeStamp: string;
  traceId: string;
  data: T;
}

// Paged response
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// User
export type UserRole = 'USER' | 'ADMIN';

export interface User {
  id: number;
  username: string;
  avatarUrl?: string;
  role: UserRole;
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

export type ContentType = 'PLAIN_TEXT' | 'MARKDOWN' | 'HTML';

export interface Article {
  id: number;
  title: string;
  content: string;
  summary?: string;
  contentType: ContentType;
  authorId: number;
  authorName?: string;
  authorAvatarUrl?: string;
  tags: string[];
  status: ArticleStatusValue;
  viewCount: number;
  likeCount: number;
  favoriteCount: number;
  commentCount: number;
  createdAt: string;
  updatedAt: string;
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
  createdAt: string;
  children?: Comment[];
}

// Notification
export interface Notification {
  id: number;
  userId: number;
  type: string;
  content: string;
  isRead: boolean;
  createdAt: string;
  relatedId?: number;
}

// Site stats
export interface SiteStats {
  userCount: number;
  articleCount: number;
  commentCount: number;
  totalViews: number;
}

// Interaction
export interface Interaction {
  likeCount: number;
  favoriteCount: number;
  liked: boolean;
  favorited: boolean;
}

// Hot tag
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
