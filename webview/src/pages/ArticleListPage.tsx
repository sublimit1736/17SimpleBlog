import React, {useEffect, useState} from 'react';
import {articlesApi} from '../api/articles';
import type {Article} from '../types';
import Layout from '../components/layout/Layout';
import ArticleList from '../components/article/ArticleList';
import Pagination from '../components/ui/Pagination';
import styles from './ArticleListPage.module.css';

const ArticleListPage: React.FC = () => {
    const [articles, setArticles] = useState<Article[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    useEffect(() => {
        const fetch = async () => {
            setLoading(true);
            try {
                const res = await articlesApi.getAll(page, 10);
                if (res.data.statusCode === 0) {
                    setArticles(res.data.data.content);
                    setTotalPages(res.data.data.totalPages);
                    setTotalElements(res.data.data.totalElements);
                }
            } catch { /* silent */
            } finally {
                setLoading(false);
            }
        };
        fetch();
    }, [page]);

    return (
        <Layout>
            <div className="container">
                <div className={styles.header}>
                    <h1 className={styles.title}>📄 所有文章</h1>
                    {!loading && <span className={styles.count}>共 {totalElements} 篇</span>}
                </div>
                <ArticleList articles={articles} loading={loading}/>
                <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage}/>
            </div>
        </Layout>
    );
};

export default ArticleListPage;
