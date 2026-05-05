import React from 'react';
import styles from './Loading.module.css';

interface LoadingProps {
  size?: 'sm' | 'md' | 'lg';
  center?: boolean;
  text?: string;
}

export const Loading: React.FC<LoadingProps> = ({ size = 'md', center = true, text }) => {
  return (
    <div className={`${styles.wrapper} ${center ? styles.center : ''}`}>
      <div className={`${styles.spinner} ${styles[size]}`} />
      {text && <p className={styles.text}>{text}</p>}
    </div>
  );
};

export const SkeletonCard: React.FC = () => (
  <div className={styles.skeletonCard}>
    <div className={`${styles.skeleton} ${styles.skTitle}`} />
    <div className={`${styles.skeleton} ${styles.skText}`} />
    <div className={`${styles.skeleton} ${styles.skText} ${styles.skShort}`} />
    <div className={styles.skMeta}>
      <div className={`${styles.skeleton} ${styles.skCircle}`} />
      <div className={`${styles.skeleton} ${styles.skSmall}`} />
    </div>
  </div>
);

export default Loading;
