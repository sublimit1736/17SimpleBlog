# SimBlog17API 接口文档

## 1. 项目简介

`SimBlog17API` 是一个轻量论坛后端，核心能力包括：

- 用户注册、登录、双令牌认证（access/refresh）
- 文章发布、草稿流转、审核与管理
- 评论与回复（平铺结构）
- 点赞与收藏
- 首页聚合数据（最新、热门、统计、标签、最新评论）
- Redis 缓存与安全增强（限流、token 吊销、traceId 追踪）

技术栈：Spring Boot + Spring Security + JWT + JPA(PostgreSQL) + Redis。

---

## 2. 全局约定

### 2.1 基础路径

- API 基础前缀：`/api`

### 2.2 统一响应结构

所有接口统一返回 `ApiStatusResponse<T>`：

```json
{
  "statusCode": 0,
  "statusMessage": "Internal Success",
  "timeStamp": 1777955448563,
  "traceId": "a3fd40f9-7454-4936-9a8a-a88bc197a8fe",
  "data": {}
}
```

字段说明：

- `statusCode`：业务状态码（见 2.6）
- `statusMessage`：业务状态说明
- `timeStamp`：服务端毫秒时间戳
- `traceId`：链路追踪 ID（同时会在响应头 `X-Trace-Id` 返回）
- `data`：具体业务返回体

### 2.3 认证与令牌

登录后会返回：

- `accessToken`：访问令牌（短期）
- `refreshToken`：刷新令牌（长期）

访问受保护接口时：

- 请求头：`Authorization: Bearer <accessToken>`

刷新令牌接口：

- 请求头：`X-Refresh-Token: <refreshToken>`

登出接口支持同时吊销：

- `Authorization: Bearer <accessToken>`
- `X-Refresh-Token: <refreshToken>`

### 2.4 TraceId

- 入站可选：`X-Trace-Id`
- 若未传，服务端自动生成
- 出站必带：`X-Trace-Id`
- 响应体也会带 `traceId`

### 2.5 分页参数

分页接口支持标准参数：

- `page`：页码（从 0 开始）
- `size`：每页条数
- `sort`：排序，如 `sort=publishedTime,desc`

分页返回统一为 `PageResponse<T>`：

```json
{
  "content": [],
  "totalElements": 100,
  "totalPages": 10,
  "number": 0,
  "size": 10
}
```

### 2.6 业务状态码（`Status`）

- `0` SUCCESS
- `1` UNEXPECTED_ERROR
- `2` INVALID_REQUEST
- `3` RESOURCE_NOT_FOUND
- `4` ACCESS_DENIED
- `5` UNAUTHORIZED
- `6` TOO_MANY_REQUESTS
- `1001` LOGIN_FAILED
- `2001` USER_ALREADY_EXISTS
- `2002` USER_NOT_FOUND
- `2003` OLD_PASSWORD_INCORRECT
- `3001` ARTICLE_NOT_FOUND
- `4001` COMMENT_NOT_FOUND

### 2.7 错误处理速查表

| `statusCode` | 含义 | 常见触发场景 | 前端处理建议 |
|---|---|---|---|
| `2` | INVALID_REQUEST | 参数校验失败、请求体格式不合法 | 直接提示用户修正输入 |
| `4` | ACCESS_DENIED | 已登录但无权限（如非管理员操作管理接口） | 提示无权限并回退上一页 |
| `5` | UNAUTHORIZED | 未登录、token 缺失/失效/被吊销 | 跳转登录页并清理本地 token |
| `6` | TOO_MANY_REQUESTS | 登录/刷新频率过高触发限流 | 提示稍后重试并做按钮节流 |
| `3/2002/3001/4001` | 资源不存在 | 用户/文章/评论不存在 | 展示空态或 404 页面 |

> 说明：HTTP 层可能同时返回 `401/403/429`，业务状态请以响应体中的 `statusCode` 为准。

### 2.8 前端统一拦截器伪代码

