import axios, {AxiosError, type InternalAxiosRequestConfig} from 'axios';
import {useAuthStore} from '../store/auth';

const normalizeBaseUrl = (url: string) => url.replace(/\/$/, '');

const resolveApiBaseUrl = () => {
    const envBaseUrl = import.meta.env.VITE_API_BASE_URL as string | undefined;
    if (envBaseUrl && envBaseUrl.trim()) return normalizeBaseUrl(envBaseUrl.trim());
    if (import.meta.env.DEV) {
        const backendPort = (import.meta.env.VITE_BACKEND_PORT as string | undefined) ?? '8080';
        return normalizeBaseUrl(`${window.location.protocol}//${window.location.hostname}:${backendPort}/api`);
    }
    return '/api';
};

const apiBaseUrl = resolveApiBaseUrl();

const client = axios.create({
    baseURL: apiBaseUrl,
    timeout: 15000,
});

let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

function subscribeTokenRefresh(cb: (token: string) => void) {
    refreshSubscribers.push(cb);
}

function onRefreshed(token: string) {
    refreshSubscribers.forEach((cb) => cb(token));
    refreshSubscribers = [];
}

client.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = useAuthStore.getState().accessToken;
    if (token && config.headers) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
});

client.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        if (error.response?.status === 401 && !originalRequest._retry) {
            const refreshToken = useAuthStore.getState().refreshToken;

            if (!refreshToken) {
                useAuthStore.getState().logout();
                window.location.href = '/login';
                return Promise.reject(error);
            }

            if (isRefreshing) {
                return new Promise((resolve) => {
                    subscribeTokenRefresh((token) => {
                        if (originalRequest.headers) {
                            originalRequest.headers['Authorization'] = `Bearer ${token}`;
                        }
                        resolve(client(originalRequest));
                    });
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                const res = await axios.post(`${apiBaseUrl}/user/auth/refresh`, null, {
                    headers: {'X-Refresh-Token': refreshToken},
                });
                const data = res.data?.data;
                if (data?.accessToken) {
                    useAuthStore.getState().setTokens(data.accessToken, data.refreshToken);
                    onRefreshed(data.accessToken);
                    if (originalRequest.headers) {
                        originalRequest.headers['Authorization'] = `Bearer ${data.accessToken}`;
                    }
                    return client(originalRequest);
                }
            } catch {
                useAuthStore.getState().logout();
                window.location.href = '/login';
                return Promise.reject(error);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);

export default client;
