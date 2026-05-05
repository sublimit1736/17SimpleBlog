import React from 'react';
import {Navigate, Outlet} from 'react-router-dom';
import {useAuthStore} from '../store/auth';

export const RequireAuth: React.FC = () => {
    const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
    return isAuthenticated ? <Outlet/> : <Navigate to="/login" replace/>;
};

export const RequireAdmin: React.FC = () => {
    const user = useAuthStore((s) => s.user);
    const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
    if (!isAuthenticated) return <Navigate to="/login" replace/>;
    if (user?.role !== 'ADMIN') return <Navigate to="/" replace/>;
    return <Outlet/>;
};
