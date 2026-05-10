import {defineConfig, loadEnv} from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig(({mode}) => {
    const env = loadEnv(mode, process.cwd(), '');
    const backendAddr = env.VITE_BACKEND_ADDRESS || 'localhost';
    const backendPort = env.VITE_BACKEND_PORT || '8080';
    const backendTarget = `http://${backendAddr}:${backendPort}`;

    return {
        plugins: [react()],
        resolve: {
            alias: {
                '@': path.resolve(__dirname, './src'),
            },
        },
        server: {
            proxy: {
                '/api': {
                    target: backendTarget,
                    changeOrigin: true,
                    rewrite: (p) => p.replace(/^\/api/, ''),
                },
            },
        },
    };
})
