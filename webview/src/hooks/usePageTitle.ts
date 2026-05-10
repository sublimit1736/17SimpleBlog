import { useEffect } from 'react';

const SITE_NAME = 'Chunana的个人博客';

export function usePageTitle(subtitle: string) {
    useEffect(() => {
        document.title = `${SITE_NAME} - ${subtitle}`;
    }, [subtitle]);
}
