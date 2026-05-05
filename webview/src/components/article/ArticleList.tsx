import React from 'react';
import type { Article } from '../../types';
import ArticleCard from './ArticleCard';
import { SkeletonCard } from '../ui/Loading';
import styles from './ArticleList.module.css';

interface ArticleListProps {
  articles: Article[];
  loading?: boolean;
  showStatus?: boolean;
  emptyText?: string;
}

const ArticleList: React.FC<ArticleListProps> = ({
  articles,
  loading = false,
  showStatus = false,
  emptyText = '暂无文章',
}) => {
  if (loading) {
    return (
      <div className={styles.list}>
        {[1, 2, 3].map((i) => <SkeletonCard key={i} />)}
      </div>
    );
  }

  if (!articles.length) {
    return (
      <div className="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
          <path strokeLinecap="round" strokeLinejoin="round"
            d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
        </svg>
        <h3>{emptyText}</h3>
        <p>快来创作你的第一篇文章吧！</p>
      </div>
    );
  }

  return (
    <div className={styles.list}>
      {articles.map((article) => (
        <ArticleCard key={article.id} article={article} showStatus={showStatus} />
      ))}
    </div>
  );
};

export default ArticleList;
