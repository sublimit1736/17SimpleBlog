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
  index?: number;
}

const statusLabels: Record<number, { label: string; color: string }> = {
  [ArticleStatus.DRAFT]: { label: '草稿', color: '#64748b' },
  [ArticleStatus.PUBLISHED]: { label: '已发布', color: '#10b981' },
  [ArticleStatus.ARCHIVED]: { label: '已归档', color: '#6366f1' },
  [ArticleStatus.HIDDEN]: { label: '已隐藏', color: '#f59e0b' },
  [ArticleStatus.DELETED]: { label: '已删除', color: '#ef4444' },
  [ArticleStatus.PENDING]: { label: '待审核', color: '#f59e0b' },
};

const COVER_GRADIENTS = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
  'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
];

const ArticleCard: React.FC<ArticleCardProps> = ({ article, showStatus = false, index = 0 }) => {
  const timeAgo = (() => {
    try {
      return formatDistanceToNow(new Date(article.publishedTime), { addSuffix: true, locale: zhCN });
    } catch {
      return '';
    }
  })();

  const statusInfo = statusLabels[article.status];

  // Use preview if available, otherwise fall back to stripping content markdown
  const excerpt = article.preview
    || (article.content
        // eslint-disable-next-line no-useless-escape
        ? article.content.replace(/[#*`>\[\]!]/g, '').replace(/\n+/g, ' ').trim().substring(0, 150)
        : '');

  const coverGradient = COVER_GRADIENTS[index % COVER_GRADIENTS.length];
  const imageRight = index % 2 !== 0;

  // Parse tags from comma-separated string
  const tagList = article.tags
    ? article.tags.split(',').map((t) => t.trim()).filter(Boolean)
    : [];

  return (
    <article className={`${styles.card} ${imageRight ? styles.imageRight : ''}`}>
      {/* Cover image side */}
      <Link to={`/article/${article.id}`} className={styles.cover} style={{ background: coverGradient }}>
        <span className={styles.coverIcon}>✦</span>
      </Link>

      {/* Info side */}
      <div className={styles.info}>
        <div className={styles.top}>
          <div className={styles.authorInfo}>
            <Avatar src={article.authorAvatarUrl} username={article.authorName} size={30} />
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

        {excerpt && (
          <p className={styles.excerpt}>
            {excerpt}{excerpt.length >= 150 ? '...' : ''}
          </p>
        )}

        {tagList.length > 0 && (
          <div className={styles.tags}>
            {tagList.slice(0, 4).map((tag) => (
              <Tag key={tag} label={tag} linkTo={`/search?type=articles&q=${encodeURIComponent(tag)}`} />
            ))}
          </div>
        )}

        <div className={styles.footer}>
          <div className={styles.stats}>
            <span title="浏览">👁 {article.viewCount}</span>
          </div>
          <Link to={`/article/${article.id}`} className={styles.readMore}>
            阅读全文 →
          </Link>
        </div>
      </div>
    </article>
  );
};

export default ArticleCard;
