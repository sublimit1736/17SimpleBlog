import React from 'react';

interface AvatarProps {
  src?: string;
  alt?: string;
  size?: number;
  username?: string;
}

const Avatar: React.FC<AvatarProps> = ({ src, alt, size = 36, username }) => {
  const initials = username ? username.charAt(0).toUpperCase() : '?';

  const style: React.CSSProperties = {
    width: size,
    height: size,
    borderRadius: '50%',
    overflow: 'hidden',
    flexShrink: 0,
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: size * 0.4,
    fontWeight: 600,
    background: 'var(--color-gradient)',
    color: '#fff',
    userSelect: 'none',
  };

  if (src) {
    return (
      <img
        src={src}
        alt={alt || username || 'avatar'}
        style={{ ...style, objectFit: 'cover' }}
      />
    );
  }

  return <span style={style}>{initials}</span>;
};

export default Avatar;
