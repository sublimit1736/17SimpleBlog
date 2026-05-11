import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';
import 'highlight.js/styles/github-dark.css';
import styles from './MarkdownRenderer.module.css';

interface Props {
  content: string;
  contentType?: string;
}

const MarkdownRenderer: React.FC<Props> = ({ content, contentType = 'MARKDOWN' }) => {
  if (contentType === 'PLAIN_TEXT') {
    return <pre className={styles.plainText}>{content}</pre>;
  }

  return (
    <div className={styles.markdown}>
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeHighlight]}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
};

export default MarkdownRenderer;
