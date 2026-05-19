import React, { useState } from 'react';
import api from '../api';

export const ReportModal = ({ location, onClose, onReportSuccess }) => {
  const [descripcion, setDescripcion] = useState('');
  const [tipo, setTipo] = useState('FORESTAL');
  const [estado, setEstado] = useState('ACTIVO');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    try {
      const userStr = localStorage.getItem('user');
      let usuarioId = null;
      if (userStr) {
        usuarioId = JSON.parse(userStr).id;
      }

      const payload = {
        descripcion,
        tipo,
        estado,
        latitud: location.lat,
        longitud: location.lng,
        usuarioId
      };

      const res = await api.post('/mapeo/reportes', payload);
      onReportSuccess(res.data);
    } catch (err) {
      console.error(err);
      alert('Error al reportar el incendio');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <h2>Reportar Incendio</h2>
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Ubicación Seleccionada</label>
            <input 
              type="text" 
              value={`Lat: ${location.lat.toFixed(4)}, Lng: ${location.lng.toFixed(4)}`} 
              disabled 
            />
          </div>

          <div className="form-group">
            <label>Descripción</label>
            <textarea 
              rows="3"
              value={descripcion} 
              onChange={e => setDescripcion(e.target.value)} 
              required 
            />
          </div>

          <div className="form-group">
            <label>Tipo de Incendio</label>
            <select value={tipo} onChange={e => setTipo(e.target.value)}>
              <option value="FORESTAL">Forestal</option>
              <option value="ESTRUCTURAL">Estructural</option>
              <option value="VEHICULAR">Vehicular</option>
              <option value="OTRO">Otro</option>
            </select>
          </div>

          <div className="form-group">
            <label>Estado</label>
            <select value={estado} onChange={e => setEstado(e.target.value)}>
              <option value="ACTIVO">Activo</option>
              <option value="CONTROLADO">Controlado</option>
              <option value="EXTINGUIDO">Extinguido</option>
            </select>
          </div>
          
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={isSubmitting}>Cancelar</button>
            <button type="submit" className="btn" disabled={isSubmitting}>
              {isSubmitting ? 'Enviando...' : 'Enviar Reporte'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
