import React, {useRef, useState} from 'react';
import Layout from '../components/layout/Layout';
import {useSiteConfigStore} from '../store/siteConfig';
import {mediaApi} from '../api/media';
import {useToast} from '../components/ui/toastContext';
import {usePageTitle} from '../hooks/usePageTitle';
import styles from './InitPage.module.css';

const InitPage: React.FC = () => {
    usePageTitle('初始化配置');

    const {
        faviconUrl, logoIconUrl,
        heroImages, heroSlideInterval,
        bloggerName, bloggerAvatarUrl, bloggerBgUrl,
        typingTexts,
        setConfig, addHeroImage, removeHeroImage, addTypingText, removeTypingText,
    } = useSiteConfigStore();

    const {showToast} = useToast();

    // Local edit states
    const [editFaviconUrl, setEditFaviconUrl] = useState(faviconUrl);
    const [editLogoUrl, setEditLogoUrl] = useState(logoIconUrl);
    const [editInterval, setEditInterval] = useState(String(heroSlideInterval / 1000));
    const [editBloggerName, setEditBloggerName] = useState(bloggerName);
    const [editBloggerAvatar, setEditBloggerAvatar] = useState(bloggerAvatarUrl);
    const [editBloggerBg, setEditBloggerBg] = useState(bloggerBgUrl);
    const [newTypingText, setNewTypingText] = useState('');
    const [newHeroImageUrl, setNewHeroImageUrl] = useState('');

    const faviconFileRef = useRef<HTMLInputElement>(null);
    const logoFileRef = useRef<HTMLInputElement>(null);
    const heroFileRef = useRef<HTMLInputElement>(null);
    const avatarFileRef = useRef<HTMLInputElement>(null);
    const bloggerBgFileRef = useRef<HTMLInputElement>(null);

    const uploadFile = async (file: File): Promise<string | null> => {
        try {
            const res = await mediaApi.upload(file);
            if (res.data.statusCode === 0) {
                return res.data.data.url;
            }
            showToast('上传失败：' + res.data.statusMessage, 'error');
            return null;
        } catch {
            showToast('上传失败', 'error');
            return null;
        }
    };

    const handleFaviconFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        const url = await uploadFile(file);
        if (url) {
            setEditFaviconUrl(url);
            showToast('图标已上传', 'success');
        }
    };

    const handleLogoFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        const url = await uploadFile(file);
        if (url) {
            setEditLogoUrl(url);
            showToast('图标已上传', 'success');
        }
    };

    const handleHeroFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        const url = await uploadFile(file);
        if (url) {
            addHeroImage(url);
            showToast('背景图已添加', 'success');
        }
        e.target.value = '';
    };

    const handleAvatarFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        const url = await uploadFile(file);
        if (url) {
            setEditBloggerAvatar(url);
            showToast('头像已上传', 'success');
        }
    };

    const handleBloggerBgFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        const url = await uploadFile(file);
        if (url) {
            setEditBloggerBg(url);
            showToast('背景图已上传', 'success');
        }
    };

    const handleSaveIcons = () => {
        setConfig({faviconUrl: editFaviconUrl, logoIconUrl: editLogoUrl});
        showToast('图标设置已保存', 'success');
    };

    const handleAddHeroImageByUrl = () => {
        const url = newHeroImageUrl.trim();
        if (!url) return;
        addHeroImage(url);
        setNewHeroImageUrl('');
        showToast('背景图已添加', 'success');
    };

    const handleSaveHeroInterval = () => {
        const val = parseFloat(editInterval);
        if (isNaN(val) || val < 1) {
            showToast('请输入有效的秒数（≥1）', 'warning');
            return;
        }
        setConfig({heroSlideInterval: Math.round(val * 1000)});
        showToast('轮播间隔已保存', 'success');
    };

    const handleSaveBlogger = () => {
        setConfig({
            bloggerName: editBloggerName.trim() || '博主',
            bloggerAvatarUrl: editBloggerAvatar,
            bloggerBgUrl: editBloggerBg,
        });
        showToast('博主卡片设置已保存', 'success');
    };

    const handleAddTypingText = () => {
        const text = newTypingText.trim();
        if (!text) return;
        addTypingText(text);
        setNewTypingText('');
        showToast('文本已添加', 'success');
    };

    return (
        <Layout>
            <div className="container">
                <div className={styles.page}>
                    <div className={styles.pageHeader}>
                        <h1 className={styles.pageTitle}>初始化配置</h1>
                        <p className={styles.pageSubtitle}>自定义博客外观，配置存储于本地浏览器</p>
                    </div>

                    {/* Section: 网站图标 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>网站图标</h2>
                        <div className={styles.grid2}>
                            <div className={styles.fieldGroup}>
                                <label className={styles.label}>浏览器标签页图标（小图标）</label>
                                {editFaviconUrl && (
                                    <img src={editFaviconUrl} alt="favicon preview" className={styles.iconPreview} />
                                )}
                                <input
                                    className={styles.input}
                                    type="text"
                                    value={editFaviconUrl}
                                    onChange={(e) => setEditFaviconUrl(e.target.value)}
                                    placeholder="输入图片 URL 或上传文件"
                                />
                                <div className={styles.btnRow}>
                                    <button className={styles.outlineBtn} onClick={() => faviconFileRef.current?.click()}>
                                        上传文件
                                    </button>
                                    <input
                                        ref={faviconFileRef}
                                        type="file"
                                        accept="image/*"
                                        className={styles.hidden}
                                        onChange={handleFaviconFileChange}
                                    />
                                </div>
                            </div>

                            <div className={styles.fieldGroup}>
                                <label className={styles.label}>顶栏图标（大图标）</label>
                                {editLogoUrl && (
                                    <img src={editLogoUrl} alt="logo preview" className={styles.logoPreview} />
                                )}
                                <input
                                    className={styles.input}
                                    type="text"
                                    value={editLogoUrl}
                                    onChange={(e) => setEditLogoUrl(e.target.value)}
                                    placeholder="输入图片 URL 或上传文件"
                                />
                                <div className={styles.btnRow}>
                                    <button className={styles.outlineBtn} onClick={() => logoFileRef.current?.click()}>
                                        上传文件
                                    </button>
                                    <input
                                        ref={logoFileRef}
                                        type="file"
                                        accept="image/*"
                                        className={styles.hidden}
                                        onChange={handleLogoFileChange}
                                    />
                                </div>
                            </div>
                        </div>
                        <button className={styles.primaryBtn} onClick={handleSaveIcons}>保存图标设置</button>
                    </section>

                    {/* Section: Hero 背景图 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>Hero 背景图</h2>
                        <p className={styles.hint}>添加图片后，首页 Hero 将以幻灯片形式轮播，不再显示渐变背景。</p>

                        <div className={styles.imageGrid}>
                            {heroImages.map((url, i) => (
                                <div key={i} className={styles.imageCard}>
                                    <img src={url} alt={`hero-${i}`} className={styles.imageThumb} />
                                    <button
                                        className={styles.removeBtn}
                                        onClick={() => removeHeroImage(i)}
                                        title="移除"
                                    >
                                        ✕
                                    </button>
                                </div>
                            ))}
                            {heroImages.length === 0 && (
                                <p className={styles.emptyHint}>暂无背景图，将使用默认渐变背景</p>
                            )}
                        </div>

                        <div className={styles.addRow}>
                            <input
                                className={styles.input}
                                type="text"
                                value={newHeroImageUrl}
                                onChange={(e) => setNewHeroImageUrl(e.target.value)}
                                placeholder="输入图片 URL"
                                onKeyDown={(e) => e.key === 'Enter' && handleAddHeroImageByUrl()}
                            />
                            <button className={styles.outlineBtn} onClick={handleAddHeroImageByUrl}>添加 URL</button>
                            <button className={styles.outlineBtn} onClick={() => heroFileRef.current?.click()}>
                                上传文件
                            </button>
                            <input
                                ref={heroFileRef}
                                type="file"
                                accept="image/*"
                                className={styles.hidden}
                                onChange={handleHeroFileChange}
                            />
                        </div>

                        <div className={styles.fieldRow}>
                            <label className={styles.label}>轮播间隔（秒）</label>
                            <input
                                className={`${styles.input} ${styles.inputSm}`}
                                type="number"
                                min={1}
                                value={editInterval}
                                onChange={(e) => setEditInterval(e.target.value)}
                            />
                            <button className={styles.outlineBtn} onClick={handleSaveHeroInterval}>保存</button>
                        </div>
                    </section>

                    {/* Section: 博主卡片 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>博主卡片</h2>
                        <div className={styles.grid2}>
                            <div className={styles.fieldGroup}>
                                <label className={styles.label}>博主名称</label>
                                <input
                                    className={styles.input}
                                    type="text"
                                    value={editBloggerName}
                                    onChange={(e) => setEditBloggerName(e.target.value)}
                                    placeholder="博主"
                                />
                            </div>

                            <div className={styles.fieldGroup}>
                                <label className={styles.label}>博主头像</label>
                                {editBloggerAvatar && (
                                    <img src={editBloggerAvatar} alt="avatar preview" className={styles.avatarPreview} />
                                )}
                                <input
                                    className={styles.input}
                                    type="text"
                                    value={editBloggerAvatar}
                                    onChange={(e) => setEditBloggerAvatar(e.target.value)}
                                    placeholder="输入图片 URL 或上传文件"
                                />
                                <div className={styles.btnRow}>
                                    <button className={styles.outlineBtn} onClick={() => avatarFileRef.current?.click()}>
                                        上传头像
                                    </button>
                                    <input
                                        ref={avatarFileRef}
                                        type="file"
                                        accept="image/*"
                                        className={styles.hidden}
                                        onChange={handleAvatarFileChange}
                                    />
                                </div>
                            </div>

                            <div className={styles.fieldGroup}>
                                <label className={styles.label}>博主卡片背景图</label>
                                {editBloggerBg && (
                                    <img src={editBloggerBg} alt="bg preview" className={styles.bgPreview} />
                                )}
                                <input
                                    className={styles.input}
                                    type="text"
                                    value={editBloggerBg}
                                    onChange={(e) => setEditBloggerBg(e.target.value)}
                                    placeholder="输入图片 URL 或上传文件"
                                />
                                <div className={styles.btnRow}>
                                    <button className={styles.outlineBtn} onClick={() => bloggerBgFileRef.current?.click()}>
                                        上传背景图
                                    </button>
                                    <input
                                        ref={bloggerBgFileRef}
                                        type="file"
                                        accept="image/*"
                                        className={styles.hidden}
                                        onChange={handleBloggerBgFileChange}
                                    />
                                </div>
                            </div>
                        </div>
                        <button className={styles.primaryBtn} onClick={handleSaveBlogger}>保存博主设置</button>
                    </section>

                    {/* Section: 打字机文本池 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>打字机文本池</h2>
                        <p className={styles.hint}>首页 Hero 打字机效果将随机从下列文本中选取，显示时间根据文本长度动态调整。</p>

                        <div className={styles.textList}>
                            {typingTexts.map((text, i) => (
                                <div key={i} className={styles.textItem}>
                                    <span className={styles.textContent}>{text}</span>
                                    <button
                                        className={styles.removeBtn}
                                        onClick={() => removeTypingText(i)}
                                        title="删除"
                                    >
                                        ✕
                                    </button>
                                </div>
                            ))}
                            {typingTexts.length === 0 && (
                                <p className={styles.emptyHint}>文本池为空，将使用默认文本</p>
                            )}
                        </div>

                        <div className={styles.addRow}>
                            <input
                                className={styles.input}
                                type="text"
                                value={newTypingText}
                                onChange={(e) => setNewTypingText(e.target.value)}
                                placeholder="输入新文本..."
                                onKeyDown={(e) => e.key === 'Enter' && handleAddTypingText()}
                            />
                            <button className={styles.primaryBtn} onClick={handleAddTypingText}>添加</button>
                        </div>
                    </section>
                </div>
            </div>
        </Layout>
    );
};

export default InitPage;
