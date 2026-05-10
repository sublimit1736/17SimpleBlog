import React, {useEffect, useRef, useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {useAuthStore} from '../../store/auth';
import {useUIStore} from '../../store/ui';
import {useSiteConfigStore} from '../../store/siteConfig';
import {authApi} from '../../api/auth';
import Avatar from '../ui/Avatar';
import {useToast} from '../ui/toastContext';
import {SITE_NAME} from '../../config/siteEnv';
import styles from './Header.module.css';

const Header: React.FC = () => {
  const { user, isAuthenticated, logout, refreshToken } = useAuthStore();
  const { theme, toggleTheme } = useUIStore();
  const { logoIconUrl } = useSiteConfigStore();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const [menuOpen, setMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const [searchValue, setSearchValue] = useState('');

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 60);
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

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

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const q = searchValue.trim();
    if (q) {
      navigate(`/search?type=articles&q=${encodeURIComponent(q)}`);
      setSearchValue('');
    }
  };

  return (
    <header className={`${styles.header} ${scrolled ? styles.scrolled : styles.transparent}`}>
      <div className={`container ${styles.inner}`}>
        {/* Left: logo + nav */}
        <div className={styles.left}>
          <Link to="/" className={styles.logo}>
            {logoIconUrl ? (
              <img src={logoIconUrl} alt="logo" className={styles.logoImg} />
            ) : (
              <span className={styles.logoIcon}>✦</span>
            )}
            <span className={styles.logoText}>{SITE_NAME}</span>
          </Link>
          <nav className={`${styles.nav} ${menuOpen ? styles.navOpen : ''}`}>
            <Link to="/" className={styles.navLink} onClick={() => setMenuOpen(false)}>首页</Link>
            <Link to="/articles" className={styles.navLink} onClick={() => setMenuOpen(false)}>文章</Link>
          </nav>
        </div>

        {/* Center: search */}
        <div className={styles.center}>
          <form className={styles.searchForm} onSubmit={handleSearch}>
            <span className={styles.searchIcon}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16">
                <circle cx="11" cy="11" r="8" />
                <path strokeLinecap="round" d="M21 21l-4.35-4.35" />
              </svg>
            </span>
            <input
              className={styles.searchInput}
              type="text"
              placeholder="搜索文章..."
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
            />
          </form>
        </div>

        {/* Right: theme toggle + login */}
        <div className={styles.right}>
          <button
            className={styles.iconBtn}
            onClick={toggleTheme}
            title={theme === 'light' ? '切换暗色模式' : '切换亮色模式'}
          >
            {theme === 'light' ? (
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16">
                <path d="M21 12.79A9 9 0 1 1 11.21 3a7 7 0 0 0 9.79 9.79z" />
              </svg>
            ) : (
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16">
                <circle cx="12" cy="12" r="5" />
                <line x1="12" y1="1" x2="12" y2="3" />
                <line x1="12" y1="21" x2="12" y2="23" />
                <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" />
                <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
                <line x1="1" y1="12" x2="3" y2="12" />
                <line x1="21" y1="12" x2="23" y2="12" />
                <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" />
                <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
              </svg>
            )}
          </button>

          {isAuthenticated ? (
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
                    个人主页
                  </Link>
                  {user?.role === 'ADMIN' && (
                    <Link
                      to="/admin"
                      className={styles.dropdownItem}
                      onClick={() => setDropdownOpen(false)}
                    >
                      管理后台
                    </Link>
                  )}
                  <div className={styles.dropdownDivider} />
                  <button
                    className={`${styles.dropdownItem} ${styles.logoutItem}`}
                    onClick={handleLogout}
                  >
                    退出登录
                  </button>
                </div>
              )}
            </div>
          ) : (
            <Link to="/login" className={styles.loginBtn}>登录</Link>
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
