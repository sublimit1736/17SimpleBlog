import {RouterProvider} from 'react-router-dom';
import {useEffect} from 'react';
import router from './router';
import {useSiteConfigStore} from './store/siteConfig';

function App() {
    const {faviconUrl} = useSiteConfigStore();

    useEffect(() => {
        if (!faviconUrl) return;
        let link = document.querySelector<HTMLLinkElement>('link[rel~="icon"]');
        if (!link) {
            link = document.createElement('link');
            link.rel = 'icon';
            document.head.appendChild(link);
        }
        link.href = faviconUrl;
    }, [faviconUrl]);

    return <RouterProvider router={router}/>;
}

export default App;
