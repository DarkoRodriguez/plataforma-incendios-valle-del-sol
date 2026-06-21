import React from 'react';

interface AvatarProps {
  name?: string;
  size?: number;
}

export default function Avatar({ name, size = 36 }: AvatarProps) {
  const letter = name ? name.charAt(0).toUpperCase() : '?';
  const style: React.CSSProperties = {
    width: size,
    height: size,
    borderRadius: '50%',
    background: 'linear-gradient(135deg, #6c5ce7, #a29bfe)',
    color: 'white',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontWeight: 'bold',
    fontSize: `${size / 2.2}px`,
    boxShadow: '0 2px 8px rgba(108, 92, 231, 0.4)',
  };
  return <div style={style}>{letter}</div>;
}
