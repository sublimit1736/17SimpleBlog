import React, { useState, useCallback } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { articlesApi } from '../api/articles';
import { authApi } from '../api/auth';
import type { Article, User } from '../types';
import Layout from '../components/layout/Layout';
import ArticleList from '../components/article/ArticleList';
import Avatar from '../components/ui/Avatar';
import styles from './SearchPage.module.css';

type SearchType = 'articles' | 'users';

const SearchPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialQ = searchParams.get('q') || '';
  const initialType = (searchParams.get('type') as SearchType) || 'articles';

  const [q, setQ] = useState(initialQ);
  const [type, setType] = useState<SearchType>(initialType);
  const [articles, setArticles] = useState<Article[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  const doSearch = useCallback(async (query: string, searchType: SearchType) => {
    if (!query.trim()) return;
    setLoading(true);
    setSearched(true);
    try {
      if (searchType === 'articles') {
        const res = await articlesApi.search(query.trim(), 0, 20);
        setArticles(res.data.statusCode === 0 ? res.data.data.content : []);
        setUsers([]);
      } else {
        const res = await authApi.searchByUsername(query.trim());
        setUsers(res.data.statusCode === 0 ? res.data.data.content : []);
        setArticles([]);
      }
    } catch { /* silent */ }
    finally { setLoading(false); }
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setSearchParams({ q, type });
    doSearch(q, type);
  };

  const switchType = (t: SearchType) => {
    setType(t);
    if (q) { setSearchParams({ q, type: t }); doSearch(q, t); }
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
                emptyText={`没有找到与"${initialQ}"相关的文章`}
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
                      <Avatar src={u.avatarUrl} username={u.username} size={48} />
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
