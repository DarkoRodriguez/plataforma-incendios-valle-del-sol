import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Fix for default Leaflet icons in React
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// Custom color-coded icons
const redIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

const orangeIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-orange.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

const greyIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-grey.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

const getMarkerIcon = (estado) => {
  if (estado === 'CONTROLADO') return orangeIcon;
  if (estado === 'EXTINGUIDO') return greyIcon;
  return redIcon; // Default to ACTIVO
};

// Component to handle map clicks for reporting
const MapClickHandler = ({ isReporting, onLocationSelected }) => {
  useMapEvents({
    click(e) {
      if (isReporting) {
        onLocationSelected(e.latlng);
      }
    },
  });
  return null;
};

export const MapView = ({ reportes, isReporting, onLocationSelected, currentUser, onStatusChange }) => {
  // Center of Valle del Sol / Chile (approx)
  const defaultCenter = [-33.4489, -70.6693];

  const isBrigadistaOrAdmin = currentUser && (
    currentUser.rol === 'BRIGADISTA' || currentUser.rol === 'ADMINISTRADOR'
  );

  return (
    <div className="map-container">
      {isReporting && (
        <div className="report-tooltip">
          Haz clic en el mapa para ubicar el incendio
        </div>
      )}
      <MapContainer 
        center={defaultCenter} 
        zoom={10} 
        style={{ width: '100%', height: '100%' }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        
        <MapClickHandler isReporting={isReporting} onLocationSelected={onLocationSelected} />

        {reportes.map((reporte) => (
          <Marker 
            key={reporte.id} 
            position={[reporte.latitud, reporte.longitud]}
            icon={getMarkerIcon(reporte.estado)}
          >
            <Popup>
              <div style={{ minWidth: '180px' }}>
                <strong style={{ fontSize: '14px' }}>Detalles del Reporte</strong>
                <hr style={{ margin: '8px 0', borderColor: 'rgba(0,0,0,0.1)' }} />
                <strong>Tipo:</strong> {reporte.tipo} <br />
                <strong>Estado:</strong> <span style={{
                  color: reporte.estado === 'ACTIVO' ? '#ff4757' : reporte.estado === 'CONTROLADO' ? '#ffa502' : '#70a1ff',
                  fontWeight: 'bold'
                }}>{reporte.estado}</span> <br />
                <strong>Descripción:</strong> {reporte.descripcion} <br />
                <small style={{ color: '#888' }}>Fecha: {new Date(reporte.fechaReporte).toLocaleString()}</small>

                {isBrigadistaOrAdmin && (
                  <div style={{ marginTop: '12px', paddingTop: '10px', borderTop: '1px solid rgba(0,0,0,0.1)' }}>
                    <label style={{ fontSize: '11px', fontWeight: 'bold', display: 'block', marginBottom: '4px' }}>
                      Modificar Estado:
                    </label>
                    <select 
                      value={reporte.estado} 
                      onChange={(e) => onStatusChange(reporte.id, e.target.value)}
                      style={{ 
                        width: '100%', 
                        padding: '6px', 
                        borderRadius: '4px',
                        border: '1px solid #ccc',
                        backgroundColor: '#fff',
                        color: '#333',
                        fontSize: '12px',
                        outline: 'none',
                        cursor: 'pointer'
                      }}
                    >
                      <option value="ACTIVO">ACTIVO (Foco encendido)</option>
                      <option value="CONTROLADO">CONTROLADO (Bajo control)</option>
                      <option value="EXTINGUIDO">EXTINGUIDO (Apagado)</option>
                    </select>
                  </div>
                )}
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
};
