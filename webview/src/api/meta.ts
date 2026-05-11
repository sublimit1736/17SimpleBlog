import client from './client';

export interface UploadPermissions {
    userAllowed: boolean;
    adminAllowed: boolean;
    blacklist: number[];
}

const metaApi = {
    /** Verify the owner token against the backend BCrypt hash. */
    verifyToken: (token: string) =>
        client.post<{ statusCode: number; statusMessage: string; data: boolean | null }>(
            '/meta/verify-token',
            { token }
        ),

    /** Fetch current upload permission settings (public). */
    getUploadPermissions: () =>
        client.get<{ statusCode: number; statusMessage: string; data: UploadPermissions }>(
            '/meta/upload-permissions'
        ),

    /** Update role-level upload switches. Requires owner token. */
    setUploadPermissions: (token: string, userAllowed: boolean, adminAllowed: boolean) =>
        client.post<{ statusCode: number; statusMessage: string; data: UploadPermissions }>(
            '/meta/upload-permissions',
            { token, userAllowed, adminAllowed }
        ),

    /** Add a user ID to the upload blacklist. Requires owner token. */
    addToBlacklist: (token: string, userId: number) =>
        client.post<{ statusCode: number; statusMessage: string; data: UploadPermissions }>(
            '/meta/upload-blacklist/add',
            { token, userId }
        ),

    /** Remove a user ID from the upload blacklist. Requires owner token. */
    removeFromBlacklist: (token: string, userId: number) =>
        client.post<{ statusCode: number; statusMessage: string; data: UploadPermissions }>(
            '/meta/upload-blacklist/remove',
            { token, userId }
        ),

    /** Promote a user to ADMIN. Requires owner token. */
    promoteUser: (token: string, uid: number) =>
        client.put<{ statusCode: number; statusMessage: string; data: string }>(
            `/meta/users/${uid}/promote`,
            { token }
        ),

    /** Demote a user to USER. Requires owner token. */
    demoteUser: (token: string, uid: number) =>
        client.put<{ statusCode: number; statusMessage: string; data: string }>(
            `/meta/users/${uid}/demote`,
            { token }
        ),
};

export { metaApi };
