import React, {useCallback, useEffect, useState} from 'react';
import {adminApi} from '../api/admin';
import type {Article, Comment, User} from '../types';
import {useToast} from '../components/ui/toastContext';
import Layout from '../components/layout/Layout';
import Button from '../components/ui/Button';
import {Loading} from '../components/ui/Loading';
import {Link} from 'react-router-dom';
import {usePageTitle} from '../hooks/usePageTitle';
import styles from './AdminPage.module.css';

type AdminTab = 'pending' | 'comments' | 'users';

const AdminPage: React.FC = () => {
    usePageTitle('管理后台');

    const [tab, setTab] = useState<AdminTab>('pending');
    const [pending, setPending] = useState<Article[]>([]);
    const [comments, setComments] = useState<Comment[]>([]);
    const [userSearch, setUserSearch] = useState('');
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(false);
    const {showToast} = useToast();

    const fetchPending = useCallback(async () => {
        setLoading(true);
        try {
            const res = await adminApi.getPendingArticles(0, 50);
            if (res.data.statusCode === 0) setPending(res.data.data.content);
        } catch { /* silent */
        } finally {
            setLoading(false);
        }
    }, []);

    const fetchComments = useCallback(async () => {
        setLoading(true);
        try {
            const res = await adminApi.getAllComments(undefined, 0, 50);
            if (res.data.statusCode === 0) setComments(res.data.data.content);
        } catch { /* silent */
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        if (tab === 'pending') fetchPending();
        if (tab === 'comments') fetchComments();
    }, [tab, fetchPending, fetchComments]);

    const approveArticle = async (articleId: number) => {
        try {
            const res = await adminApi.updateArticleStatus(articleId, 1);
            if (res.data.statusCode === 0) {
                showToast('已通过审核', 'success');
                setPending((p) => p.filter((a) => a.id !== articleId));
            } else showToast(res.data.statusMessage || '操作失败', 'error');
        } catch {
            showToast('操作失败', 'error');
        }
    };

    const rejectArticle = async (articleId: number) => {
        try {
            const res = await adminApi.updateArticleStatus(articleId, 3);
            if (res.data.statusCode === 0) {
                showToast('已拒绝', 'success');
                setPending((p) => p.filter((a) => a.id !== articleId));
            } else showToast(res.data.statusMessage || '操作失败', 'error');
        } catch {
            showToast('操作失败', 'error');
        }
    };

    const deleteComment = async (commentId: number) => {
        if (!window.confirm('确定删除这条评论?')) return;
        try {
            const res = await adminApi.deleteComment(commentId);
            if (res.data.statusCode === 0) {
                showToast('已删除', 'success');
                setComments((c) => c.filter((x) => x.id !== commentId));
            } else showToast(res.data.statusMessage || '删除失败', 'error');
        } catch {
            showToast('删除失败', 'error');
        }
    };

    const searchUsers = async () => {
        if (!userSearch.trim()) return;
        setLoading(true);
        try {
            const res = await adminApi.searchUsers(userSearch.trim());
            if (res.data.statusCode === 0) setUsers(res.data.data.content);
        } catch { /* silent */
        } finally {
            setLoading(false);
        }
    };

    const promoteUser = async (uid: number) => {
        try {
            const res = await adminApi.promoteUser(uid);
            if (res.data.statusCode === 0) {
                showToast('已提升为管理员', 'success');
                setUsers((u) => u.map((x) => x.id === uid ? {...x, role: 'ADMIN'} : x));
            }
        } catch {
            showToast('操作失败', 'error');
        }
    };

    const demoteUser = async (uid: number) => {
        try {
            const res = await adminApi.demoteUser(uid);
            if (res.data.statusCode === 0) {
                showToast('已撤销管理员', 'success');
                setUsers((u) => u.map((x) => x.id === uid ? {...x, role: 'USER'} : x));
            }
        } catch {
            showToast('操作失败', 'error');
        }
    };

    const tabs: { key: AdminTab; label: string }[] = [
        {key: 'pending', label: '⏳ 待审核'},
        {key: 'comments', label: '💬 评论管理'},
        {key: 'users', label: '👤 用户管理'},
    ];

    return (
        <Layout>
            <div className="container">
                <h1 className={styles.heading}>🛡 管理后台</h1>

                <div className={styles.tabBar}>
                    {tabs.map((t) => (
                        <button
                            key={t.key}
                            className={`${styles.tabBtn} ${tab === t.key ? styles.tabActive : ''}`}
                            onClick={() => setTab(t.key)}
                        >
                            {t.label}
                        </button>
                    ))}
                </div>

                {/* Pending articles */}
                {tab === 'pending' && (
                    loading ? <Loading center/> : pending.length === 0 ? (
                        <div className="empty-state">
                            <div className="empty-icon">✅</div>
                            <p>暂无待审核文章</p></div>
                    ) : (
                        <div className={styles.list}>
                            {pending.map((a) => (
                                <div key={a.id} className={styles.row}>
                                    <div className={styles.rowInfo}>
                                        <Link to={`/article/${a.id}`} className={styles.articleTitle}>{a.title}</Link>
                                        <span
                                            className={styles.meta}>by {a.authorName} · {new Date(a.publishedTime).toLocaleDateString()}</span>
                                    </div>
                                    <div className={styles.rowActions}>
                                        <Button size="sm" onClick={() => approveArticle(a.id)}>通过</Button>
                                        <Button size="sm" variant="danger"
                                                onClick={() => rejectArticle(a.id)}>拒绝</Button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )
                )}

                {/* Comments */}
                {tab === 'comments' && (
                    loading ? <Loading center/> : comments.length === 0 ? (
                        <div className="empty-state">
                            <div className="empty-icon">💬</div>
                            <p>暂无评论</p></div>
                    ) : (
                        <div className={styles.list}>
                            {comments.map((c) => (
                                <div key={c.id} className={styles.row}>
                                    <div className={styles.rowInfo}>
                                        <span className={styles.commentContent}>{c.content}</span>
                                        <span className={styles.meta}>by {c.authorName} · <Link
                                            to={`/article/${c.articleId}`}>查看文章</Link></span>
                                    </div>
                                    <Button size="sm" variant="danger" onClick={() => deleteComment(c.id)}>删除</Button>
                                </div>
                            ))}
                        </div>
                    )
                )}

                {/* User management */}
                {tab === 'users' && (
                    <div>
                        <div className={styles.userSearch}>
                            <input
                                className={styles.searchInput}
                                placeholder="搜索用户名..."
                                value={userSearch}
                                onChange={(e) => setUserSearch(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && searchUsers()}
                            />
                            <Button onClick={searchUsers} loading={loading}>搜索</Button>
                        </div>
                        {users.length > 0 && (
                            <div className={styles.list}>
                                {users.map((u) => (
                                    <div key={u.id} className={styles.row}>
                                        <div className={styles.rowInfo}>
                                            <Link to={`/profile/${u.id}`}
                                                  className={styles.articleTitle}>{u.username}</Link>
                                            <span className={styles.meta}>UID: {u.id} · {u.role}</span>
                                        </div>
                                        <div className={styles.rowActions}>
                                            {u.role === 'ADMIN'
                                                ? <Button size="sm" variant="secondary"
                                                          onClick={() => demoteUser(u.id)}>撤销管理员</Button>
                                                :
                                                <Button size="sm" onClick={() => promoteUser(u.id)}>设为管理员</Button>
                                            }
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </Layout>
    );
};

export default AdminPage;
