import React, {useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {authApi} from '../api/auth';
import {useAuthStore} from '../store/auth';
import {useToast} from '../components/ui/toastContext';
import Button from '../components/ui/Button';
import Layout from '../components/layout/Layout';
import styles from './AuthPage.module.css';

const LoginPage: React.FC = () => {
    const navigate = useNavigate();
    const login = useAuthStore((s) => s.login);
    const {showToast} = useToast();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!username.trim() || !password) {
            showToast('请填写用户名和密码', 'warning');
            return;
        }
        setLoading(true);
        try {
            const res = await authApi.login(username.trim(), password);
            if (res.data.statusCode === 0) {
                const d = res.data.data;
                login(
                    {id: d.id, username: d.username, avatarUrl: d.avatarUrl, role: d.role},
                    d.accessToken,
                    d.refreshToken
                );
                showToast(`欢迎回来，${d.username}！`, 'success');
                navigate('/');
            } else {
                showToast(res.data.statusMessage || '登录失败', 'error');
            }
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { statusMessage?: string } } })?.response?.data?.statusMessage;
            showToast(msg || '登录失败，请检查用户名和密码', 'error');
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
                    <h2 className={styles.heading}>登录账号</h2>
                    <p className={styles.sub}>欢迎回来！请登录你的账号</p>

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
                                placeholder="请输入密码"
                                autoComplete="current-password"
                            />
                        </div>
                        <Button type="submit" loading={loading} fullWidth size="lg">
                            登录
                        </Button>
                    </form>

                    <p className={styles.switchText}>
                        还没有账号？<Link to="/register" className={styles.switchLink}>立即注册</Link>
                    </p>
                </div>
            </div>
        </Layout>
    );
};

export default LoginPage;
