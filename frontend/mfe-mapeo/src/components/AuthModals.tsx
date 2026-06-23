import React, { useState } from 'react';
import { login, register, UserDTO } from '../services/api';
import { CHILE_REGIONS, COMMUNES_BY_REGION, RegionName } from '../data/chileRegions';

interface AuthModalsProps {
  type: 'login' | 'register';
  onClose: () => void;
  onAuthSuccess: (user: UserDTO) => void;
}

export const AuthModals: React.FC<AuthModalsProps> = ({ type, onClose, onAuthSuccess }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [region, setRegion] = useState<RegionName | ''>('');
  const [commune, setCommune] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [role, setRole] = useState('USER');
  const [error, setError] = useState('');

  const isLogin = type === 'login';
  const communes = region ? COMMUNES_BY_REGION[region] : [];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    try {
      if (isLogin) {
        const data = await login(username, password);
        if (data && data.token && data.user) {
          localStorage.setItem('token', data.token);
          localStorage.setItem('user', JSON.stringify(data.user));
          onAuthSuccess(data.user);
          onClose();
        } else {
          setError('Usuario o contraseña inválidos');
        }
      } else {
        const payload = { username, password, region, commune, email, phone, role };
        const success = await register(payload);
        if (success) {
          // Log in automatically after registration
          const data = await login(username, password);
          if (data && data.token && data.user) {
            localStorage.setItem('token', data.token);
            localStorage.setItem('user', JSON.stringify(data.user));
            onAuthSuccess(data.user);
            onClose();
          } else {
            setError('Registro exitoso, pero el ingreso falló. Inicia sesión manualmente.');
          }
        } else {
          setError('No se pudo crear la cuenta. El usuario ya existe o los datos son incorrectos.');
        }
      }
    } catch (err) {
      console.error(err);
      setError(isLogin ? 'Error al iniciar sesión' : 'Error al registrarse');
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxWidth: isLogin ? '400px' : '500px' }}>
        <h2>{isLogin ? 'Iniciar sesión' : 'Crear cuenta'}</h2>
        {error && <div className="error-message">{error}</div>}
        
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
                  <select
                    value={region}
                    onChange={e => {
                      const selected = e.target.value as RegionName | '';
                      setRegion(selected);
                      setCommune('');
                    }}
                    required
                  >
                    <option value="">Seleccione región</option>
                    {CHILE_REGIONS.map(r => (
                      <option key={r} value={r}>{r}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                  <label>Comuna</label>
                  <select
                    value={commune}
                    onChange={e => setCommune(e.target.value)}
                    disabled={!region}
                    required
                  >
                    <option value="">Seleccione comuna</option>
                    {communes.map(c => (
                      <option key={c} value={c}>{c}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label>Correo electrónico</label>
                <input 
                  type="email" 
                  value={email} 
                  onChange={e => setEmail(e.target.value)} 
                />
              </div>
              <div className="form-group">
                <label>Teléfono</label>
                <input 
                  type="text" 
                  value={phone} 
                  onChange={e => setPhone(e.target.value)} 
                />
              </div>
              <div className="form-group">
                <label>Rol en la plataforma</label>
                <select 
                  value={role} 
                  onChange={e => setRole(e.target.value)}
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
                  <option value="USER">Usuario estándar</option>
                  <option value="BRIGADIST">Brigadista</option>
                  <option value="ADMINISTRATOR">Administrador</option>
                </select>
              </div>
            </>
          )}
          
          <div className="modal-actions" style={{ marginTop: '20px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancelar</button>
            <button type="submit" className="btn btn-primary">{isLogin ? 'Ingresar' : 'Registrar'}</button>
          </div>
        </form>
      </div>
    </div>
  );
};
