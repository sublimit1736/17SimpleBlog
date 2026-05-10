import React, {useEffect, useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import {articlesApi} from '../api/articles';
import {type Article, type ContentType} from '../types';
import {useToast} from '../components/ui/toastContext';
import Layout from '../components/layout/Layout';
import Button from '../components/ui/Button';
import styles from './WriteArticlePage.module.css';

const CONTENT_TYPES: { value: ContentType; label: string }[] = [
    {value: 'MARKDOWN', label: 'Markdown'},
    {value: 'PLAIN_TEXT', label: '纯文本'},
    {value: 'HTML', label: 'HTML'},
];

const WriteArticlePage: React.FC = () => {
    const {id} = useParams<{ id?: string }>();
    const isEdit = Boolean(id);
    const navigate = useNavigate();
    const {showToast} = useToast();

    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [tags, setTags] = useState('');
    const [contentType, setContentType] = useState<ContentType>('MARKDOWN');
    const [loading, setLoading] = useState(false);
    const [fetching, setFetching] = useState(isEdit);

    useEffect(() => {
        if (!isEdit) return;
        (async () => {
            try {
                const res = await articlesApi.getById(Number(id));
                if (res.data.statusCode === 0) {
                    const article: Article = res.data.data;
                    setTitle(article.title);
                    setContent(article.content ?? '');
                    setTags(article.tags ?? '');
                    setContentType(article.contentType || 'MARKDOWN');
                }
            } catch {
                showToast('加载文章失败', 'error');
            } finally {
                setFetching(false);
            }
        })();
    }, [id, isEdit, showToast]);

    const handleSubmit = async (publish: boolean) => {
        if (!title.trim()) {
            showToast('请输入标题', 'warning');
            return;
        }
        if (!content.trim()) {
            showToast('请输入内容', 'warning');
            return;
        }
        setLoading(true);
        const tagArr = tags.split(',').map((t) => t.trim()).filter(Boolean);
        const payload = {title: title.trim(), content, tags: tagArr, contentType};
        try {
            let res;
            if (isEdit) {
                res = await articlesApi.update(Number(id), payload);
            } else {
                res = publish
                    ? await articlesApi.publishNew(payload)
                    : await articlesApi.saveDraft(payload);
            }
            if (res.data.statusCode === 0) {
                showToast(publish ? '发布成功' : '草稿已保存', 'success');
                navigate(`/article/${res.data.data.id}`);
            } else {
                showToast(res.data.statusMessage || '操作失败', 'error');
            }
        } catch {
            showToast('操作失败', 'error');
        } finally {
            setLoading(false);
        }
    };

    if (fetching) return <Layout>
        <div className="container" style={{padding: '40px 0'}}>加载中...</div>
    </Layout>;

    return (
        <Layout>
            <div className="container">
                <div className={styles.editor}>
                    <h1 className={styles.heading}>{isEdit ? '编辑文章' : '写文章'}</h1>

                    <input
                        className={styles.titleInput}
                        placeholder="文章标题..."
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                    />

                    <div className={styles.metaRow}>
                        <div className={styles.field}>
                            <label className={styles.label}>内容格式</label>
                            <select
                                className={styles.select}
                                value={contentType}
                                onChange={(e) => setContentType(e.target.value as ContentType)}
                            >
                                {CONTENT_TYPES.map((t) => (
                                    <option key={t.value} value={t.value}>{t.label}</option>
                                ))}
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

                    <textarea
                        className={styles.contentArea}
                        placeholder="开始写作..."
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                    />

                    <div className={styles.actions}>
                        <Button variant="secondary" onClick={() => navigate(-1)} disabled={loading}>取消</Button>
                        {!isEdit && (
                            <Button variant="outline" onClick={() => handleSubmit(false)} loading={loading}>
                                💾 存草稿
                            </Button>
                        )}
                        <Button onClick={() => handleSubmit(true)} loading={loading}>
                            🚀 {isEdit ? '更新发布' : '发布'}
                        </Button>
                    </div>
                </div>
            </div>
        </Layout>
    );
};

export default WriteArticlePage;
