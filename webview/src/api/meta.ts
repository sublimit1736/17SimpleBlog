import client from './client';

const metaApi = {
    /**
     * Verify the owner token against the backend BCrypt hash.
     * Returns statusCode=0 on success, statusCode=5001 on invalid token.
     */
    verifyToken: (token: string) =>
        client.post<{ statusCode: number; statusMessage: string; data: boolean | null }>(
            '/meta/verify-token',
            { token }
        ),
};

export { metaApi };
