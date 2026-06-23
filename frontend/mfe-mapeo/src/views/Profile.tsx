import React, { useEffect, useState } from 'react';
import { useAuthState, useAuthDispatch } from '../store/AuthContext';
import { getUser, updateUser } from '../services/api';

export default function Profile() {
  const { user, token } = useAuthState();
  const dispatch = useAuthDispatch();
  const [username, setUsername] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!user || !token) return;
    (async () => {
      setLoading(true);
      const u = await getUser(user.id, token);
      if (u) setUsername(u.username);
      setLoading(false);
    })();
  }, [user, token]);

  async function save() {
    if (!user || !token) return;
    const updated = await updateUser(user.id, { username }, token);
    if (updated) {
      dispatch({ type: 'update_user', payload: { username: updated.username } });
      alert('Perfil actualizado correctamente');
    }
  }

  if (!user) return null;

  return (
    <div className="container mt-4" style={{ maxWidth: 720 }}>
      <h3>Perfil</h3>
      {loading ? <div>Cargando...</div> : (
        <div>
          <div className="mb-3">
            <label className="form-label">Usuario</label>
            <input className="form-control" value={username} onChange={e => setUsername(e.target.value)} />
          </div>
          <button className="btn btn-success" onClick={save}>Guardar</button>
        </div>
      )}
    </div>
  );
}
