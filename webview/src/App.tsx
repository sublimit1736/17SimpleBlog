import {RouterProvider} from 'react-router-dom';
import {useEffect} from 'react';
import router from './router';
import {useSiteConfigStore} from './store/siteConfig';
import {sanitizeImageUrl} from './utils/sanitizeImageUrl';

function App() {
    const {faviconUrl} = useSiteConfigStore();

    useEffect(() => {
        const safeUrl = sanitizeImageUrl(faviconUrl);
        if (!safeUrl) return;
        let link = document.querySelector<HTMLLinkElement>('link[rel~="icon"]');
        if (!link) {
            link = document.createElement('link');
            link.rel = 'icon';
            document.head.appendChild(link);
        }
        link.href = safeUrl;
    }, [faviconUrl]);

    return <RouterProvider router={router}/>;
}

export default App;