```ts
// axios/fetch response interceptor pseudo code
function handleApiResponse(httpStatus: number, body: ApiStatusResponse<any>) {
  const code = body?.statusCode;

  if (code === 0) return body.data;

  // Priority 1: authentication and permission
  if (code === 5 || httpStatus === 401) {
    clearLocalTokens();
    redirectToLogin();
    throw new Error("UNAUTHORIZED");
  }
  if (code === 4 || httpStatus === 403) {
    toast("你没有权限执行该操作");
    throw new Error("ACCESS_DENIED");
  }

  // Priority 2: rate-limit and input validation
  if (code === 6 || httpStatus === 429) {
    toast("请求过于频繁，请稍后重试");
    throttleCurrentAction();
    throw new Error("TOO_MANY_REQUESTS");
  }
  if (code === 2 || httpStatus === 400) {
    toast(body?.data ?? "请求参数不合法");
    throw new Error("INVALID_REQUEST");
  }

  // Priority 3: not found and fallback
  if (code === 3 || code === 2002 || code === 3001 || code === 4001 || httpStatus === 404) {
    renderNotFoundOrEmptyState();
    throw new Error("NOT_FOUND");
  }

  toast(body?.statusMessage ?? "系统异常，请稍后重试");
  throw new Error("UNEXPECTED_ERROR");
}
```

---

## 3. 用户认证与用户信息接口

前缀：`/api/user/auth`

### 3.1 登录

- `POST /login`
- 鉴权：无需登录
- 请求体：`UserAccessRequest`

```json
{
  "username": "demo_user",
  "password": "secret123"
}
```

- 返回：`UserAccessResponse`（含 `accessToken`、`refreshToken`）
- 备注：有 IP + username 维度限流

### 3.2 注册

- `POST /register`
- 鉴权：无需登录
- 请求体：`UserAccessRequest`
- 返回：`UserAccessResponse`（无 token）

### 3.3 用户详情

- `GET /profile/{uid}`
- 鉴权：无需登录
- 返回：指定用户基本信息

### 3.4 修改用户名

- `PUT /profile/{uid}`
- 鉴权：需要登录（本人或管理员）
- 请求体：`UpdateUsernameRequest`

```json
{ "username": "new_name" }
```

### 3.5.1 上传头像

- `PUT /profile/{uid}/avatar`
- 鉴权：需要登录（本人或管理员）
- 请求类型：`multipart/form-data`
- 表单字段：`file`
- 返回：`MediaUploadResponse`（包含头像 URL）

请求示例：

```bash
curl -X PUT "http://localhost:8080/api/user/auth/profile/1001/avatar" \
  -H "Authorization: Bearer <accessToken>" \
  -F "file=@avatar.png"
```

响应示例：

```json
{
  "statusCode": 0,
  "statusMessage": "Internal Success",
  "timeStamp": 1778001000000,
  "traceId": "4d9cafe8-2f4f-4f9f-9a65-a28f8d1f5f49",
  "data": {
    "id": 501,
    "url": "/api/media/files/avatar_1001_abcd.png",
    "originalFileName": "avatar.png",
    "storedFileName": "avatar_1001_abcd.png",
    "contentType": "image/png",
    "sizeBytes": 93214
  }
}
```

### 3.5 修改密码

- `PUT /password`
- 鉴权：需要登录
- 请求体：`ChangePasswordRequest`

```json
{
  "oldPassword": "oldSecret123",
  "newPassword": "newSecret456"
}
```

### 3.6 刷新令牌

- `POST /refresh`
- 鉴权：无需 access token
- 请求头：`X-Refresh-Token`
- 返回：新的 `accessToken` + `refreshToken`
- 备注：旧 refresh token 会被吊销（轮换）

请求示例：

```bash
curl -X POST "http://localhost:8080/api/user/auth/refresh" \
  -H "X-Refresh-Token: <refreshToken>"
```

响应示例：

