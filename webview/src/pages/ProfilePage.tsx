import React, {useCallback, useEffect, useState} from 'react';
import {useParams} from 'react-router-dom';
import {authApi} from '../api/auth';
import {articlesApi} from '../api/articles';
import type {Article, User} from '../types';
import {useAuthStore} from '../store/auth';
import {useToast} from '../components/ui/toastContext';
import Layout from '../components/layout/Layout';
import ArticleList from '../components/article/ArticleList';
import Pagination from '../components/ui/Pagination';
import Avatar from '../components/ui/Avatar';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import {Loading} from '../components/ui/Loading';
import {usePageTitle} from '../hooks/usePageTitle';
import styles from './ProfilePage.module.css';

type ProfileTab = 'articles' | 'likes' | 'favorites';

const ProfilePage: React.FC = () => {
    usePageTitle('个人主页');

    const {uid} = useParams<{ uid: string }>();
    const userId = Number(uid);
    const {user: currentUser, isAuthenticated, setUser} = useAuthStore();
    const {showToast} = useToast();
    const isSelf = isAuthenticated && currentUser?.id === userId;

    const [profile, setProfile] = useState<User | null>(null);
    const [loadingProfile, setLoadingProfile] = useState(true);
    const [tab, setTab] = useState<ProfileTab>('articles');
    const [articles, setArticles] = useState<Article[]>([]);
    const [loadingArticles, setLoadingArticles] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const [editModal, setEditModal] = useState(false);
    const [pwModal, setPwModal] = useState(false);
    const [newUsername, setNewUsername] = useState('');
    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [saving, setSaving] = useState(false);

    const fetchProfile = useCallback(async () => {
        setLoadingProfile(true);
        try {
            const res = await authApi.getProfile(userId);
            if (res.data.statusCode === 0) setProfile(res.data.data);
        } catch { /* silent */
        } finally {
            setLoadingProfile(false);
        }
    }, [userId]);

    const fetchArticles = useCallback(async () => {
        setLoadingArticles(true);
        try {
            let res;
            if (tab === 'articles') res = await articlesApi.getByAuthor(userId, page, 10);
            else if (tab === 'likes') res = await articlesApi.getLikedArticles(userId, page, 10);
            else if (tab === 'favorites') res = await articlesApi.getFavoritedArticles(userId, page, 10);

            if (res && res.data.statusCode === 0) {
                setArticles(res.data.data.content);
                setTotalPages(res.data.data.totalPages);
            }
        } catch { /* silent */
        } finally {
            setLoadingArticles(false);
        }
    }, [userId, tab, page]);

    // eslint-disable-next-line react-hooks/set-state-in-effect
    useEffect(() => {
        fetchProfile();
    }, [fetchProfile]);
    // eslint-disable-next-line react-hooks/set-state-in-effect
    useEffect(() => {
        fetchArticles();
    }, [fetchArticles]);

    const handleUpdateUsername = async () => {
        if (!newUsername.trim()) {
            showToast('请输入新用户名', 'warning');
            return;
        }
        setSaving(true);
        try {
            const res = await authApi.updateProfile(userId, newUsername.trim());
            if (res.data.statusCode === 0) {
                setProfile(res.data.data);
                if (isSelf) setUser(res.data.data);
                showToast('修改成功', 'success');
                setEditModal(false);
            } else {
                showToast(res.data.statusMessage || '修改失败', 'error');
            }
        } catch {
            showToast('修改失败', 'error');
        } finally {
            setSaving(false);
        }
    };

    const handleChangePassword = async () => {
        if (!oldPassword || !newPassword) {
            showToast('请填写密码', 'warning');
            return;
        }
        if (newPassword.length < 6) {
            showToast('新密码至少6位', 'warning');
            return;
        }
        setSaving(true);
        try {
            const res = await authApi.changePassword(oldPassword, newPassword);
            if (res.data.statusCode === 0) {
                showToast('密码修改成功', 'success');
                setPwModal(false);
                setOldPassword('');
                setNewPassword('');
            } else {
                showToast(res.data.statusMessage || '修改失败', 'error');
            }
        } catch {
            showToast('修改失败', 'error');
        } finally {
            setSaving(false);
        }
    };

    const handleAvatarChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        try {
            const res = await authApi.updateAvatar(userId, file);
            if (res.data.statusCode === 0) {
                setProfile(res.data.data);
                if (isSelf) setUser(res.data.data);
                showToast('头像更新成功', 'success');
            } else {
                showToast(res.data.statusMessage || '上传失败', 'error');
            }
        } catch {
            showToast('上传失败', 'error');
        }
    };

    if (loadingProfile) {
        return <Layout><Loading center text="加载中..."/></Layout>;
    }

    if (!profile) {
        return <Layout>
            <div className="container"><p>用户不存在</p></div>
        </Layout>;
    }

    const tabs: { key: ProfileTab; label: string; show: boolean }[] = [
        {key: 'articles', label: '文章', show: true},
        {key: 'likes', label: '点赞', show: isSelf},
        {key: 'favorites', label: '收藏', show: isSelf},
    ];

    return (
        <Layout>
            <div className="container">
                {/* Hero banner */}
                <div className={styles.profileHero}>
                    <div className={styles.profileHeroOverlay}/>
                </div>

                <div className={styles.profileHeader}>
                    <div className={styles.avatarWrap}>
                        <Avatar src={profile.avatarUrl} username={profile.username} size={96}/>
                        {isSelf && (
                            <label className={styles.avatarEdit} title="更换头像">
                                📷
                                <input type="file" accept="image/*" onChange={handleAvatarChange} hidden/>
                            </label>
                        )}
                    </div>
                    <div className={styles.profileInfo}>
                        <div className={styles.usernameRow}>
                            <h1 className={styles.username}>{profile.username}</h1>
                            <span className={styles.roleTag}>
                {profile.role === 'ADMIN' ? '🛡 管理员' : '👤 用户'}
              </span>
                        </div>
                        <p className={styles.userId}>UID: {profile.id}</p>
                        {isSelf && (
                            <div className={styles.editBtns}>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => {
                                        setNewUsername(profile.username);
                                        setEditModal(true);
                                    }}
                                >
                                    ✏ 修改用户名
                                </Button>
                                <Button
                                    variant="secondary"
                                    size="sm"
                                    onClick={() => setPwModal(true)}
                                >
                                    🔑 修改密码
                                </Button>
                            </div>
                        )}
                    </div>
                </div>

                <div className={styles.tabBar}>
                    {tabs.filter((t) => t.show).map((t) => (
                        <button
                            key={t.key}
                            className={`${styles.tabBtn} ${tab === t.key ? styles.tabActive : ''}`}
                            onClick={() => {
                                setTab(t.key);
                                setPage(0);
                            }}
                        >
                            {t.label}
                        </button>
                    ))}
                </div>

                <ArticleList
                    articles={articles}
                    loading={loadingArticles}
                    showStatus={false}
                    emptyText={
                        tab === 'articles' ? '还没有发布文章' :
                            tab === 'likes' ? '还没有点赞的文章' :
                                '还没有收藏的文章'
                    }
                />
                <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage}/>
            </div>

            {/* Edit username modal */}
            <Modal open={editModal} onClose={() => setEditModal(false)} title="修改用户名">
                <div className={styles.modalForm}>
                    <label className={styles.modalLabel}>新用户名</label>
                    <input
                        className={styles.modalInput}
                        value={newUsername}
                        onChange={(e) => setNewUsername(e.target.value)}
                        placeholder="请输入新用户名"
                    />
                    <div className={styles.modalActions}>
                        <Button variant="secondary" onClick={() => setEditModal(false)}>取消</Button>
                        <Button loading={saving} onClick={handleUpdateUsername}>保存</Button>
                    </div>
                </div>
            </Modal>

            {/* Change password modal */}
            <Modal open={pwModal} onClose={() => setPwModal(false)} title="修改密码">
                <div className={styles.modalForm}>
                    <label className={styles.modalLabel}>当前密码</label>
                    <input
                        className={styles.modalInput}
                        type="password"
                        value={oldPassword}
                        onChange={(e) => setOldPassword(e.target.value)}
                        placeholder="请输入当前密码"
                    />
                    <label className={styles.modalLabel}>新密码</label>
                    <input
                        className={styles.modalInput}
                        type="password"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        placeholder="至少6位新密码"
                    />
                    <div className={styles.modalActions}>
                        <Button variant="secondary" onClick={() => setPwModal(false)}>取消</Button>
                        <Button loading={saving} onClick={handleChangePassword}>保存</Button>
                    </div>
                </div>
            </Modal>
        </Layout>
    );
};

export default ProfilePage;
