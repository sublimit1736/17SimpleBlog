import React from 'react';
import Avatar from '../ui/Avatar';
import type { SiteStats } from '../../types';
import { useNavigate } from 'react-router-dom';
import { useSiteConfigStore } from '../../store/siteConfig';
import { sanitizeImageUrl } from '../../utils/sanitizeImageUrl';
import styles from './BloggerCard.module.css';

interface BloggerCardProps {
  stats: SiteStats | null;
}

const BloggerCard: React.FC<BloggerCardProps> = ({ stats }) => {
  const navigate = useNavigate();
  const { bloggerName, bloggerAvatarUrl, bloggerBgUrl } = useSiteConfigStore();

  const name = bloggerName || '博主';
  const safeBgUrl = sanitizeImageUrl(bloggerBgUrl);
  const coverStyle = safeBgUrl
    ? { backgroundImage: `url('${safeBgUrl}')`, backgroundSize: 'cover', backgroundPosition: 'center' }
    : undefined;

  return (
    <div className={styles.card}>
      {/* Cover banner */}
      <div className={`${styles.cover} ${safeBgUrl ? styles.coverPhoto : ''}`} style={coverStyle} />

      {/* Avatar overlapping cover */}
      <div className={styles.avatarWrap}>
        <Avatar src={bloggerAvatarUrl || undefined} username={name} size={72} />
      </div>

      {/* Info */}
      <div className={styles.info}>
        <span className={styles.name}>{name}</span>
        <p className={styles.motto}>记录生活，分享技术，感谢关注 ♥</p>
      </div>

      {/* Stats row */}
      <div className={styles.stats}>
        <button className={styles.statItem} onClick={() => navigate('/articles')}>
          <span className={styles.statNum}>{stats?.totalArticles ?? '—'}</span>
          <span className={styles.statLabel}>文章</span>
        </button>
        <div className={styles.divider} />
        <div className={styles.statItem}>
          <span className={styles.statNum}>{stats?.totalComments ?? '—'}</span>
          <span className={styles.statLabel}>评论</span>
        </div>
        <div className={styles.divider} />
        <div className={styles.statItem}>
          <span className={styles.statNum}>{stats?.totalViews ?? '—'}</span>
          <span className={styles.statLabel}>浏览</span>
        </div>
      </div>
    </div>
  );
};

export default BloggerCard;
