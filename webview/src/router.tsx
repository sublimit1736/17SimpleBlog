import React from 'react';
import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from './store/auth';

import HomePage from './pages/HomePage';
import ArticleListPage from './pages/ArticleListPage';
import ArticleDetailPage from './pages/ArticleDetailPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import WriteArticlePage from './pages/WriteArticlePage';
import AdminPage from './pages/AdminPage';
import NotificationsPage from './pages/NotificationsPage';
import SearchPage from './pages/SearchPage';
import NotFoundPage from './pages/NotFoundPage';

const RequireAuth: React.FC = () => {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
};

const RequireAdmin: React.FC = () => {
  const user = useAuthStore((s) => s.user);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role !== 'ADMIN') return <Navigate to="/" replace />;
  return <Outlet />;
};

const router = createBrowserRouter([
  { path: '/', element: <HomePage /> },
  { path: '/articles', element: <ArticleListPage /> },
  { path: '/article/:id', element: <ArticleDetailPage /> },
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  { path: '/profile/:uid', element: <ProfilePage /> },
  { path: '/search', element: <SearchPage /> },
  {
    element: <RequireAuth />,
    children: [
      { path: '/write', element: <WriteArticlePage /> },
      { path: '/edit/:id', element: <WriteArticlePage /> },
      { path: '/notifications', element: <NotificationsPage /> },
    ],
  },
  {
    element: <RequireAdmin />,
    children: [
      { path: '/admin', element: <AdminPage /> },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
]);

export default router;
