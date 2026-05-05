import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './styles/variables.css'
import './styles/global.css'
import {ToastProvider} from './components/ui/Toast'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <ToastProvider>
            <App/>
        </ToastProvider>
    </StrictMode>,
)
