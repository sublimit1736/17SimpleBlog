import React, {useRef, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {articlesApi} from '../api/articles';
import {useToast} from '../components/ui/toastContext';
import Layout from '../components/layout/Layout';
import Button from '../components/ui/Button';
import {usePageTitle} from '../hooks/usePageTitle';
import styles from './WriteArticlePage.module.css';

type ContentType = 'MARKDOWN' | 'PLAIN_TEXT';

const UploadArticlePage: React.FC = () => {
    const navigate = useNavigate();
    const {showToast} = useToast();
    usePageTitle('上传文章');

    const [title, setTitle] = useState('');
    const [tags, setTags] = useState('');
    const [contentType, setContentType] = useState<ContentType>('MARKDOWN');
    const [contentFile, setContentFile] = useState<File | null>(null);
    const [imageFiles, setImageFiles] = useState<File[]>([]);
    const [loading, setLoading] = useState(false);

    const contentRef = useRef<HTMLInputElement>(null);
    const imagesRef = useRef<HTMLInputElement>(null);

    const handleContentFile = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0] ?? null;
        setContentFile(file);
    };

    const handleImageFiles = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files) return;
        setImageFiles(Array.from(e.target.files));
    };

    const removeImage = (index: number) => {
        setImageFiles((prev) => prev.filter((_, i) => i !== index));
    };

    const handleSubmit = async () => {
        if (!title.trim()) {
            showToast('请输入文章标题', 'warning');
            return;
        }
        if (!contentFile) {
            showToast('请选择文章正文文件', 'warning');
            return;
        }

        setLoading(true);
        try {
            const formData = new FormData();
            formData.append('title', title.trim());
            formData.append('contentType', contentType);
            if (tags.trim()) formData.append('tags', tags.trim());
            formData.append('content', contentFile, contentFile.name);
            imageFiles.forEach((img) => formData.append('images', img, img.name));

            const res = await articlesApi.upload(formData);
            if (res.data.statusCode === 0) {
                showToast('文章发布成功', 'success');
                navigate(`/article/${res.data.data.id}`);
            } else {
                showToast(res.data.statusMessage || '发布失败', 'error');
            }
        } catch {
            showToast('发布失败，请稍后重试', 'error');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Layout>
            <div className="container">
                <div className={styles.editor}>
                    <h1 className={styles.heading}>上传文章</h1>

                    {/* Title */}
                    <input
                        className={styles.titleInput}
                        placeholder="文章标题..."
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        maxLength={100}
                    />

                    {/* Meta row */}
                    <div className={styles.metaRow}>
                        <div className={styles.field}>
                            <label className={styles.label}>内容格式</label>
                            <select
                                className={styles.select}
                                value={contentType}
                                onChange={(e) => setContentType(e.target.value as ContentType)}
                            >
                                <option value="MARKDOWN">Markdown (.md)</option>
                                <option value="PLAIN_TEXT">纯文本 (.txt)</option>
                            </select>
                        </div>

                        <div className={styles.field} style={{flex: 1}}>
                            <label className={styles.label}>标签（英文逗号分隔）</label>
                            <input
                                className={styles.input}
                                placeholder="例：React, TypeScript, Web"
                                value={tags}
                                onChange={(e) => setTags(e.target.value)}
                            />
                        </div>
                    </div>

                    {/* Content file */}
                    <div className={styles.field}>
                        <label className={styles.label}>
                            文章正文文件 <span style={{color: 'var(--color-danger)'}}>*</span>
                            <span style={{
                                fontWeight: 400,
                                color: 'var(--color-text-muted)',
                                marginLeft: 8
                            }}>（.md 或 .txt，UTF-8 编码）</span>
                        </label>
                        <div style={{display: 'flex', alignItems: 'center', gap: 12}}>
                            <Button
                                variant="outline"
                                onClick={() => contentRef.current?.click()}
                                disabled={loading}
                            >
                                选择文件
                            </Button>
                            <span style={{color: 'var(--color-text-secondary)', fontSize: '0.9rem'}}>
                                {contentFile ? contentFile.name : '未选择文件'}
                            </span>
                        </div>
                        <input
                            ref={contentRef}
                            type="file"
                            accept=".md,.txt,text/plain,text/markdown"
                            style={{display: 'none'}}
                            onChange={handleContentFile}
                        />
                    </div>

                    {/* Image files */}
                    <div className={styles.field}>
                        <label className={styles.label}>
                            配图文件
                            <span style={{
                                fontWeight: 400,
                                color: 'var(--color-text-muted)',
                                marginLeft: 8
                            }}>（可选，多选；图片只能在本文章中引用）</span>
                        </label>
                        <Button
                            variant="outline"
                            onClick={() => imagesRef.current?.click()}
                            disabled={loading}
                        >
                            选择配图
                        </Button>
                        <input
                            ref={imagesRef}
                            type="file"
                            accept="image/*"
                            multiple
                            style={{display: 'none'}}
                            onChange={handleImageFiles}
                        />
                        {imageFiles.length > 0 && (
                            <div style={{marginTop: 10, display: 'flex', flexWrap: 'wrap', gap: 8}}>
                                {imageFiles.map((img, i) => (
                                    <div key={i} style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: 6,
                                        background: 'var(--color-bg)',
                                        border: '1px solid var(--color-border)',
                                        borderRadius: 'var(--radius-sm)',
                                        padding: '4px 10px',
                                        fontSize: '0.85rem',
                                    }}>
                                        <span>{img.name}</span>
                                        <button
                                            onClick={() => removeImage(i)}
                                            style={{
                                                background: 'none',
                                                border: 'none',
                                                cursor: 'pointer',
                                                color: 'var(--color-danger)',
                                                fontWeight: 700,
                                                padding: 0,
                                                lineHeight: 1,
                                            }}
                                            aria-label={`移除 ${img.name}`}
                                        >×</button>
                                    </div>
                                ))}
                            </div>
                        )}
                        {imageFiles.length > 0 && (
                            <p style={{fontSize: '0.8rem', color: 'var(--color-text-muted)', marginTop: 6}}>
                                提示：在 Markdown 正文中使用 <code>![描述](文件名.jpg)</code> 引用配图，系统会自动替换为正确的地址。
                            </p>
                        )}
                    </div>

                    {/* Actions */}
                    <div className={styles.actions}>
                        <Button variant="secondary" onClick={() => navigate(-1)} disabled={loading}>
                            取消
                        </Button>
                        <Button onClick={handleSubmit} loading={loading}>
                            发布文章
                        </Button>
                    </div>
                </div>
            </div>
        </Layout>
    );
};

export default UploadArticlePage;
