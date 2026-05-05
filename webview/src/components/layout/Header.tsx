import React, {useEffect, useRef, useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {useAuthStore} from '../../store/auth';
import {useUIStore} from '../../store/ui';
import {authApi} from '../../api/auth';
import {notificationsApi} from '../../api/notifications';
import Avatar from '../ui/Avatar';
import {useToast} from '../ui/toastContext';
import styles from './Header.module.css';

const Header: React.FC = () => {
  const { user, isAuthenticated, logout, refreshToken } = useAuthStore();
  const { theme, toggleTheme } = useUIStore();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) return;
    const fetchUnread = async () => {
      try {
        const res = await notificationsApi.getUnreadCount();
        if (res.data.statusCode === 0) setUnreadCount(res.data.data);
      } catch { /* silent */ }
    };
    fetchUnread();
    const interval = setInterval(fetchUnread, 60000);
    return () => clearInterval(interval);
  }, [isAuthenticated]);

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleLogout = async () => {
    try {
      if (refreshToken) {
        await authApi.logout(refreshToken);
      }
    } catch { /* silent */ }
    logout();
    showToast('已退出登录', 'info');
    navigate('/');
    setDropdownOpen(false);
  };

  return (
    <header className={styles.header}>
      <div className={`container ${styles.inner}`}>
        <div className={styles.left}>
          <Link to="/" className={styles.logo}>
            <span className={styles.logoIcon}>✦</span>
            <span>17SimpleBlog</span>
          </Link>
          <nav className={`${styles.nav} ${menuOpen ? styles.navOpen : ''}`}>
            <Link to="/" className={styles.navLink} onClick={() => setMenuOpen(false)}>首页</Link>
            <Link to="/articles" className={styles.navLink} onClick={() => setMenuOpen(false)}>文章</Link>
            <Link to="/search" className={styles.navLink} onClick={() => setMenuOpen(false)}>搜索</Link>
          </nav>
        </div>

        <div className={styles.right}>
          <button
            className={styles.iconBtn}
            onClick={toggleTheme}
            title={theme === 'light' ? '切换暗色模式' : '切换亮色模式'}
          >
            {theme === 'light' ? '🌙' : '☀️'}
          </button>

          {isAuthenticated ? (
            <>
              <Link to="/notifications" className={styles.bellBtn} title="通知">
                <span>🔔</span>
                {unreadCount > 0 && (
                  <span className={styles.badge}>{unreadCount > 99 ? '99+' : unreadCount}</span>
                )}
              </Link>
              <Link to="/write" className={styles.writeBtn}>✏ 写文章</Link>
              <div className={styles.userMenu} ref={dropdownRef}>
                <button
                  className={styles.avatarBtn}
                  onClick={() => setDropdownOpen((v) => !v)}
                >
                  <Avatar src={user?.avatarUrl} username={user?.username} size={34} />
                </button>
                {dropdownOpen && (
                  <div className={styles.dropdown}>
                    <div className={styles.dropdownHeader}>
                      <strong>{user?.username}</strong>
                      <span className={styles.role}>
                        {user?.role === 'ADMIN' ? '管理员' : '用户'}
                      </span>
                    </div>
                    <div className={styles.dropdownDivider} />
                    <Link
                      to={`/profile/${user?.id}`}
                      className={styles.dropdownItem}
                      onClick={() => setDropdownOpen(false)}
                    >
                      👤 个人主页
                    </Link>
                    {user?.role === 'ADMIN' && (
                      <Link
                        to="/admin"
                        className={styles.dropdownItem}
                        onClick={() => setDropdownOpen(false)}
                      >
                        🛡 管理后台
                      </Link>
                    )}
                    <div className={styles.dropdownDivider} />
                    <button
                      className={`${styles.dropdownItem} ${styles.logoutItem}`}
                      onClick={handleLogout}
                    >
                      🚪 退出登录
                    </button>
                  </div>
                )}
              </div>
            </>
          ) : (
            <>
              <Link to="/login" className={styles.loginBtn}>登录</Link>
              <Link to="/register" className={styles.registerBtn}>注册</Link>
            </>
          )}

          <button
            className={styles.hamburger}
            onClick={() => setMenuOpen((v) => !v)}
            aria-label="菜单"
          >
            <span />
            <span />
            <span />
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;
