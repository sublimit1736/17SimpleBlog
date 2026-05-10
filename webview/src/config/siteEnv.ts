/**
 * Centralised access to VITE_ environment variables with defaults.
 */

export const SITE_NAME: string =
    (import.meta.env.VITE_SITE_NAME as string | undefined)?.trim() || 'Chunana的个人博客';

export const META_CONFIG_ENABLE: boolean =
    (import.meta.env.VITE_META_CONFIG_ENABLE as string | undefined)?.trim().toLowerCase() !== 'false';
