import React, {useCallback, useEffect, useState} from 'react';
import {Link, useNavigate, useParams} from 'react-router-dom';
import {articlesApi} from '../api/articles';
import {commentsApi} from '../api/comments';
import type {Article, Comment} from '../types';
import {useAuthStore} from '../store/auth';
import {useToast} from '../components/ui/toastContext';
import Layout from '../components/layout/Layout';
import MarkdownRenderer from '../components/article/MarkdownRenderer';
import Avatar from '../components/ui/Avatar';
import Button from '../components/ui/Button';
import {Loading} from '../components/ui/Loading';
import {format, formatDistanceToNow} from 'date-fns';
import {zhCN} from 'date-fns/locale';
import styles from './ArticleDetailPage.module.css';

const ArticleDetailPage: React.FC = () => {
    const {id} = useParams<{ id: string }>();
    const navigate = useNavigate();
    const {user, isAuthenticated} = useAuthStore();
    const {showToast} = useToast();

    const [article, setArticle] = useState<Article | null>(null);
    const [loading, setLoading] = useState(true);
    const [liked, setLiked] = useState(false);
    const [favorited, setFavorited] = useState(false);
    const [likeCount, setLikeCount] = useState(0);
    const [favoriteCount, setFavoriteCount] = useState(0);
    const [comments, setComments] = useState<Comment[]>([]);
    const [commentContent, setCommentContent] = useState('');
    const [replyTo, setReplyTo] = useState<Comment | null>(null);
    const [submitting, setSubmitting] = useState(false);

    const articleId = Number(id);

    const fetchArticle = useCallback(async () => {
        if (!id) return;
        setLoading(true);
        try {
            const res = await articlesApi.getById(articleId);
            if (res.data.statusCode === 0) {
                setArticle(res.data.data);
            } else {
                showToast('文章不存在或已删除', 'error');
                navigate('/');
            }
        } catch {
            showToast('加载失败', 'error');
        } finally {
            setLoading(false);
        }
    }, [articleId, navigate, showToast, id]);

    const fetchInteractions = useCallback(async () => {
        if (!id || !isAuthenticated) return;
        try {
            const res = await articlesApi.getInteractions(articleId);
            if (res.data.statusCode === 0) {
                const d = res.data.data;
                setLiked(d.likedByCurrentUser);
                setFavorited(d.favoritedByCurrentUser);
                setLikeCount(d.likeCount);
                setFavoriteCount(d.favoriteCount);
            }
        } catch { /* silent */
        }
    }, [articleId, isAuthenticated, id]);

    const fetchComments = useCallback(async () => {
        if (!id) return;
        try {
            const res = await commentsApi.getByArticle(articleId, 0, 50);
            if (res.data.statusCode === 0) {
                setComments(res.data.data.content);
            }
        } catch { /* silent */
        }
    }, [articleId, id]);

    useEffect(() => {
        fetchArticle();
        fetchInteractions();
        fetchComments();
    }, [fetchArticle, fetchInteractions, fetchComments]);

    useEffect(() => {
        if (article) {
            setLikeCount(0);
            setFavoriteCount(0);
        }
    }, [article]);

    const handleLike = async () => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }
        try {
            const res = await articlesApi.toggleLike(articleId);
            if (res.data.statusCode === 0) {
                const isNowLiked = res.data.data;
                setLiked(isNowLiked);
                setLikeCount((c) => c + (isNowLiked ? 1 : -1));
                showToast(isNowLiked ? '已点赞' : '已取消点赞', 'success');
            }
        } catch {
            showToast('操作失败', 'error');
        }
    };

    const handleFavorite = async () => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }
        try {
            const res = await articlesApi.toggleFavorite(articleId);
            if (res.data.statusCode === 0) {
                const isNowFav = res.data.data;
                setFavorited(isNowFav);
                setFavoriteCount((c) => c + (isNowFav ? 1 : -1));
                showToast(isNowFav ? '已收藏' : '已取消收藏', 'success');
            }
        } catch {
            showToast('操作失败', 'error');
        }
    };

    const handleComment = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }
        if (!commentContent.trim()) {
            showToast('请输入评论内容', 'warning');
            return;
        }
        setSubmitting(true);
        try {
            const res = await commentsApi.create({
                articleId,
                content: commentContent.trim(),
                parentCommentId: replyTo?.id,
            });
            if (res.data.statusCode === 0) {
                showToast('评论成功', 'success');
                setCommentContent('');
                setReplyTo(null);
                fetchComments();
            } else {
                showToast(res.data.statusMessage || '评论失败', 'error');
            }
        } catch {
            showToast('评论失败', 'error');
        } finally {
            setSubmitting(false);
        }
    };

    const handleDeleteComment = async (commentId: number) => {
        if (!window.confirm('确定要删除这条评论吗？')) return;
        try {
            const res = await commentsApi.delete(commentId);
            if (res.data.statusCode === 0) {
                showToast('删除成功', 'success');
                fetchComments();
            }
        } catch {
            showToast('删除失败', 'error');
        }
    };

    const handleDeleteArticle = async () => {
        if (!article) return;
        if (!window.confirm('确定要删除这篇文章吗？此操作不可撤销。')) return;
        try {
            const res = await articlesApi.delete(article.id);
            if (res.data.statusCode === 0) {
                showToast('文章已删除', 'success');
                navigate('/');
            }
        } catch {
            showToast('删除失败', 'error');
        }
    };

    if (loading) {
        return (
            <Layout>
                <div className="container">
                    <Loading center text="加载中..."/>
                </div>
            </Layout>
        );
    }

    if (!article) return null;

    const isAuthor = user?.id === article.authorId;
    const isAdmin = user?.role === 'ADMIN';
    const canEdit = isAuthor || isAdmin;

    const timeStr = (() => {
        try {
            return format(new Date(article.publishedTime), 'yyyy年MM月dd日 HH:mm');
        } catch {
            return '';
        }
    })();

    const buildCommentTree = (comments: Comment[]): Comment[] => {
        const map = new Map<number, Comment>();
        const roots: Comment[] = [];
        comments.forEach((c) => map.set(c.id, {...c, children: []}));
        map.forEach((c) => {
            if (c.parentCommentId && map.has(c.parentCommentId)) {
                map.get(c.parentCommentId)!.children!.push(c);
            } else {
                roots.push(c);
            }
        });
        return roots;
    };

    const commentTree = buildCommentTree(comments);

    const CommentItem = ({comment, depth = 0}: { comment: Comment; depth?: number }) => {
        const timeAgo = (() => {
            try {
                return formatDistanceToNow(new Date(comment.createTime), {addSuffix: true, locale: zhCN});
            } catch {
                return '';
            }
        })();
        const canDelete = user?.id === comment.authorId || isAdmin;

        return (
            <div className={`${styles.comment} ${depth > 0 ? styles.commentReply : ''}`}>
                <Avatar src={comment.authorAvatarUrl} username={comment.authorName} size={32}/>
                <div className={styles.commentBody}>
                    <div className={styles.commentHeader}>
                        <Link to={`/profile/${comment.authorId}`} className={styles.commenterName}>
                            {comment.authorName || '用户'}
                        </Link>
                        <span className={styles.commentTime}>{timeAgo}</span>
                    </div>
                    <p className={styles.commentText}>{comment.content}</p>
                    <div className={styles.commentActions}>
                        {isAuthenticated && (
                            <button
                                className={styles.replyBtn}
                                onClick={() => setReplyTo(comment)}
                            >
                                回复
                            </button>
                        )}
                        {canDelete && (
                            <button
                                className={styles.deleteCommentBtn}
                                onClick={() => handleDeleteComment(comment.id)}
                            >
                                删除
                            </button>
                        )}
                    </div>
                    {comment.children?.map((child) => (
                        <CommentItem key={child.id} comment={child} depth={depth + 1}/>
                    ))}
                </div>
            </div>
        );
    };

    return (
        <Layout>
            {/* Full-width article hero */}
            <div className={styles.articleHero}>
                <div className={styles.heroOverlay} />
                <div className={styles.heroContent}>
                    {article.tags && (
                        <div className={styles.heroTags}>
                            {article.tags.split(',').map((t) => t.trim()).filter(Boolean).map((tag) => (
                                <Link
                                    key={tag}
                                    to={`/search?type=articles&q=${encodeURIComponent(tag)}`}
                                    className={styles.heroTag}
                                >
                                    #{tag}
                                </Link>
                            ))}
                        </div>
                    )}
                    <h1 className={styles.heroTitle}>{article.title}</h1>
                    <div className={styles.heroMeta}>
                        <Avatar src={article.authorAvatarUrl} username={article.authorName} size={28} />
                        <Link to={`/profile/${article.authorId}`} className={styles.heroAuthor}>
                            {article.authorName || '未知用户'}
                        </Link>
                        <span className={styles.heroMetaSep}>·</span>
                        <span>📅 {timeStr}</span>
                        <span className={styles.heroMetaSep}>·</span>
                        <span>👁 {article.viewCount}</span>
                    </div>
                    {canEdit && (
                        <div className={styles.heroActions}>
                            <button
                                className={styles.heroActionBtn}
                                onClick={() => navigate(`/edit/${article.id}`)}
                            >
                                ✏ 编辑
                            </button>
                            <button
                                className={`${styles.heroActionBtn} ${styles.heroActionDanger}`}
                                onClick={handleDeleteArticle}
                            >
                                🗑 删除
                            </button>
                        </div>
                    )}
                </div>
            </div>

            <div className="container">
                <article className={styles.article}>
                    {/* Content */}
                    <div className={styles.content}>
                        <MarkdownRenderer content={article.content ?? ''} contentType={article.contentType}/>
                    </div>

                    {/* Interaction bar */}
                    <div className={styles.interactions}>
                        <button
                            className={`${styles.interBtn} ${liked ? styles.liked : ''}`}
                            onClick={handleLike}
                        >
                            <span>{liked ? '👍' : '👍'}</span>
                            <span>{likeCount}</span>
                            <span>{liked ? '已点赞' : '点赞'}</span>
                        </button>
                        <button
                            className={`${styles.interBtn} ${favorited ? styles.favorited : ''}`}
                            onClick={handleFavorite}
                        >
                            <span>{favorited ? '⭐' : '☆'}</span>
                            <span>{favoriteCount}</span>
                            <span>{favorited ? '已收藏' : '收藏'}</span>
                        </button>
                    </div>

                    {/* Comments */}
                    <div className={styles.commentsSection}>
                        <h2 className={styles.commentsTitle}>💬 评论 ({comments.length})</h2>

                        {isAuthenticated ? (
                            <form onSubmit={handleComment} className={styles.commentForm}>
                                {replyTo && (
                                    <div className={styles.replyBanner}>
                                        回复 <strong>@{replyTo.authorName}</strong>：「{replyTo.content.substring(0, 40)}」
                                        <button
                                            type="button"
                                            onClick={() => setReplyTo(null)}
                                            className={styles.cancelReply}
                                        >
                                            ✕
                                        </button>
                                    </div>
                                )}
                                <textarea
                                    className={styles.commentInput}
                                    value={commentContent}
                                    onChange={(e) => setCommentContent(e.target.value)}
                                    placeholder="写下你的评论..."
                                    rows={3}
                                />
                                <div className={styles.commentSubmit}>
                                    <Button type="submit" loading={submitting} size="sm">
                                        发表评论
                                    </Button>
                                </div>
                            </form>
                        ) : (
                            <div className={styles.loginPrompt}>
                                <Link to="/login">登录</Link> 后才能发表评论
                            </div>
                        )}

                        {commentTree.length === 0 ? (
                            <p className={styles.noComments}>暂无评论，快来发表第一条评论吧！</p>
                        ) : (
                            <div className={styles.commentList}>
                                {commentTree.map((c) => (
                                    <CommentItem key={c.id} comment={c}/>
                                ))}
                            </div>
                        )}
                    </div>
                </article>
            </div>
        </Layout>
    );
};

export default ArticleDetailPage;
