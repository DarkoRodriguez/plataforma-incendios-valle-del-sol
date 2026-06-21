import React, { useState } from 'react';
import { createFireReport, createFireReportMultipart, FireReportDTO } from '../services/api';
import { CHILE_REGIONS, COMMUNES_BY_REGION, RegionName } from '../data/chileRegions';

interface ReportModalProps {
  location: { lat: number; lng: number };
  onClose: () => void;
  onReportSuccess: (report: FireReportDTO) => void;
}

export const ReportModal: React.FC<ReportModalProps> = ({ location, onClose, onReportSuccess }) => {
  const [description, setDescription] = useState('');
  const [type, setType] = useState('FORESTAL');
  const [status, setStatus] = useState('ACTIVE');
  const [region, setRegion] = useState<RegionName | ''>('');
  const [commune, setCommune] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const communes = region ? COMMUNES_BY_REGION[region] : [];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError('');
    
    try {
      const userStr = localStorage.getItem('user');
      let userId: number | undefined = undefined;
      if (userStr) {
        try {
          userId = JSON.parse(userStr).id;
        } catch (e) {
          // ignore
        }
      }

      let result: FireReportDTO | null = null;

      if (selectedFile) {
        // Submit multipart
        const formData = new FormData();
        formData.append('description', description);
        formData.append('type', type);
        formData.append('status', status);
        formData.append('latitude', location.lat.toString());
        formData.append('longitude', location.lng.toString());
        formData.append('region', region);
        formData.append('commune', commune);
        if (userId !== undefined) {
          formData.append('userId', userId.toString());
        }
        formData.append('file', selectedFile);

        result = await createFireReportMultipart(formData);
      } else {
        // Submit standard JSON
        const payload: FireReportDTO = {
          description,
          type,
          status,
          latitude: location.lat,
          longitude: location.lng,
          region,
          commune,
          userId,
        };
        result = await createFireReport(payload);
      }

      if (result) {
        onReportSuccess(result);
        onClose();
      } else {
        setError('Failed to submit fire report. Please try again.');
      }
    } catch (err) {
      console.error(err);
      setError('An error occurred while sending report.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <h2>Reportar foco de incendio</h2>
        
        {error && <div className="error-message">{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Ubicación seleccionada</label>
            <input 
              type="text" 
              value={`Lat: ${location.lat.toFixed(6)}, Lng: ${location.lng.toFixed(6)}`} 
              disabled 
            />
          </div>

          <div className="form-group">
            <label>Descripción</label>
            <textarea 
              rows={3}
              value={description} 
              onChange={e => setDescription(e.target.value)} 
              placeholder="Describe la condición del incendio, intensidad, daños, etc."
              required 
            />
          </div>

          <div className="form-row" style={{ display: 'flex', gap: '10px' }}>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Fire Type</label>
              <select value={type} onChange={e => setType(e.target.value)}>
                <option value="FORESTAL">Forestal</option>
                <option value="ESTRUCTURAL">Estructural</option>
                <option value="VEHICULAR">Vehicular</option>
                <option value="OTRO">Other</option>
              </select>
            </div>

            <div className="form-group" style={{ flex: 1 }}>
              <label>Status</label>
              <select value={status} onChange={e => setStatus(e.target.value)}>
                <option value="ACTIVE">Active</option>
                <option value="CONTROLLED">Controlled</option>
                <option value="EXTINGUISHED">Extinguished</option>
              </select>
            </div>
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
            <label>Adjuntar foto/video (opcional)</label>
            <input 
              type="file" 
              accept="image/*,video/*"
              onChange={handleFileChange}
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '8px',
                border: '1px solid rgba(255, 255, 255, 0.1)',
                backgroundColor: 'rgba(0,0,0,0.2)',
                color: 'white',
              }}
            />
          </div>
          
          <div className="modal-actions" style={{ marginTop: '20px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={isSubmitting}>Cancelar</button>
            <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
              {isSubmitting ? 'Enviando...' : 'Enviar reporte'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
