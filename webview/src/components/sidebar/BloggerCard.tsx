import React from 'react';
import Avatar from '../ui/Avatar';
import type { SiteStats } from '../../types';
import { useNavigate } from 'react-router-dom';
import styles from './BloggerCard.module.css';

interface BloggerCardProps {
  stats: SiteStats | null;
  authorName?: string;
  authorAvatarUrl?: string;
  motto?: string;
}

const BloggerCard: React.FC<BloggerCardProps> = ({
  stats,
  authorName = '博主',
  authorAvatarUrl,
  motto = '记录生活，分享技术，感谢关注 ♥',
}) => {
  const navigate = useNavigate();

  return (
    <div className={styles.card}>
      {/* Cover gradient banner */}
      <div className={styles.cover} />

      {/* Avatar overlapping cover */}
      <div className={styles.avatarWrap}>
        <Avatar src={authorAvatarUrl} username={authorName} size={72} />
      </div>

      {/* Info */}
      <div className={styles.info}>
        <span className={styles.name}>{authorName}</span>
        <p className={styles.motto}>{motto}</p>
      </div>

      {/* Stats row */}
      <div className={styles.stats}>
        <button className={styles.statItem} onClick={() => navigate('/articles')}>
          <span className={styles.statNum}>{stats?.articleCount ?? '—'}</span>
          <span className={styles.statLabel}>文章</span>
        </button>
        <div className={styles.divider} />
        <div className={styles.statItem}>
          <span className={styles.statNum}>{stats?.commentCount ?? '—'}</span>
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
