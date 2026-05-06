import React, { useEffect, useRef } from 'react';
import styles from './HeroBanner.module.css';

const TYPING_STRINGS = [
  '分享技术，记录生活',
  '在这里找到你感兴趣的内容',
  'Share ideas, explore the world',
];

const HeroBanner: React.FC = () => {
  const typingRef = useRef<HTMLSpanElement>(null);
  const indexRef = useRef(0);
  const charRef = useRef(0);
  const deletingRef = useRef(false);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    const el = typingRef.current;
    if (!el) return;

    const tick = () => {
      const str = TYPING_STRINGS[indexRef.current];
      if (!deletingRef.current) {
        charRef.current++;
        el.textContent = str.substring(0, charRef.current);
        if (charRef.current === str.length) {
          deletingRef.current = true;
          timerRef.current = setTimeout(tick, 1800);
          return;
        }
      } else {
        charRef.current--;
        el.textContent = str.substring(0, charRef.current);
        if (charRef.current === 0) {
          deletingRef.current = false;
          indexRef.current = (indexRef.current + 1) % TYPING_STRINGS.length;
        }
      }
      timerRef.current = setTimeout(tick, deletingRef.current ? 50 : 90);
    };

    timerRef.current = setTimeout(tick, 600);
    return () => { if (timerRef.current) clearTimeout(timerRef.current); };
  }, []);

  const handleScrollDown = () => {
    const heroEl = document.getElementById('hero-banner');
    if (heroEl) {
      window.scrollTo({ top: heroEl.offsetHeight, behavior: 'smooth' });
    }
  };

  return (
    <div id="hero-banner" className={styles.hero}>
      {/* gradient overlay */}
      <div className={styles.overlay} />

      {/* center content */}
      <div className={styles.center}>
        <div className={styles.logoIcon}>✦</div>
        <h1 className={styles.title}>17SimpleBlog</h1>
        <p className={styles.subtitle}>
          <span ref={typingRef} />
          <span className={styles.cursor}>|</span>
        </p>
      </div>

      {/* wave */}
      <div className={styles.waves}>
        <svg viewBox="0 24 150 28" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <path id="wave-path" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z" />
          </defs>
          <g className={styles.waveMover}>
            <use href="#wave-path" x="48" y="0" className={styles.waveLayer1} />
            <use href="#wave-path" x="48" y="3" className={styles.waveLayer2} />
            <use href="#wave-path" x="48" y="5" className={styles.waveLayer3} />
            <use href="#wave-path" x="48" y="7" className={styles.waveLayer4} />
          </g>
        </svg>
      </div>

      {/* scroll arrow */}
      <button className={styles.scrollArrow} onClick={handleScrollDown} aria-label="向下滚动">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
        </svg>
      </button>
    </div>
  );
};

export default HeroBanner;