```json
{
  "statusCode": 0,
  "statusMessage": "Internal Success",
  "timeStamp": 1778001000123,
  "traceId": "113bf5c3-9c8f-4d57-a4bc-a06ac90ca7f6",
  "data": {
    "id": 1001,
    "username": "demo_user",
    "avatarUrl": "/api/media/files/avatar_1001_abcd.png",
    "role": "USER",
    "createTime": "2026-05-01T12:00:00",
    "accessToken": "<new-access-token>",
    "refreshToken": "<new-refresh-token>"
  }
}
```

### 3.7 登出

- `POST /logout`
- 鉴权：需要登录
- 可选请求头：
  - `Authorization: Bearer <accessToken>`
  - `X-Refresh-Token: <refreshToken>`
- 返回：`true`
- 备注：服务端会将传入 token 加入吊销列表

### 3.8 用户名搜索（纯文本）

- `GET /search/by_username?keyword=...`
- 鉴权：无需登录
- 返回：`PageResponse<UserAccessResponse>`

### 3.9 用户名搜索（正则）

- `GET /search/regex/by_username?pattern=...`
- 鉴权：无需登录
- 返回：`PageResponse<UserAccessResponse>`

---

## 4. 文章接口

前缀：`/api/articles`

### 4.1 新建文章

- `POST /new`
- 鉴权：需要登录
- 请求体：`ArticleRequest`

```json
{
  "title": "My first article",
  "content": "# Hello",
  "contentType": "MARKDOWN",
  "authorId": 1001,
  "tags": "java,spring"
}
```

说明：服务端会以当前登录用户作为作者，忽略客户端伪造作者 ID。

### 4.2 查看文章

- `GET /view/{id}`
- 鉴权：无需登录
- 返回：`ArticleResponse`
- 备注：会异步增加浏览量

### 4.3 已发布文章列表

- `GET /all`
- 鉴权：无需登录
- 返回：`PageResponse<ArticleMetaResponse>`

### 4.4 按作者查询文章

- `GET /by_author/{id}`
- 鉴权：无需登录
- 返回：`PageResponse<ArticleResponse>`

### 4.5 标题搜索（纯文本）

- `GET /search/by_title?keyword=...`
- 鉴权：无需登录
- 返回：`PageResponse<ArticleResponse>`

### 4.6 标签搜索（纯文本）

- `GET /search/by_tags?keyword=...`
- 鉴权：无需登录
- 返回：`PageResponse<ArticleResponse>`

### 4.7 标题搜索（正则）

- `GET /search/regex/by_title?pattern=...`
- 鉴权：无需登录
- 返回：`PageResponse<ArticleResponse>`

### 4.8 标签搜索（正则）

- `GET /search/regex/by_tags?pattern=...`
- 鉴权：无需登录
- 返回：`PageResponse<ArticleResponse>`

### 4.9 草稿创建

- `POST /draft`
- 鉴权：需要登录
- 请求体：`ArticleRequest`
- 返回：`ArticleResponse`

### 4.10 草稿列表

- `GET /profile/{uid}/drafts`
- 鉴权：需要登录（本人或管理员）
- 返回：`PageResponse<ArticleResponse>`

### 4.11 草稿更新

- `PUT /draft/{id}`
- 鉴权：需要登录（草稿作者或管理员）
- 请求体：`ArticleRequest`

### 4.12 草稿发布

- `POST /draft/{id}/publish`
- 鉴权：需要登录（草稿作者或管理员）

### 4.13 更新文章

- `PUT /update/{id}`
- 鉴权：需要登录
- 请求体：`ArticleRequest`

### 4.14 隐藏文章

- `PUT /hide/{id}`
- 鉴权：需要登录

### 4.15 发布文章

- `PUT /publish/{id}`
- 鉴权：需要登录

### 4.16 删除文章

- `DELETE /delete/{id}`
- 鉴权：需要登录
- 权限：管理员可删任意，普通用户只能删自己的文章

### 4.17 文章内容类型

`ArticleRequest.contentType` 支持：

- `PLAIN_TEXT`
- `MARKDOWN`
- `HTML`

后端对 `HTML` 会执行安全清洗并禁止脚本相关内容（如 script/javascript/event handler）。

---

## 5. 评论接口

前缀：`/api/comments`

