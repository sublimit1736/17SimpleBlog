import React from 'react';
import {Link} from 'react-router-dom';
import Layout from '../components/layout/Layout';
import Button from '../components/ui/Button';
import styles from './NotFoundPage.module.css';

const ServerErrorPage: React.FC = () => (
    <Layout>
        <div className={styles.wrapper}>
            <div className={styles.code}>500</div>
            <h1 className={styles.heading}>服务器错误</h1>
            <p className={styles.sub}>服务器出现了一些问题，请稍后再试</p>
            <Link to="/"><Button>返回首页</Button></Link>
        </div>
    </Layout>
);

export default ServerErrorPage;
