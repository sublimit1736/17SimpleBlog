import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface SiteConfig {
    faviconUrl: string;
    logoIconUrl: string;
    heroImages: string[];
    heroSlideInterval: number;
    bloggerName: string;
    bloggerAvatarUrl: string;
    bloggerBgUrl: string;
    typingTexts: string[];
}

interface SiteConfigState extends SiteConfig {
    setConfig: (partial: Partial<SiteConfig>) => void;
    addHeroImage: (url: string) => void;
    removeHeroImage: (index: number) => void;
    addTypingText: (text: string) => void;
    removeTypingText: (index: number) => void;
}

const DEFAULT_CONFIG: SiteConfig = {
    faviconUrl: '',
    logoIconUrl: '',
    heroImages: [],
    heroSlideInterval: 5000,
    bloggerName: '博主',
    bloggerAvatarUrl: '',
    bloggerBgUrl: '',
    typingTexts: ['分享技术，记录生活', '在这里找到你感兴趣的内容', 'Share ideas, explore the world'],
};

export const useSiteConfigStore = create<SiteConfigState>()(
    persist(
        (set) => ({
            ...DEFAULT_CONFIG,
            setConfig: (partial) => set((state) => ({ ...state, ...partial })),
            addHeroImage: (url) => set((state) => ({ heroImages: [...state.heroImages, url] })),
            removeHeroImage: (index) =>
                set((state) => ({ heroImages: state.heroImages.filter((_, i) => i !== index) })),
            addTypingText: (text) =>
                set((state) => ({ typingTexts: [...state.typingTexts, text] })),
            removeTypingText: (index) =>
                set((state) => ({ typingTexts: state.typingTexts.filter((_, i) => i !== index) })),
        }),
        { name: 'site-config' }
    )
);
