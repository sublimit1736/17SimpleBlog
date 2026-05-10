import React, {useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import {homeApi} from '../api/home';
import type {Article, Comment, HotTag, SiteStats} from '../types';
import Layout from '../components/layout/Layout';
import HeroBanner from '../components/layout/HeroBanner';
import ArticleList from '../components/article/ArticleList';
import Pagination from '../components/ui/Pagination';
import Tag from '../components/ui/Tag';
import Avatar from '../components/ui/Avatar';
import BloggerCard from '../components/sidebar/BloggerCard';
import SiteInfoCard from '../components/sidebar/SiteInfoCard';
import {formatDistanceToNow} from 'date-fns';
import {zhCN} from 'date-fns/locale';
import {usePageTitle} from '../hooks/usePageTitle';
import styles from './HomePage.module.css';

type SortMode = 'latest' | 'hot';

const HomePage: React.FC = () => {
    usePageTitle('首页');

    const [sort, setSort] = useState<SortMode>('latest');
    const [articles, setArticles] = useState<Article[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [stats, setStats] = useState<SiteStats | null>(null);
    const [hotTags, setHotTags] = useState<HotTag[]>([]);
    const [recentComments, setRecentComments] = useState<Comment[]>([]);

    useEffect(() => {
        const fetchSidebar = async () => {
            try {
                const [statsRes, tagsRes, commentsRes] = await Promise.all([
                    homeApi.getStats(),
                    homeApi.getHotTags(20),
                    homeApi.getRecentComments(0, 5),
                ]);
                if (statsRes.data.statusCode === 0) setStats(statsRes.data.data);
                if (tagsRes.data.statusCode === 0) setHotTags(tagsRes.data.data);
                if (commentsRes.data.statusCode === 0) setRecentComments(commentsRes.data.data.content);
            } catch { /* silent */
            }
        };
        fetchSidebar();
    }, []);

    useEffect(() => {
        const fetchArticles = async () => {
            setLoading(true);
            try {
                const res = sort === 'latest'
                    ? await homeApi.getLatest(page, 10)
                    : await homeApi.getHot(7, page, 10);
                if (res.data.statusCode === 0) {
                    setArticles(res.data.data.content);
                    setTotalPages(res.data.data.totalPages);
                }
            } catch { /* silent */
            } finally {
                setLoading(false);
            }
        };
        fetchArticles();
    }, [sort, page]);

    const handleSortToggle = () => {
        setSort((prev) => (prev === 'latest' ? 'hot' : 'latest'));
        setPage(0);
    };

    return (
        <Layout>
            <HeroBanner />
            <div className="container">
                <div className="page-layout">
                    <div className="page-main">
                        <div className={styles.articleBoxHeader}>
                            <span className={styles.articleBoxTitle}>
                                {sort === 'latest' ? '最新文章' : '热门文章'}
                            </span>
                            <button
                                className={styles.sortToggleBtn}
                                onClick={handleSortToggle}
                                title={sort === 'latest' ? '切换为热度排序' : '切换为时间排序'}
                            >
                                {sort === 'latest' ? (
                                    <>
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="13" height="13">
                                            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
                                            <polyline points="17 6 23 6 23 12" />
                                        </svg>
                                        热度
                                    </>
                                ) : (
                                    <>
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="13" height="13">
                                            <circle cx="12" cy="12" r="10" />
                                            <polyline points="12 6 12 12 16 14" />
                                        </svg>
                                        最新
                                    </>
                                )}
                            </button>
                        </div>

                        <ArticleList articles={articles} loading={loading}/>
                        <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage}/>
                    </div>

                    <aside className="page-sidebar">
                        {/* Blogger Info Card */}
                        <BloggerCard stats={stats} />

                        {/* Site Info Card */}
                        <SiteInfoCard stats={stats} />

                        {/* Hot Tags */}
                        {hotTags.length > 0 && (
                            <div className="card">
                                <h3 className={styles.sideTitle}>热门标签</h3>
                                <div className={styles.tagCloud}>
                                    {hotTags.map((ht) => (
                                        <Tag
                                            key={ht.tag}
                                            label={`${ht.tag} (${ht.count})`}
                                            linkTo={`/search?type=articles&q=${encodeURIComponent(ht.tag)}`}
                                        />
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* Recent Comments */}
                        {recentComments.length > 0 && (
                            <div className="card">
                                <h3 className={styles.sideTitle}>最新评论</h3>
                                <div className={styles.commentList}>
                                    {recentComments.map((c) => (
                                        <div key={c.id} className={styles.commentItem}>
                                            <div className={styles.commentAuthor}>
                                                <Avatar src={c.authorAvatarUrl} username={c.authorName} size={24}/>
                                                <Link to={`/profile/${c.authorId}`} className={styles.commentName}>
                                                    {c.authorName || '用户'}
                                                </Link>
                                                <span className={styles.commentTime}>
                          {(() => {
                              try {
                                  return formatDistanceToNow(new Date(c.createTime), {addSuffix: true, locale: zhCN});
                              } catch {
                                  return '';
                              }
                          })()}
                        </span>
                                            </div>
                                            <Link to={`/article/${c.articleId}`} className={styles.commentContent}>
                                                {c.content.substring(0, 60)}{c.content.length > 60 ? '...' : ''}
                                            </Link>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </aside>
                </div>
            </div>
        </Layout>
    );
};

export default HomePage;
