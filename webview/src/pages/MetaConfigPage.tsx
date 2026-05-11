import React, {useCallback, useEffect, useRef, useState} from 'react';
import {Navigate} from 'react-router-dom';
import Layout from '../components/layout/Layout';
import {useSiteConfigStore} from '../store/siteConfig';
import {mediaApi} from '../api/media';
import {metaApi, type UploadPermissions} from '../api/meta';
import {useToast} from '../components/ui/toastContext';
import {usePageTitle} from '../hooks/usePageTitle';
import {META_CONFIG_ENABLE} from '../config/siteEnv';
import {sanitizeImageUrl} from '../utils/sanitizeImageUrl';
import styles from './MetaConfigPage.module.css';

/* ─────────────────────────────────────────────────────────
   Token-gate: a simple overlay asking for the owner token.
───────────────────────────────────────────────────────── */
const SESSION_KEY = '_mcv';

function isSessionVerified(): boolean {
    try { return sessionStorage.getItem(SESSION_KEY) === '1'; } catch { return false; }
}
function markSessionVerified(): void {
    try { sessionStorage.setItem(SESSION_KEY, '1'); } catch { /* ignore */ }
}

interface TokenGateProps { onVerified: () => void; }

const TokenGate: React.FC<TokenGateProps> = ({onVerified}) => {
    const [token, setToken] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!token.trim()) return;
        setLoading(true); setError('');
        try {
            const res = await metaApi.verifyToken(token.trim());
            if (res.data.statusCode === 0) { markSessionVerified(); onVerified(); }
            else if (res.data.statusCode === 6) setError('请求过于频繁，请稍后再试');
            else setError('口令不正确，请重新输入');
        } catch { setError('验证失败，请检查网络后重试'); }
        finally { setLoading(false); }
    };

    return (
        <Layout>
            <div className={styles.gateWrapper}>
                <div className={styles.gateCard}>
                    <div className={styles.gateLock}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="36" height="36">
                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                        </svg>
                    </div>
                    <h2 className={styles.gateTitle}>站点配置</h2>
                    <p className={styles.gateSub}>请输入站长口令以继续</p>
                    <form className={styles.gateForm} onSubmit={handleSubmit}>
                        <input
                            className={`${styles.input} ${error ? styles.inputError : ''}`}
                            type="password"
                            value={token}
                            onChange={(e) => { setToken(e.target.value); setError(''); }}
                            placeholder="站长口令"
                            autoFocus
                            autoComplete="current-password"
                        />
                        {error && <p className={styles.gateError}>{error}</p>}
                        <button className={styles.primaryBtn} type="submit" disabled={loading || !token.trim()}>
                            {loading ? '验证中…' : '确认'}
                        </button>
                    </form>
                </div>
            </div>
        </Layout>
    );
};

/* ─────────────────────────────────────────────────────────
   Confirm-token dialog: shown before every save operation.
   The confirmed token is passed back to the action so that
   API-backed operations can include it in the request.
───────────────────────────────────────────────────────── */
interface ConfirmTokenDialogProps {
    onConfirmed: (token: string) => void;
    onCancel: () => void;
}

