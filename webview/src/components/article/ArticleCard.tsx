import React from 'react';
import { Link } from 'react-router-dom';
import { ArticleStatus, type Article } from '../../types';
import { formatDistanceToNow } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import Avatar from '../ui/Avatar';
import Tag from '../ui/Tag';
import styles from './ArticleCard.module.css';

interface ArticleCardProps {
  article: Article;
  showStatus?: boolean;
}

const statusLabels: Record<number, { label: string; color: string }> = {
  [ArticleStatus.DRAFT]: { label: '草稿', color: '#64748b' },
  [ArticleStatus.PUBLISHED]: { label: '已发布', color: '#10b981' },
  [ArticleStatus.ARCHIVED]: { label: '已归档', color: '#6366f1' },
  [ArticleStatus.HIDDEN]: { label: '已隐藏', color: '#f59e0b' },
  [ArticleStatus.DELETED]: { label: '已删除', color: '#ef4444' },
  [ArticleStatus.PENDING]: { label: '待审核', color: '#f59e0b' },
};

const ArticleCard: React.FC<ArticleCardProps> = ({ article, showStatus = false }) => {
  const timeAgo = (() => {
    try {
      return formatDistanceToNow(new Date(article.createdAt), { addSuffix: true, locale: zhCN });
    } catch {
      return '';
    }
  })();

  const statusInfo = statusLabels[article.status];

  const excerpt = article.content.replace(/[#*`>\u005B\u005D]/g, '').substring(0, 120);

  return (
    <article className={styles.card}>
      <div className={styles.top}>
        <div className={styles.authorInfo}>
          <Avatar src={article.authorAvatarUrl} username={article.authorName} size={32} />
          <div>
            <Link to={`/profile/${article.authorId}`} className={styles.authorName}>
              {article.authorName || '未知用户'}
            </Link>
            <span className={styles.time}>{timeAgo}</span>
          </div>
        </div>
        {showStatus && statusInfo && (
          <span className={styles.status} style={{ color: statusInfo.color, borderColor: statusInfo.color }}>
            {statusInfo.label}
          </span>
        )}
      </div>

      <Link to={`/article/${article.id}`} className={styles.titleLink}>
        <h2 className={styles.title}>{article.title}</h2>
      </Link>

      {excerpt && <p className={styles.excerpt}>{excerpt}{article.content.length > 120 ? '...' : ''}</p>}

      {article.tags && article.tags.length > 0 && (
        <div className={styles.tags}>
          {article.tags.slice(0, 4).map((tag) => (
            <Tag key={tag} label={tag} linkTo={`/search?type=articles&q=${encodeURIComponent(tag)}`} />
          ))}
        </div>
      )}

      <div className={styles.footer}>
        <div className={styles.stats}>
          <span title="浏览">👁 {article.viewCount}</span>
          <span title="点赞">👍 {article.likeCount}</span>
          <span title="评论">💬 {article.commentCount}</span>
          <span title="收藏">⭐ {article.favoriteCount}</span>
        </div>
        <Link to={`/article/${article.id}`} className={styles.readMore}>
          阅读全文 →
        </Link>
      </div>
    </article>
  );
};

export default ArticleCard;
