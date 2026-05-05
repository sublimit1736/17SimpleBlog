import React from 'react';
import { Link } from 'react-router-dom';
import styles from './Footer.module.css';

const Footer: React.FC = () => {
  return (
    <footer className={styles.footer}>
      <div className={`container ${styles.inner}`}>
        <div className={styles.left}>
          <Link to="/" className={styles.logo}>✦ 17SimpleBlog</Link>
          <p className={styles.desc}>一个简单的博客/论坛系统</p>
        </div>
        <div className={styles.links}>
          <Link to="/" className={styles.link}>首页</Link>
          <Link to="/articles" className={styles.link}>文章</Link>
          <Link to="/search" className={styles.link}>搜索</Link>
        </div>
        <p className={styles.copy}>© {new Date().getFullYear()} 17SimpleBlog. All rights reserved.</p>
      </div>
    </footer>
  );
};

export default Footer;