const ConfirmTokenDialog: React.FC<ConfirmTokenDialogProps> = ({onConfirmed, onCancel}) => {
    const [token, setToken] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!token.trim()) return;
        setLoading(true); setError('');
        try {
            const res = await metaApi.verifyToken(token.trim());
            if (res.data.statusCode === 0) onConfirmed(token.trim());
            else if (res.data.statusCode === 6) setError('请求过于频繁，请稍后再试');
            else setError('口令不正确');
        } catch { setError('验证失败，请检查网络后重试'); }
        finally { setLoading(false); }
    };

    return (
        <div className={styles.dialogOverlay} onClick={onCancel}>
            <div className={styles.dialogCard} onClick={(e) => e.stopPropagation()}>
                <h3 className={styles.dialogTitle}>确认身份</h3>
                <p className={styles.dialogSub}>请再次输入站长口令以保存更改</p>
                <form onSubmit={handleSubmit} className={styles.gateForm}>
                    <input
                        className={`${styles.input} ${error ? styles.inputError : ''}`}
                        type="password"
                        value={token}
                        onChange={(e) => { setToken(e.target.value); setError(''); }}
                        placeholder="站长口令"
                        autoFocus
                        autoComplete="current-password"
                    />
                    {error && <p className={styles.gateError}>{error}</p>}
                    <div className={styles.dialogBtns}>
                        <button type="button" className={styles.outlineBtn} onClick={onCancel}>取消</button>
                        <button type="submit" className={styles.primaryBtn} disabled={loading || !token.trim()}>
                            {loading ? '验证中…' : '确认保存'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

const MetaConfigPage: React.FC = () => {
    usePageTitle('站点配置');
    if (!META_CONFIG_ENABLE) return <Navigate to="/forbidden" replace />;
    const [verified, setVerified] = useState(isSessionVerified());
    if (!verified) return <TokenGate onVerified={() => setVerified(true)} />;
    return <MetaConfigContent />;
};

/* Inner component (rendered only after token gate passes) */
const MetaConfigContent: React.FC = () => {
    const {
        faviconUrl, logoIconUrl,
        heroImages, heroSlideInterval,
        bloggerName, bloggerAvatarUrl, bloggerBgUrl,
        typingTexts,
        setConfig, addHeroImage, removeHeroImage, addTypingText, removeTypingText,
    } = useSiteConfigStore();

    const {showToast} = useToast();

    // ── Appearance states ────────────────────────────────────────────────────
    const [editFaviconUrl, setEditFaviconUrl]   = useState(faviconUrl);
    const [editLogoUrl, setEditLogoUrl]         = useState(logoIconUrl);
    const [editInterval, setEditInterval]       = useState(String(heroSlideInterval / 1000));
    const [editBloggerName, setEditBloggerName] = useState(bloggerName);
    const [editBloggerAvatar, setEditBloggerAvatar] = useState(bloggerAvatarUrl);
    const [editBloggerBg, setEditBloggerBg]     = useState(bloggerBgUrl);
    const [newTypingText, setNewTypingText]     = useState('');
    const [newHeroImageUrl, setNewHeroImageUrl] = useState('');

    // ── Upload permission states ──────────────────────────────────────────────
    const [uploadPerms, setUploadPerms] = useState<UploadPermissions>({
        userAllowed: true, adminAllowed: true, blacklist: [],
    });
    const [blacklistInput, setBlacklistInput] = useState('');
    const [permsBusy, setPermsBusy] = useState(false);

    // ── User role management states ───────────────────────────────────────────
    const [manageUid, setManageUid] = useState('');
    const [roleBusy, setRoleBusy] = useState(false);

    // ── Pending save (token re-confirmation) ──────────────────────────────────
    const [pendingSave, setPendingSave] = useState<{fn: (token: string) => void} | null>(null);

    const faviconFileRef    = useRef<HTMLInputElement>(null);
    const logoFileRef       = useRef<HTMLInputElement>(null);
    const heroFileRef       = useRef<HTMLInputElement>(null);
    const avatarFileRef     = useRef<HTMLInputElement>(null);
    const bloggerBgFileRef  = useRef<HTMLInputElement>(null);

    const safeFaviconUrl   = sanitizeImageUrl(editFaviconUrl);
    const safeLogoUrl      = sanitizeImageUrl(editLogoUrl);
    const safeAvatarUrl    = sanitizeImageUrl(editBloggerAvatar);
    const safeBloggerBgUrl = sanitizeImageUrl(editBloggerBg);

    /** Wrap any save action with a token re-confirmation dialog. */
    const requireToken = (action: (token: string) => void) => {
        setPendingSave({fn: action});
    };

    // ── Load upload permissions on mount ──────────────────────────────────────
    const loadPerms = useCallback(async () => {
        try {
            const res = await metaApi.getUploadPermissions();
            if (res.data.statusCode === 0) setUploadPerms(res.data.data);
        } catch { /* silent – backend may be offline */ }
    }, []);

    useEffect(() => { void loadPerms(); }, [loadPerms]);

    // ── File upload helper ────────────────────────────────────────────────────
    const uploadFile = async (file: File): Promise<string | null> => {
        try {
            const res = await mediaApi.upload(file);
            if (res.data.statusCode === 0) return res.data.data.url;
            showToast('上传失败：' + res.data.statusMessage, 'error');
            return null;
        } catch { showToast('上传失败', 'error'); return null; }
    };

    // ── Appearance handlers ───────────────────────────────────────────────────
    const handleFaviconFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0]; if (!file) return;
        const url = await uploadFile(file); if (url) { setEditFaviconUrl(url); showToast('图标已上传', 'success'); }
    };
    const handleLogoFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0]; if (!file) return;
        const url = await uploadFile(file); if (url) { setEditLogoUrl(url); showToast('图标已上传', 'success'); }
    };
    const handleHeroFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0]; if (!file) return;
        const url = await uploadFile(file); if (url) { addHeroImage(url); showToast('背景图已添加', 'success'); }
        e.target.value = '';
    };
    const handleAvatarFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0]; if (!file) return;
        const url = await uploadFile(file); if (url) { setEditBloggerAvatar(url); showToast('头像已上传', 'success'); }
    };
    const handleBloggerBgFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0]; if (!file) return;
        const url = await uploadFile(file); if (url) { setEditBloggerBg(url); showToast('背景图已上传', 'success'); }
    };

    const handleSaveIcons = () => requireToken(() => {
        setConfig({ faviconUrl: sanitizeImageUrl(editFaviconUrl), logoIconUrl: sanitizeImageUrl(editLogoUrl) });
        showToast('图标设置已保存', 'success');
    });

    const handleAddHeroImageByUrl = () => {
        const url = sanitizeImageUrl(newHeroImageUrl);
        if (!url) { showToast('请输入有效的图片 URL（https:// 或 /path/...）', 'warning'); return; }
        addHeroImage(url); setNewHeroImageUrl(''); showToast('背景图已添加', 'success');
    };

    const handleSaveHeroInterval = () => {
        const val = parseFloat(editInterval);
        if (isNaN(val) || val < 1) { showToast('请输入有效的秒数（≥1）', 'warning'); return; }
        requireToken(() => { setConfig({heroSlideInterval: Math.round(val * 1000)}); showToast('轮播间隔已保存', 'success'); });
    };

    const handleSaveBlogger = () => requireToken(() => {
        setConfig({
            bloggerName: editBloggerName.trim() || '博主',
            bloggerAvatarUrl: sanitizeImageUrl(editBloggerAvatar),
            bloggerBgUrl: sanitizeImageUrl(editBloggerBg),
        });
        showToast('博主卡片设置已保存', 'success');
    });

    const handleAddTypingText = () => {
        const text = newTypingText.trim(); if (!text) return;
        requireToken(() => { addTypingText(text); setNewTypingText(''); showToast('文本已添加', 'success'); });
    };

    // ── Upload permission handlers ────────────────────────────────────────────
    const handleSaveUploadPermissions = () => requireToken(async (token) => {
        setPermsBusy(true);
        try {
            const res = await metaApi.setUploadPermissions(token, uploadPerms.userAllowed, uploadPerms.adminAllowed);
            if (res.data.statusCode === 0) {
                setUploadPerms(res.data.data);
                showToast('投稿权限设置已保存', 'success');
            } else {
                showToast(res.data.statusMessage || '保存失败', 'error');
            }
        } catch { showToast('保存失败', 'error'); }
        finally { setPermsBusy(false); }
    });

    const handleAddToBlacklist = () => {
        const uid = parseInt(blacklistInput.trim(), 10);
        if (isNaN(uid) || uid <= 0) { showToast('请输入有效的用户 UID', 'warning'); return; }
        requireToken(async (token) => {
            setPermsBusy(true);
            try {
                const res = await metaApi.addToBlacklist(token, uid);
                if (res.data.statusCode === 0) {
                    setUploadPerms(res.data.data);
                    setBlacklistInput('');
                    showToast(`UID ${uid} 已加入黑名单`, 'success');
                } else {
                    showToast(res.data.statusMessage || '操作失败', 'error');
                }
            } catch { showToast('操作失败', 'error'); }
            finally { setPermsBusy(false); }
        });
    };

    const handleRemoveFromBlacklist = (uid: number) => {
        requireToken(async (token) => {
            setPermsBusy(true);
            try {
                const res = await metaApi.removeFromBlacklist(token, uid);
                if (res.data.statusCode === 0) {
                    setUploadPerms(res.data.data);
                    showToast(`UID ${uid} 已移出黑名单`, 'success');
                } else {
                    showToast(res.data.statusMessage || '操作失败', 'error');
                }
            } catch { showToast('操作失败', 'error'); }
            finally { setPermsBusy(false); }
        });
    };

    // ── User role management handlers ─────────────────────────────────────────
    const handlePromoteUser = () => {
        const uid = parseInt(manageUid.trim(), 10);
        if (isNaN(uid) || uid <= 0) { showToast('请输入有效的用户 UID', 'warning'); return; }
        requireToken(async (token) => {
            setRoleBusy(true);
            try {
                const res = await metaApi.promoteUser(token, uid);
                if (res.data.statusCode === 0) {
                    showToast(res.data.data || `UID ${uid} 已设为管理员`, 'success');
                    setManageUid('');
                } else if (res.data.statusCode === 2002) {
                    showToast(`找不到 UID ${uid} 的用户`, 'error');
                } else {
                    showToast(res.data.statusMessage || '操作失败', 'error');
                }
            } catch { showToast('操作失败', 'error'); }
            finally { setRoleBusy(false); }
        });
    };

    const handleDemoteUser = () => {
        const uid = parseInt(manageUid.trim(), 10);
        if (isNaN(uid) || uid <= 0) { showToast('请输入有效的用户 UID', 'warning'); return; }
        requireToken(async (token) => {
            setRoleBusy(true);
            try {
                const res = await metaApi.demoteUser(token, uid);
                if (res.data.statusCode === 0) {
                    showToast(res.data.data || `UID ${uid} 已降级为普通用户`, 'success');
                    setManageUid('');
                } else if (res.data.statusCode === 2002) {
                    showToast(`找不到 UID ${uid} 的用户`, 'error');
                } else {
                    showToast(res.data.statusMessage || '操作失败', 'error');
                }
            } catch { showToast('操作失败', 'error'); }
            finally { setRoleBusy(false); }
        });
    };

    return (
        <>
            {pendingSave && (
                <ConfirmTokenDialog
                    onConfirmed={(token) => { pendingSave.fn(token); setPendingSave(null); }}
                    onCancel={() => setPendingSave(null)}
                />
            )}
            <Layout>
                <div className="container">
                    <div className={styles.page}>
                        <div className={styles.pageHeader}>
                            <h1 className={styles.pageTitle}>站点配置</h1>
                            <p className={styles.pageSubtitle}>自定义博客外观，配置存储于本地浏览器</p>
                        </div>

                    {/* Section: 网站图标 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>网站图标</h2>
                        <div className={styles.grid2}>
                            <div className={styles.fieldGroup}>
                                <label className={styles.label}>浏览器标签页图标（小图标）</label>
                                {safeFaviconUrl && (
                                    <img src={safeFaviconUrl} alt="favicon preview" className={styles.iconPreview} />
                                )}
                                <input className={styles.input} type="text" value={editFaviconUrl}
                                    onChange={(e) => setEditFaviconUrl(e.target.value)}
                                    placeholder="输入图片 URL 或上传文件" />
                                <div className={styles.btnRow}>
                                    <button className={styles.outlineBtn} onClick={() => faviconFileRef.current?.click()}>上传文件</button>
                                    <input ref={faviconFileRef} type="file" accept="image/*" className={styles.hidden}
                                        onChange={handleFaviconFileChange} />
                                </div>
                            </div>

                            <div className={styles.fieldGroup}>
                                <label className={styles.label}>顶栏图标（大图标）</label>
                                {safeLogoUrl && (
                                    <img src={safeLogoUrl} alt="logo preview" className={styles.logoPreview} />
                                )}
                                <input className={styles.input} type="text" value={editLogoUrl}
                                    onChange={(e) => setEditLogoUrl(e.target.value)}
                                    placeholder="输入图片 URL 或上传文件" />
                                <div className={styles.btnRow}>
                                    <button className={styles.outlineBtn} onClick={() => logoFileRef.current?.click()}>上传文件</button>
                                    <input ref={logoFileRef} type="file" accept="image/*" className={styles.hidden}
                                        onChange={handleLogoFileChange} />
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
                                    <img src={sanitizeImageUrl(url)} alt={`hero-${i}`} className={styles.imageThumb} />
                                    <button className={styles.removeBtn} onClick={() => removeHeroImage(i)} title="移除">✕</button>
                                </div>
                            ))}
                            {heroImages.length === 0 && <p className={styles.emptyHint}>暂无背景图，将使用默认渐变背景</p>}
                        </div>

                        <div className={styles.addRow}>
                            <input className={styles.input} type="text" value={newHeroImageUrl}
                                onChange={(e) => setNewHeroImageUrl(e.target.value)}
                                placeholder="输入图片 URL"
                                onKeyDown={(e) => e.key === 'Enter' && handleAddHeroImageByUrl()} />
                            <button className={styles.outlineBtn} onClick={handleAddHeroImageByUrl}>添加 URL</button>
                            <button className={styles.outlineBtn} onClick={() => heroFileRef.current?.click()}>上传文件</button>
                            <input ref={heroFileRef} type="file" accept="image/*" className={styles.hidden}
                                onChange={handleHeroFileChange} />
                        </div>

                        <div className={styles.fieldRow}>
                            <label className={styles.label}>轮播间隔（秒）</label>
                            <input className={`${styles.input} ${styles.inputSm}`} type="number" min={1}
                                value={editInterval} onChange={(e) => setEditInterval(e.target.value)} />
                            <button className={styles.outlineBtn} onClick={handleSaveHeroInterval}>保存</button>
                        </div>
                    </section>

                    {/* Section: 博主卡片 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>博主卡片</h2>
                        <div className={styles.grid2}>
                            <div className={styles.fieldGroup}>
                                <label className={styles.label}>博主名称</label>
                                <input className={styles.input} type="text" value={editBloggerName}
                                    onChange={(e) => setEditBloggerName(e.target.value)} placeholder="博主" />
                            </div>

                            <div className={styles.fieldGroup}>
                                <label className={styles.label}>博主头像</label>
                                {safeAvatarUrl && <img src={safeAvatarUrl} alt="avatar preview" className={styles.avatarPreview} />}
                                <input className={styles.input} type="text" value={editBloggerAvatar}
                                    onChange={(e) => setEditBloggerAvatar(e.target.value)} placeholder="输入图片 URL 或上传文件" />
                                <div className={styles.btnRow}>
                                    <button className={styles.outlineBtn} onClick={() => avatarFileRef.current?.click()}>上传头像</button>
                                    <input ref={avatarFileRef} type="file" accept="image/*" className={styles.hidden}
                                        onChange={handleAvatarFileChange} />
                                </div>
                            </div>

                            <div className={styles.fieldGroup}>
                                <label className={styles.label}>博主卡片背景图</label>
                                {safeBloggerBgUrl && <img src={safeBloggerBgUrl} alt="bg preview" className={styles.bgPreview} />}
                                <input className={styles.input} type="text" value={editBloggerBg}
                                    onChange={(e) => setEditBloggerBg(e.target.value)} placeholder="输入图片 URL 或上传文件" />
                                <div className={styles.btnRow}>
                                    <button className={styles.outlineBtn} onClick={() => bloggerBgFileRef.current?.click()}>上传背景图</button>
                                    <input ref={bloggerBgFileRef} type="file" accept="image/*" className={styles.hidden}
                                        onChange={handleBloggerBgFileChange} />
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
                                    <button className={styles.removeBtn} onClick={() => removeTypingText(i)} title="删除">✕</button>
                                </div>
                            ))}
                            {typingTexts.length === 0 && <p className={styles.emptyHint}>文本池为空，将使用默认文本</p>}
                        </div>

                        <div className={styles.addRow}>
                            <input className={styles.input} type="text" value={newTypingText}
                                onChange={(e) => setNewTypingText(e.target.value)}
                                placeholder="输入新文本..."
                                onKeyDown={(e) => e.key === 'Enter' && handleAddTypingText()} />
                            <button className={styles.primaryBtn} onClick={handleAddTypingText}>添加</button>
                        </div>
                    </section>

                    {/* Section: 投稿权限设置 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>投稿权限设置</h2>
                        <p className={styles.hint}>控制哪些用户组可以上传文章，以及对特定用户的细粒度禁止。</p>

                        <div className={styles.toggleGroup}>
                            <label className={styles.toggleLabel}>
                                <input
                                    type="checkbox"
                                    className={styles.checkbox}
                                    checked={uploadPerms.userAllowed}
                                    onChange={(e) => setUploadPerms(p => ({...p, userAllowed: e.target.checked}))}
                                />
                                <span>允许普通用户上传文章</span>
                            </label>
                            <label className={styles.toggleLabel}>
                                <input
                                    type="checkbox"
                                    className={styles.checkbox}
                                    checked={uploadPerms.adminAllowed}
                                    onChange={(e) => setUploadPerms(p => ({...p, adminAllowed: e.target.checked}))}
                                />
                                <span>允许管理员上传文章</span>
                            </label>
                        </div>
                        <button className={styles.primaryBtn} onClick={handleSaveUploadPermissions} disabled={permsBusy}>
                            {permsBusy ? '保存中…' : '保存权限设置'}
                        </button>

                        <h3 className={styles.subSectionTitle}>上传黑名单</h3>
                        <p className={styles.hint}>在用户组权限基础上，进一步禁止特定用户上传（无论其所在组是否被允许）。</p>

                        {uploadPerms.blacklist.length > 0 ? (
                            <div className={styles.textList}>
                                {uploadPerms.blacklist.map((uid) => (
                                    <div key={uid} className={styles.textItem}>
                                        <span className={styles.textContent}>UID: {uid}</span>
                                        <button
                                            className={styles.removeBtn}
                                            onClick={() => handleRemoveFromBlacklist(uid)}
                                            disabled={permsBusy}
                                            title="移出黑名单"
                                        >✕</button>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <p className={styles.emptyHint}>黑名单为空</p>
                        )}

                        <div className={styles.addRow}>
                            <input
                                className={`${styles.input} ${styles.inputSm}`}
                                type="number"
                                min={1}
                                value={blacklistInput}
                                onChange={(e) => setBlacklistInput(e.target.value)}
                                placeholder="输入用户 UID"
                                onKeyDown={(e) => e.key === 'Enter' && handleAddToBlacklist()}
                            />
                            <button className={styles.outlineBtn} onClick={handleAddToBlacklist} disabled={permsBusy}>
                                加入黑名单
                            </button>
                        </div>
                    </section>

                    {/* Section: 用户组管理 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>用户组管理</h2>
                        <p className={styles.hint}>通过站长口令直接调整指定用户的角色。不是管理员的用户均为普通用户。</p>

                        <div className={styles.addRow}>
                            <input
                                className={`${styles.input} ${styles.inputSm}`}
                                type="number"
                                min={1}
                                value={manageUid}
                                onChange={(e) => setManageUid(e.target.value)}
                                placeholder="输入用户 UID"
                            />
                            <button className={styles.primaryBtn} onClick={handlePromoteUser} disabled={roleBusy || !manageUid.trim()}>
                                {roleBusy ? '操作中…' : '设为管理员'}
                            </button>
                            <button className={styles.outlineBtn} onClick={handleDemoteUser} disabled={roleBusy || !manageUid.trim()}>
                                {roleBusy ? '操作中…' : '降为普通用户'}
                            </button>
                        </div>
                    </section>

                    </div>
                </div>
            </Layout>
        </>
    );
};

export default MetaConfigPage;
