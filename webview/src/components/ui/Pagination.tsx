import React from 'react';
import styles from './Pagination.module.css';
import Button from './Button';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

const Pagination: React.FC<PaginationProps> = ({ currentPage, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;

  const pages: (number | '...')[] = [];
  if (totalPages <= 7) {
    for (let i = 0; i < totalPages; i++) pages.push(i);
  } else {
    pages.push(0);
    if (currentPage > 3) pages.push('...');
    for (let i = Math.max(1, currentPage - 2); i <= Math.min(totalPages - 2, currentPage + 2); i++) {
      pages.push(i);
    }
    if (currentPage < totalPages - 4) pages.push('...');
    pages.push(totalPages - 1);
  }

  return (
    <div className={styles.pagination}>
      <Button
        variant="ghost"
        size="sm"
        disabled={currentPage === 0}
        onClick={() => onPageChange(currentPage - 1)}
      >
        ← 上一页
      </Button>
      <div className={styles.pages}>
        {pages.map((p, i) =>
          p === '...' ? (
            <span key={`ellipsis-${i}`} className={styles.ellipsis}>…</span>
          ) : (
            <button
              key={p}
              className={`${styles.page} ${p === currentPage ? styles.active : ''}`}
              onClick={() => onPageChange(p as number)}
            >
              {(p as number) + 1}
            </button>
          )
        )}
      </div>
      <Button
        variant="ghost"
        size="sm"
        disabled={currentPage === totalPages - 1}
        onClick={() => onPageChange(currentPage + 1)}
      >
        下一页 →
      </Button>
    </div>
  );
};

export default Pagination;
