import client from './client';
import type {ApiResponse, MediaUpload} from '../types';

export const mediaApi = {
    upload: (file: File) => {
        const form = new FormData();
        form.append('file', file);
        return client.post<ApiResponse<MediaUpload>>('/media/upload', form);
    },

    getFileUrl: (fileName: string) => `/api/media/files/${fileName}`,
};
