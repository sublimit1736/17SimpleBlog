import { useEffect } from 'react';
import { SITE_NAME } from '../config/siteEnv';

export function usePageTitle(subtitle: string) {
    useEffect(() => {
        document.title = `${SITE_NAME} - ${subtitle}`;
    }, [subtitle]);
}
