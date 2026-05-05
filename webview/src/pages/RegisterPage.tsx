import React, {useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {authApi} from '../api/auth';
import {useToast} from '../components/ui/toastContext';
import Button from '../components/ui/Button';
import Layout from '../components/layout/Layout';
import styles from './AuthPage.module.css';

const RegisterPage: React.FC = () => {
    const navigate = useNavigate();
    const {showToast} = useToast();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirm, setConfirm] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!username.trim() || !password) {
            showToast('请填写用户名和密码', 'warning');
            return;
        }
        if (password !== confirm) {
            showToast('两次密码不一致', 'error');
            return;
        }
        if (password.length < 6) {
            showToast('密码至少6位', 'warning');
            return;
        }
        setLoading(true);
        try {
            const res = await authApi.register(username.trim(), password);
            if (res.data.statusCode === 0) {
                showToast('注册成功，请登录', 'success');
                navigate('/login');
            } else {
                showToast(res.data.statusMessage || '注册失败', 'error');
            }
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { statusMessage?: string } } })?.response?.data?.statusMessage;
            showToast(msg || '注册失败', 'error');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Layout>
            <div className={styles.pageWrapper}>
                <div className={styles.card}>
                    <div className={styles.brand}>
                        <span className={styles.brandIcon}>✦</span>
                        <h1 className={styles.brandName}>17SimpleBlog</h1>
                    </div>
                    <h2 className={styles.heading}>创建账号</h2>
                    <p className={styles.sub}>加入我们，开始你的写作之旅</p>

                    <form onSubmit={handleSubmit} className={styles.form}>
                        <div className={styles.field}>
                            <label className={styles.label}>用户名</label>
                            <input
                                className={styles.input}
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                placeholder="请输入用户名"
                                autoComplete="username"
                            />
                        </div>
                        <div className={styles.field}>
                            <label className={styles.label}>密码</label>
                            <input
                                className={styles.input}
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="至少6位密码"
                                autoComplete="new-password"
                            />
                        </div>
                        <div className={styles.field}>
                            <label className={styles.label}>确认密码</label>
                            <input
                                className={styles.input}
                                type="password"
                                value={confirm}
                                onChange={(e) => setConfirm(e.target.value)}
                                placeholder="再次输入密码"
                                autoComplete="new-password"
                            />
                        </div>
                        <Button type="submit" loading={loading} fullWidth size="lg">
                            注册
                        </Button>
                    </form>

                    <p className={styles.switchText}>
                        已有账号？<Link to="/login" className={styles.switchLink}>立即登录</Link>
                    </p>
                </div>
            </div>
        </Layout>
    );
};

export default RegisterPage;
