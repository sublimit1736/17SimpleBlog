import React, { useState, useEffect, useCallback } from 'react';
import { notificationsApi } from '../api/notifications';
import type { Notification } from '../types';
import { useToast } from '../components/ui/toastContext';
import Layout from '../components/layout/Layout';
import Button from '../components/ui/Button';
import { Loading } from '../components/ui/Loading';
import { formatDistanceToNow } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import styles from './NotificationsPage.module.css';

const NotificationsPage: React.FC = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const { showToast } = useToast();

  const fetch = useCallback(async () => {
    setLoading(true);
    try {
      const res = await notificationsApi.getAll(0, 50);
      if (res.data.statusCode === 0) setNotifications(res.data.data.content);
    } catch { /* silent */ }
    finally { setLoading(false); }
  }, []);

  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => { fetch(); }, [fetch]);

  const markAll = async () => {
    try {
      await notificationsApi.markAllRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      showToast('已全部标记为已读', 'success');
    } catch { showToast('操作失败', 'error'); }
  };

  const markOne = async (id: number) => {
    try {
      await notificationsApi.markRead(id);
      setNotifications((prev) => prev.map((n) => n.id === id ? { ...n, isRead: true } : n));
    } catch { /* silent */ }
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <Layout>
      <div className="container">
        <div className={styles.header}>
          <h1 className={styles.title}>通知中心</h1>
          {unreadCount > 0 && (
            <Button variant="outline" size="sm" onClick={markAll}>全部已读</Button>
          )}
        </div>

        {loading ? (
          <Loading center text="加载中..." />
        ) : notifications.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🔔</div>
            <p>暂无通知</p>
          </div>
        ) : (
          <div className={styles.list}>
            {notifications.map((n) => (
              <div
                key={n.id}
                className={`${styles.item} ${!n.isRead ? styles.unread : ''}`}
                onClick={() => !n.isRead && markOne(n.id)}
              >
                <div className={styles.dot} data-unread={!n.isRead} />
                <div className={styles.body}>
                  <p className={styles.content}>{n.content}</p>
                  <span className={styles.time}>
                    {formatDistanceToNow(new Date(n.createdAt), { addSuffix: true, locale: zhCN })}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default NotificationsPage;
