import React, { useState } from 'react';
import { updateUser, UserDTO } from '../services/api';
import { CHILE_REGIONS, COMMUNES_BY_REGION, RegionName } from '../data/chileRegions';

interface ProfileModalProps {
  user: UserDTO;
  onClose: () => void;
  onUpdateSuccess: (user: UserDTO) => void;
}

export const ProfileModal: React.FC<ProfileModalProps> = ({ user, onClose, onUpdateSuccess }) => {
  const [username, setUsername] = useState(user.username || '');
  const [password, setPassword] = useState('');
  const [region, setRegion] = useState<RegionName | ''>((user.region || '') as RegionName | '');
  const [commune, setCommune] = useState(user.commune || '');
  const [email, setEmail] = useState(user.email || '');
  const [phone, setPhone] = useState(user.phone || '');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const communes = region ? COMMUNES_BY_REGION[region] : [];

  const displayRole = user.role === 'ADMINISTRATOR'
    ? 'Administrador'
    : user.role === 'BRIGADIST'
      ? 'Brigadista'
      : 'Usuario estándar';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const token = localStorage.getItem('token');
    if (!token) {
      setError('Sesión expirada. Por favor, inicia sesión nuevamente.');
      return;
    }

    try {
      const payload: Partial<UserDTO> & { password?: string } = {
        username,
        region,
        commune,
        email,
        phone
      };

      if (password.trim() !== '') {
        payload.password = password;
      }

      const updatedUser = await updateUser(user.id, payload, token);
      if (updatedUser) {
        localStorage.setItem('user', JSON.stringify(updatedUser));
        setSuccess('Perfil actualizado correctamente');
        setTimeout(() => {
          onUpdateSuccess(updatedUser);
        }, 1500);
      } else {
        setError('No se pudo actualizar el perfil');
      }
    } catch (err) {
      console.error("Error updating profile", err);
      setError('Ocurrió un error al actualizar el perfil');
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxWidth: '500px' }}>
        <h2>Editar perfil</h2>
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}
        
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
            <label>Nueva contraseña (dejar en blanco para conservar la actual)</label>
            <input 
              type="password" 
              value={password} 
              onChange={e => setPassword(e.target.value)} 
              placeholder="••••••••"
            />
          </div>

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
            <label>Rol (no editable)</label>
            <input 
              type="text" 
              value={displayRole} 
              disabled 
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '8px',
                border: '1px solid rgba(255, 255, 255, 0.05)',
                backgroundColor: 'rgba(255, 255, 255, 0.05)',
                color: 'rgba(255, 255, 255, 0.5)',
                cursor: 'not-allowed'
              }}
            />
          </div>
          
          <div className="modal-actions" style={{ marginTop: '20px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancelar</button>
            <button type="submit" className="btn btn-primary">Guardar cambios</button>
          </div>
        </form>
      </div>
    </div>
  );
};
