import React, { useState } from 'react';
import api from '../api';

export const AuthModals = ({ type, onClose, onAuthSuccess }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [region, setRegion] = useState('');
  const [comuna, setComuna] = useState('');
  const [correo, setCorreo] = useState('');
  const [telefono, setTelefono] = useState('');
  const [rol, setRol] = useState('USUARIO');
  const [error, setError] = useState('');

  const isLogin = type === 'login';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    try {
      const endpoint = isLogin ? '/usuarios/auth/login' : '/usuarios/auth/register';
      const payload = isLogin 
        ? { username, password }
        : { username, password, region, comuna, correo, telefono, rol };

      const res = await api.post(endpoint, payload);
      
      const token = res.data.token;
      const user = res.data.user;
      
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      
      onAuthSuccess(user);
    } catch (err) {
      console.error(err);
      setError(isLogin ? 'Credenciales incorrectas' : 'Hubo un error al registrar el usuario');
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxWidth: isLogin ? '400px' : '500px' }}>
        <h2>{isLogin ? 'Iniciar Sesión' : 'Registrarse'}</h2>
        
        {error && <div style={{color: '#ff4757', marginBottom: '10px'}}>{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Usuario</label>
            <input 
              type="text" 
              value={username} 
              onChange={e => setUsername(e.target.value)} 
              required 
            />
          </div>
          <div className="form-group">
            <label>Contraseña</label>
            <input 
              type="password" 
              value={password} 
              onChange={e => setPassword(e.target.value)} 
              required 
            />
          </div>

          {!isLogin && (
            <>
              <div className="form-row" style={{ display: 'flex', gap: '10px' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label>Región</label>
                  <input 
                    type="text" 
                    value={region} 
                    onChange={e => setRegion(e.target.value)} 
                  />
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                  <label>Comuna</label>
                  <input 
                    type="text" 
                    value={comuna} 
                    onChange={e => setComuna(e.target.value)} 
                  />
                </div>
              </div>
              <div className="form-group">
                <label>Correo Electrónico</label>
                <input 
                  type="email" 
                  value={correo} 
                  onChange={e => setCorreo(e.target.value)} 
                />
              </div>
              <div className="form-group">
                <label>Número Telefónico</label>
                <input 
                  type="text" 
                  value={telefono} 
                  onChange={e => setTelefono(e.target.value)} 
                />
              </div>
              <div className="form-group">
                <label>Rol en la Plataforma</label>
                <select 
                  value={rol} 
                  onChange={e => setRol(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '10px',
                    borderRadius: '8px',
                    border: '1px solid rgba(255, 255, 255, 0.1)',
                    backgroundColor: 'rgba(0, 0, 0, 0.4)',
                    color: 'white',
                    outline: 'none'
                  }}
                >
                  <option value="USUARIO">Usuario Común</option>
                  <option value="BRIGADISTA">Brigadista (Control de Incendios)</option>
                  <option value="ADMINISTRADOR">Administrador</option>
                </select>
              </div>
            </>
          )}
          
          <div className="modal-actions" style={{ marginTop: '20px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancelar</button>
            <button type="submit" className="btn">{isLogin ? 'Entrar' : 'Crear Cuenta'}</button>
          </div>
        </form>
      </div>
    </div>
  );
};
