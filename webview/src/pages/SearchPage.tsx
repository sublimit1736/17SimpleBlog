import React, {useCallback, useEffect, useState} from 'react';
import {Link, useSearchParams} from 'react-router-dom';
import {articlesApi} from '../api/articles';
import {authApi} from '../api/auth';
import type {Article, User} from '../types';
import Layout from '../components/layout/Layout';
import ArticleList from '../components/article/ArticleList';
import Avatar from '../components/ui/Avatar';
import styles from './SearchPage.module.css';

type SearchType = 'articles' | 'users';

const SearchPage: React.FC = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const queryParam = searchParams.get('q') || '';
    const typeParam = (searchParams.get('type') as SearchType) || 'articles';

    const [q, setQ] = useState(queryParam);
    const [type, setType] = useState<SearchType>(typeParam);
    const [articles, setArticles] = useState<Article[]>([]);
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(false);
    const [searched, setSearched] = useState(false);

    const doSearch = useCallback(async (query: string, searchType: SearchType) => {
        const trimmed = query.trim();
        if (!trimmed) {
            setSearched(false);
            setArticles([]);
            setUsers([]);
            return;
        }
        setLoading(true);
        setSearched(true);
        try {
            if (searchType === 'articles') {
                const res = await articlesApi.searchByTitle(trimmed, 0, 20);
                setArticles(res.data.statusCode === 0 ? res.data.data.content : []);
                setUsers([]);
            } else {
                const res = await authApi.searchByUsername(trimmed);
                setUsers(res.data.statusCode === 0 ? res.data.data.content : []);
                setArticles([]);
            }
        } catch { /* silent */
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        setQ(queryParam);
        setType(typeParam);
        doSearch(queryParam, typeParam);
    }, [queryParam, typeParam, doSearch]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        const trimmed = q.trim();
        if (!trimmed) {
            setSearchParams({type});
            return;
        }
        setSearchParams({q: trimmed, type});
    };

    const switchType = (t: SearchType) => {
        setType(t);
        const trimmed = q.trim();
        if (trimmed) {
            setSearchParams({q: trimmed, type: t});
        } else {
            setSearchParams({type: t});
        }
    };

    return (
        <Layout>
            <div className="container">
                <form className={styles.searchBar} onSubmit={handleSearch}>
                    <input
                        className={styles.searchInput}
                        placeholder="搜索..."
                        value={q}
                        onChange={(e) => setQ(e.target.value)}
                        autoFocus
                    />
                    <button className={styles.searchBtn} type="submit">🔍 搜索</button>
                </form>

                <div className={styles.typeSwitch}>
                    <button
                        className={`${styles.typeBtn} ${type === 'articles' ? styles.typeActive : ''}`}
                        onClick={() => switchType('articles')}
                    >
                        📄 文章
                    </button>
                    <button
                        className={`${styles.typeBtn} ${type === 'users' ? styles.typeActive : ''}`}
                        onClick={() => switchType('users')}
                    >
                        👤 用户
                    </button>
                </div>

                {searched && (
                    <>
                        {type === 'articles' && (
                            <ArticleList
                                articles={articles}
                                loading={loading}
                                emptyText={`没有找到与"${queryParam}"相关的文章`}
                            />
                        )}
                        {type === 'users' && !loading && (
                            <div className={styles.userList}>
                                {users.length === 0 ? (
                                    <div className="empty-state">
                                        <div className="empty-icon">👤</div>
                                        <p>没有找到用户</p>
                                    </div>
                                ) : (
                                    users.map((u) => (
                                        <Link key={u.id} to={`/profile/${u.id}`} className={styles.userCard}>
                                            <Avatar src={u.avatarUrl} username={u.username} size={48}/>
                                            <div>
                                                <div className={styles.userUsername}>{u.username}</div>
                                                <div className={styles.userRole}>
                                                    {u.role === 'ADMIN' ? '🛡 管理员' : '👤 普通用户'}
                                                </div>
                                            </div>
                                        </Link>
                                    ))
                                )}
                            </div>
                        )}
                    </>
                )}

                {!searched && (
                    <div className="empty-state">
                        <div className="empty-icon">🔍</div>
                        <p>输入关键词开始搜索</p>
                    </div>
                )}
            </div>
        </Layout>
    );
};

export default SearchPage;
