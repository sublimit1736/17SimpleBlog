import {createBrowserRouter} from 'react-router-dom';
import {RequireAdmin, RequireAuth} from './router/RouteGuards';

import HomePage from './pages/HomePage';
import ArticleListPage from './pages/ArticleListPage';
import ArticleDetailPage from './pages/ArticleDetailPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import UploadArticlePage from './pages/UploadArticlePage';
import AdminPage from './pages/AdminPage';
import NotificationsPage from './pages/NotificationsPage';
import SearchPage from './pages/SearchPage';
import NotFoundPage from './pages/NotFoundPage';
import ForbiddenPage from './pages/ForbiddenPage';
import ServerErrorPage from './pages/ServerErrorPage';
import MetaConfigPage from './pages/MetaConfigPage';

const router = createBrowserRouter([
    {path: '/', element: <HomePage/>},
    {path: '/articles', element: <ArticleListPage/>},
    {path: '/article/:id', element: <ArticleDetailPage/>},
    {path: '/login', element: <LoginPage/>},
    {path: '/register', element: <RegisterPage/>},
    {path: '/profile/:uid', element: <ProfilePage/>},
    {path: '/search', element: <SearchPage/>},
    {path: '/forbidden', element: <ForbiddenPage/>},
    {path: '/error', element: <ServerErrorPage/>},
    {path: '/metaconfig', element: <MetaConfigPage/>},
    {
        element: <RequireAuth/>,
        children: [
            {path: '/write', element: <UploadArticlePage/>},
            {path: '/notifications', element: <NotificationsPage/>},
        ],
    },
    {
        element: <RequireAdmin/>,
        children: [
            {path: '/admin', element: <AdminPage/>},
        ],
    },
    {path: '*', element: <NotFoundPage/>},
]);

export default router;
