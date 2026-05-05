import React from 'react';
import { Link } from 'react-router-dom';
import styles from './Tag.module.css';

interface TagProps {
  label: string;
  onClick?: () => void;
  linkTo?: string;
  variant?: 'default' | 'outline' | 'filled';
  size?: 'sm' | 'md';
}

const Tag: React.FC<TagProps> = ({ label, onClick, linkTo, variant = 'default', size = 'sm' }) => {
  const cls = `${styles.tag} ${styles[variant]} ${styles[size]}`;

  if (linkTo) {
    return <Link to={linkTo} className={cls}>#{label}</Link>;
  }

  if (onClick) {
    return <button className={cls} onClick={onClick}>#{label}</button>;
  }

  return <span className={cls}>#{label}</span>;
};

export default Tag;
