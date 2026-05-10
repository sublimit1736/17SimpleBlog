import React from 'react';
import {Link} from 'react-router-dom';
import Layout from '../components/layout/Layout';
import Button from '../components/ui/Button';
import styles from './NotFoundPage.module.css';

const ForbiddenPage: React.FC = () => (
    <Layout>
        <div className={styles.wrapper}>
            <div className={styles.code}>403</div>
            <h1 className={styles.heading}>访问被拒绝</h1>
            <p className={styles.sub}>你没有权限访问此页面</p>
            <Link to="/"><Button>返回首页</Button></Link>
        </div>
    </Layout>
);

export default ForbiddenPage;
