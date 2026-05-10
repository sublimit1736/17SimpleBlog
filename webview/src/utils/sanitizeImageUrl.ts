/**
 * Sanitize a user-supplied URL so it can be safely used as a CSS
 * `background-image` value or `<img src>` attribute.
 *
 * Only allows safe schemes: https, http, root-relative paths, and
 * well-formed image data URIs.  Everything else returns an empty string.
 */
export function sanitizeImageUrl(url: string): string {
    if (!url) return '';
    const trimmed = url.trim();
    const lower = trimmed.toLowerCase();
    if (lower.startsWith('https://') || lower.startsWith('http://') || lower.startsWith('/')) {
        return trimmed;
    }
    // Only accept data URIs that are clearly image types (base64-encoded)
    if (/^data:image\/(png|jpe?g|gif|webp|svg\+xml|bmp|ico|avif);base64,/i.test(trimmed)) {
        return trimmed;
    }
    return '';
}
