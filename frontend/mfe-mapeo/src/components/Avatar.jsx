import React from 'react';

export default function Avatar({ name, size = 36 }) {
  const letter = name ? name.charAt(0).toUpperCase() : '?';
  const style = {
    width: size,
    height: size,
    borderRadius: '50%',
    background: '#6c757d',
    color: 'white',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
  };
  return <div style={style}>{letter}</div>;
}
