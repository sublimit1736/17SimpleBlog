import React, { useMemo } from 'react';
import type { SiteStats } from '../../types';
import styles from './SiteInfoCard.module.css';

interface SiteInfoCardProps {
  stats: SiteStats | null;
  startDate?: string; // ISO date string for "site launched" date
}

const SiteInfoCard: React.FC<SiteInfoCardProps> = ({
  stats,
  startDate = '2024-01-01',
}) => {
  const runtimeDays = useMemo(() => {
    try {
      const diff = Date.now() - new Date(startDate).getTime();
      return Math.floor(diff / 86_400_000);
    } catch {
      return 0;
    }
  }, [startDate]);

  const rows: { label: string; value: string | number }[] = [
    { label: '文章数目', value: stats?.articleCount ?? '—' },
    { label: '运行时间', value: `${runtimeDays} 天` },
    { label: '评论总数', value: stats?.commentCount ?? '—' },
    { label: '访问次数', value: stats?.totalViews ?? '—' },
  ];

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <span className={styles.icon}>📊</span>
        <span className={styles.title}>网站资讯</span>
      </div>
      <div className={styles.rows}>
        {rows.map((r) => (
          <div key={r.label} className={styles.row}>
            <span className={styles.label}>{r.label}</span>
            <span className={styles.value}>{r.value}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default SiteInfoCard;