### 5.1 发表评论/回复

- `POST /api/comments`
- 鉴权：需要登录
- 请求体：`CommentRequest`

```json
{
  "articleId": 1,
  "content": "写得很好！",
  "parentCommentId": null
}
```

说明：

- `parentCommentId = null`：文章下顶层评论
- `parentCommentId != null`：回复某条评论

### 5.2 文章评论列表

- `GET /api/comments/by_article/{articleId}`
- 鉴权：无需登录
- 返回：`PageResponse<CommentResponse>`

### 5.3 删除评论

- `DELETE /api/comments/{id}`
- 鉴权：需要登录
- 权限：评论作者或管理员

### 5.4 评论审核状态

评论状态值：

- `0` 待审核（PENDING）
- `1` 已通过（APPROVED）
- `2` 已驳回（REJECTED）
- `3` 已删除（DELETED）

---

## 6. 点赞/收藏接口

前缀：`/api/articles`

### 6.1 切换点赞

- `POST /{id}/like`
- 鉴权：需要登录
- 返回：`Boolean`（操作后是否为已点赞）

### 6.2 切换收藏

- `POST /{id}/favorite`
- 鉴权：需要登录
- 返回：`Boolean`（操作后是否为已收藏）

### 6.3 文章互动状态

- `GET /{id}/interactions`
- 鉴权：可匿名
- 返回：点赞数、收藏数、当前用户状态

### 6.4 用户点赞文章列表

- `GET /profile/{uid}/likes`
- 鉴权：无需登录
- 返回：`PageResponse<ArticleMetaResponse>`

### 6.5 用户收藏文章列表

- `GET /profile/{uid}/favorites`
- 鉴权：无需登录
- 返回：`PageResponse<ArticleMetaResponse>`

---

## 7. 首页聚合接口

前缀：`/api/home`

### 7.1 最新文章

- `GET /latest`
- 参数：分页
- 返回：`PageResponse<ArticleMetaResponse>`

### 7.2 热门文章

- `GET /hot?days=7`
- 参数：`days` 范围 `1~365` + 分页
- 返回：`PageResponse<ArticleMetaResponse>`

### 7.3 站点统计

- `GET /stats`
- 返回：用户数、文章数、评论数、总浏览量

### 7.4 热门标签

- `GET /hot-tags?limit=20`
- 参数：`limit` 范围 `1~100`
- 返回：标签词频 TopN

### 7.5 最新评论

- `GET /recent-comments`
- 参数：分页
- 返回：`PageResponse<CommentResponse>`

说明：首页最新评论仅返回“已通过审核”的评论。

---

## 8. 管理员接口

前缀：`/api/admin`

全部接口要求：`ROLE_ADMIN`

### 8.1 用户权限管理

- `PUT /users/{userId}/promote`：提升为管理员
- `PUT /users/{userId}/demote`：降级为普通用户

### 8.2 文章审核管理

- `GET /articles/pending`：待审核文章列表
- `PUT /articles/{id}/status`：调整文章状态
  - 请求体：`AdminArticleStatusRequest`

```json
{ "status": 1 }
```

### 8.3 评论管理

- `GET /comments?status=0`：评论列表
- `DELETE /comments/{id}`：管理员删除评论
- `PUT /comments/{id}/status`：管理员更新评论审核状态
  - 请求体：`AdminCommentStatusRequest`

```json
{ "status": 1 }
```

响应示例：

```json
{
  "statusCode": 0,
  "statusMessage": "Internal Success",
  "timeStamp": 1778001000200,
  "traceId": "f3e4b8d9-6fe0-43d6-a6b7-a654de7aa221",
  "data": {
    "id": 66,
    "articleId": 42,
    "authorId": 1002,
    "parentCommentId": null,
    "content": "审核后可见的评论",
    "createTime": "2026-05-05T10:20:00",
    "status": 1
  }
}
```

### 8.4 媒体清理

- `POST /media/cleanup?olderThanDays=7`
- 作用：清理“未被文章内容和用户头像引用”的历史图片
- 返回：本次清理删除数量
- 系统还会按定时任务自动清理（可配置）

