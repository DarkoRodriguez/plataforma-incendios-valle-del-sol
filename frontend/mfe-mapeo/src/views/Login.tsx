import React, { useState } from 'react';
import { useAuthDispatch } from '../store/AuthContext';
import { login } from '../services/api';

export default function Login() {
  const dispatch = useAuthDispatch();
  const [username, setUsername] = useState('demo');
  const [password, setPassword] = useState('demo');
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    const data = await login(username, password);
    if (!data) {
      setError('Credenciales inválidas');
      return;
    }
    dispatch({ type: 'login', payload: { user: data.user, token: data.token } });
  }

  return (
    <div className="container mt-5" style={{ maxWidth: 420 }}>
      <h3>Iniciar sesión</h3>
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label className="form-label">Usuario</label>
          <input className="form-control" value={username} onChange={e => setUsername(e.target.value)} />
        </div>
        <div className="mb-3">
          <label className="form-label">Contraseña</label>
          <input type="password" className="form-control" value={password} onChange={e => setPassword(e.target.value)} />
        </div>
        {error && <div className="alert alert-danger">{error}</div>}
        <button className="btn btn-primary" type="submit">Ingresar</button>
      </form>
    </div>
  );
}
