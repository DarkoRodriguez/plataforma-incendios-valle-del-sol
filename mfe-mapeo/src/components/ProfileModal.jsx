import React, { useState } from 'react';
import api from '../api';

/**
 * Componente de Modal de Perfil que permite a los usuarios autenticados
 * actualizar su información personal y credenciales de acceso.
 *
 * @param {Object} props
 * @param {Object} props.user - Objeto del usuario autenticado actualmente
 * @param {function} props.onClose - Manejador para cerrar el modal
 * @param {function} props.onUpdateSuccess - Callback al guardar con éxito los cambios
 */
export const ProfileModal = ({ user, onClose, onUpdateSuccess }) => {
  // Inicialización de estados con los datos de sesión del usuario
  const [username, setUsername] = useState(user.username || '');
  const [password, setPassword] = useState('');
  const [region, setRegion] = useState(user.region || '');
  const [comuna, setComuna] = useState(user.comuna || '');
  const [correo, setCorreo] = useState(user.correo || '');
  const [telefono, setTelefono] = useState(user.telefono || '');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  /**
   * Manejador del envío del formulario para actualizar el perfil.
   * Envía las propiedades actualizadas en un payload al microservicio a través del gateway.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    try {
      const payload = {
        username,
        region,
        comuna,
        correo,
        telefono
      };

      // Adjuntar contraseña opcional si no está vacía
      if (password.trim() !== '') {
        payload.password = password;
      }

      // Consumir el endpoint PUT securizado de usuarios a través del API Gateway
      const res = await api.put(`/usuarios/users/${user.id}`, payload);
      
      const updatedUser = res.data;
      localStorage.setItem('user', JSON.stringify(updatedUser));
      
      setSuccess('Perfil actualizado con éxito');
      
      // Esperar brevemente antes de cerrar y notificar cambios
      setTimeout(() => {
        onUpdateSuccess(updatedUser);
      }, 1500);
    } catch (err) {
      console.error("Error updating profile", err);
      setError('Hubo un error al actualizar el perfil');
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxWidth: '500px' }}>
        <h2>Editar Perfil</h2>
        
        {error && <div style={{color: '#ff4757', marginBottom: '10px'}}>{error}</div>}
        {success && <div style={{color: '#2ed573', marginBottom: '10px'}}>{success}</div>}
        
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
            <label>Nueva Contraseña (dejar en blanco para mantener actual)</label>
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
            <label>Rol (No modificable)</label>
            <input 
              type="text" 
              value={user.rol || 'USUARIO'} 
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
            <button type="submit" className="btn">Guardar Cambios</button>
          </div>
        </form>
      </div>
    </div>
  );
};