请求示例：

```bash
curl -X POST "http://localhost:8080/api/admin/media/cleanup?olderThanDays=7" \
  -H "Authorization: Bearer <admin-accessToken>"
```

响应示例：

```json
{
  "statusCode": 0,
  "statusMessage": "Internal Success",
  "timeStamp": 1778001000300,
  "traceId": "3463cde5-b2ea-4c12-ab43-e4ea2a897c17",
  "data": 12
}
```

相关配置项（`application.properties`）：

- `app.media.cleanup-enabled`：是否启用自动清理
- `app.media.cleanup-older-than-days`：自动清理阈值天数
- `app.media.cleanup-cron`：自动清理 CRON

---

## 9. 安全探针接口（调试/测试）

- `GET /api/public/ping`：公开探针
- `GET /api/private/ping`：受保护探针（prod 需认证）

---

## 10. 附：常用请求头清单

- `Authorization: Bearer <accessToken>`
- `X-Refresh-Token: <refreshToken>`
- `X-Trace-Id: <trace-id>`

---

## 11. 媒体接口

前缀：`/api/media`

### 11.1 上传图片（文章插图）

- `POST /upload`
- 鉴权：需要登录
- 请求类型：`multipart/form-data`
- 表单字段：`file`
- 返回：`MediaUploadResponse`

请求示例：

```bash
curl -X POST "http://localhost:8080/api/media/upload" \
  -H "Authorization: Bearer <accessToken>" \
  -F "file=@cover.png"
```

响应示例：

```json
{
  "statusCode": 0,
  "statusMessage": "Internal Success",
  "timeStamp": 1778001000400,
  "traceId": "c76f9e34-4cc2-4c8b-b074-f4cd9222d53f",
  "data": {
    "id": 9001,
    "url": "/api/media/files/20260505_abc123.png",
    "originalFileName": "cover.png",
    "storedFileName": "20260505_abc123.png",
    "contentType": "image/png",
    "sizeBytes": 102400
  }
}
```

返回中的 `url` 可直接嵌入：

- 纯文本：直接展示 URL
- Markdown：`![](url)`
- HTML：`<img src="url" />`

### 11.2 访问图片

- `GET /files/{fileName}`
- 鉴权：无需登录
- 返回：图片二进制内容

---

## 12. 通知接口（审核通知）

前缀：`/api/notifications`

### 12.1 我的通知列表

- `GET /api/notifications`
- 鉴权：需要登录
- 返回：`PageResponse<NotificationResponse>`

响应示例：

```json
{
  "statusCode": 0,
  "statusMessage": "Internal Success",
  "timeStamp": 1778001000500,
  "traceId": "19b42f13-55a4-4bc6-b3a8-9f4b7a96a4c0",
  "data": {
    "content": [
      {
        "id": 3001,
        "type": "MODERATION",
        "targetType": "ARTICLE",
        "targetId": 42,
        "title": "文章审核结果",
        "message": "你的文章已审核通过并发布",
        "status": 0,
        "createTime": "2026-05-05T11:20:00",
        "readTime": null
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 20
  }
}
```

### 12.2 未读数量

- `GET /api/notifications/unread-count`
- 鉴权：需要登录
- 返回：`Long`

响应示例：

```json
{
  "statusCode": 0,
  "statusMessage": "Internal Success",
  "timeStamp": 1778001000600,
  "traceId": "14a3e08d-f795-4d79-9768-5dbc3f4ef8d2",
  "data": 3
}
```

### 12.3 标记单条已读

- `PUT /api/notifications/{id}/read`
- 鉴权：需要登录

### 12.4 全部标记已读

- `PUT /api/notifications/read-all`
- 鉴权：需要登录

---

## 13. 附：文章状态值

- `0` 草稿（DRAFT）
- `1` 已发布（PUBLISHED）
- `2` 已归档（ARCHIVED）
- `3` 已隐藏（HIDDEN）
- `4` 已删除（DELETED）
- `5` 待审核（PENDING）

