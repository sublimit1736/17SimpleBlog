import React from 'react';
import {Link} from 'react-router-dom';
import Layout from '../components/layout/Layout';
import Button from '../components/ui/Button';
import styles from './NotFoundPage.module.css';

const NotFoundPage: React.FC = () => (
    <Layout>
        <div className={styles.wrapper}>
            <div className={styles.code}>404</div>
            <h1 className={styles.heading}>页面不存在</h1>
            <p className={styles.sub}>你访问的页面已不存在或被移动了</p>
            <Link to="/"><Button>返回首页</Button></Link>
        </div>
    </Layout>
);

export default NotFoundPage;
