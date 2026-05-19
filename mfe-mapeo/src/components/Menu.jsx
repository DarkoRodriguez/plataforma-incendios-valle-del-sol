import React from 'react';
import Avatar from './Avatar';
import { useAuthState, useAuthDispatch } from '../store/AuthContext';

export default function Menu() {
  const { user } = useAuthState();
  const dispatch = useAuthDispatch();

  function logout() {
    dispatch({ type: 'logout' });
  }

  return (
    <nav className="navbar navbar-light bg-light">
      <div className="container-fluid">
        <a className="navbar-brand" href="#">Valle del Sol - Plataforma de Incendios</a>
        <div>
          {user ? (
            <div className="d-flex align-items-center">
              <div className="me-2">{user.username}</div>
              <Avatar name={user.username} />
              <button className="btn btn-link ms-3" onClick={logout}>Cerrar sesión</button>
            </div>
          ) : null}
        </div>
      </div>
    </nav>
  );
}
