import React from 'react';
import Avatar from './Avatar';
import { useAuthState, useAuthDispatch } from '../store/AuthContext';

export default function Menu() {
  const { user } = useAuthState();
  const dispatch = useAuthDispatch();

  function logout() {
    dispatch({ type: 'logout' });
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.dispatchEvent(new Event('auth-change'));
  }

  return (
    <nav className="navbar navbar-light bg-light">
      <div className="container-fluid">
        <span className="navbar-brand">Valle del Sol - Plataforma de Incendios</span>
        <div>
          {user ? (
            <div className="d-flex align-items-center">
              <span className="me-2">{user.username}</span>
              <Avatar name={user.username} />
              <button className="btn btn-link ms-3" onClick={logout}>Cerrar sesión</button>
            </div>
          ) : null}
        </div>
      </div>
    </nav>
  );
}
