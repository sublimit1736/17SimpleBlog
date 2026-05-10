/**
 * Sanitize a user-supplied URL so it can be safely used as a CSS
 * `background-image` value or `<img src>` attribute.
 *
 * Only allows safe schemes: https, http, root-relative paths, and
 * data URIs that are images.  Everything else returns an empty string.
 */
export function sanitizeImageUrl(url: string): string {
    if (!url) return '';
    const trimmed = url.trim();
    const lower = trimmed.toLowerCase();
    if (
        lower.startsWith('https://') ||
        lower.startsWith('http://') ||
        lower.startsWith('/') ||
        lower.startsWith('data:image/')
    ) {
        return trimmed;
    }
    return '';
}
