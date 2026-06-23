import React from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, Circle, LayersControl, LayerGroup, useMapEvents } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import { FireReportDTO, UserDTO } from '../services/api';

// Fix for default Leaflet icons in React
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

const redIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34], shadowSize: [41, 41]
});

const orangeIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-orange.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34], shadowSize: [41, 41]
});

const greyIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-grey.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34], shadowSize: [41, 41]
});

const blueIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34], shadowSize: [41, 41]
});

const getMarkerIcon = (status: string) => {
  if (status === 'CONTROLLED') return orangeIcon;
  if (status === 'EXTINGUISHED') return greyIcon;
  return redIcon;
};

interface MapClickHandlerProps {
  isReporting: boolean;
  onLocationSelected: (latlng: L.LatLng) => void;
}

const MapClickHandler: React.FC<MapClickHandlerProps> = ({ isReporting, onLocationSelected }) => {
  useMapEvents({
    click(e) {
      if (isReporting) {
        onLocationSelected(e.latlng);
      }
    },
  });
  return null;
};

interface MapViewProps {
  reports: FireReportDTO[];
  isReporting: boolean;
  onLocationSelected: (latlng: L.LatLng) => void;
  currentUser: UserDTO | null;
  onStatusChange: (id: number, newStatus: string) => void;
}

export const MapView: React.FC<MapViewProps> = ({
  reports,
  isReporting,
  onLocationSelected,
  currentUser,
  onStatusChange,
}) => {
  const defaultCenter: [number, number] = [-33.4489, -70.6693];

  const isBrigadistaOrAdmin = currentUser && (
    currentUser.role === 'BRIGADIST' || currentUser.role === 'ADMINISTRATOR'
  );

  // Mock Brigadas activas
  const brigades = [
    { id: 1, name: 'Brigada Forestal Delta', lat: -33.4350, lng: -70.6550, status: 'En patrulla' },
    { id: 2, name: 'Brigada Central Sol', lat: -33.4680, lng: -70.6900, status: 'En sitio' },
  ];

  // Mock Evacuation Routes
  const routeNorth: [number, number][] = [
    [-33.4489, -70.6693], [-33.4400, -70.6200], [-33.4200, -70.6000]
  ];
  const routeSouth: [number, number][] = [
    [-33.4589, -70.6793], [-33.4800, -70.7100], [-33.5000, -70.7300]
  ];

  // Mock Risk Zones
  const riskZones = [
    { id: 1, name: 'Zona de riesgo cerro norte', lat: -33.4150, lng: -70.6250, radius: 1200 },
    { id: 2, name: 'Zona de riesgo borde sur del bosque', lat: -33.4850, lng: -70.7150, radius: 900 },
  ];

  const renderMedia = (url?: string) => {
    if (!url) return null;
    const isVideo = url.toLowerCase().match(/\.(mp4|webm|ogg)$/) || url.includes('video');
    return (
      <div style={{ marginTop: '10px' }}>
        <strong style={{ fontSize: '11px', display: 'block', marginBottom: '4px' }}>Adjunto:</strong>
        {isVideo ? (
          <video src={url} controls style={{ width: '100%', maxHeight: '120px', borderRadius: '6px', backgroundColor: '#000' }} />
        ) : (
          <img src={url} alt="Report" style={{ width: '100%', maxHeight: '120px', objectFit: 'cover', borderRadius: '6px', cursor: 'pointer' }}
            onClick={() => window.open(url, '_blank')} />
        )}
      </div>
    );
  };

  return (
    <div className="map-container">
      {isReporting && (
        <div className="report-tooltip">
          Haz clic en el mapa para ubicar el foco de incendio
        </div>
      )}
      <MapContainer center={defaultCenter} zoom={12} style={{ width: '100%', height: '100%' }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        <MapClickHandler isReporting={isReporting} onLocationSelected={onLocationSelected} />

        <LayersControl position="topright">
          <LayersControl.Overlay checked name="Reportes de incendios">
            <LayerGroup>
              {reports.map((report) => (
                <Marker
                  key={report.id}
                  position={[report.latitude, report.longitude]}
                  icon={getMarkerIcon(report.status)}
                >
                  <Popup>
                    <div style={{ minWidth: '220px' }}>
                      <strong style={{ fontSize: '14px', color: '#ff4757' }}>Detalles del reporte</strong>
                      <hr style={{ margin: '8px 0', borderColor: 'rgba(255,255,255,0.1)' }} />
                      <strong>Tipo:</strong> {report.type === 'FORESTAL' ? 'Forestal' : report.type === 'ESTRUCTURAL' ? 'Estructural' : report.type === 'VEHICULAR' ? 'Vehicular' : report.type} <br />
                      <strong>Estado:</strong> <span style={{
                        color: report.status === 'ACTIVE' ? '#ff4757' : report.status === 'CONTROLLED' ? '#ffa502' : '#2ed573',
                        fontWeight: 'bold'
                      }}>{report.status === 'ACTIVE' ? 'ACTIVO' : report.status === 'CONTROLLED' ? 'CONTROLADO' : report.status === 'EXTINGUISHED' ? 'EXTINTO' : report.status}</span> <br />
                      <strong>Descripción:</strong> {report.description} <br />
                      <small style={{ color: '#aaa' }}>
                        Fecha: {report.reportDate ? new Date(report.reportDate).toLocaleString() : 'N/D'}
                      </small>
                      {renderMedia(report.mediaUrl)}
                      {isBrigadistaOrAdmin && report.id !== undefined && (
                        <div style={{ marginTop: '12px', paddingTop: '10px', borderTop: '1px solid rgba(255,255,255,0.1)' }}>
                          <label style={{ fontSize: '11px', fontWeight: 'bold', display: 'block', marginBottom: '4px' }}>
                            Actualizar estado:
                          </label>
                          <select
                            value={report.status}
                            onChange={(e) => onStatusChange(report.id!, e.target.value)}
                            style={{ width: '100%', padding: '6px', borderRadius: '4px', border: '1px solid rgba(255,255,255,0.2)', backgroundColor: '#1e272e', color: '#fff', fontSize: '12px', cursor: 'pointer' }}
                          >
                            <option value="ACTIVE">ACTIVO</option>
                            <option value="CONTROLLED">CONTROLADO</option>
                            <option value="EXTINGUISHED">EXTINTO</option>
                          </select>
                        </div>
                      )}
                    </div>
                  </Popup>
                </Marker>
              ))}
            </LayerGroup>
          </LayersControl.Overlay>

          <LayersControl.Overlay checked name="Brigadas activas">
            <LayerGroup>
              {brigades.map((b) => (
                <Marker key={b.id} position={[b.lat, b.lng]} icon={blueIcon}>
                  <Popup>
                    <div>
                      <strong style={{ color: '#70a1ff' }}>{b.name}</strong> <br />
                      <strong>Estado:</strong> {b.status} <br />
                      <small style={{ color: '#bbb' }}>Posición GPS activa</small>
                    </div>
                  </Popup>
                </Marker>
              ))}
            </LayerGroup>
          </LayersControl.Overlay>

          <LayersControl.Overlay checked name="Rutas de evacuación">
            <LayerGroup>
              <Polyline positions={routeNorth} pathOptions={{ color: '#2ed573', weight: 5, dashArray: '8, 8' }}>
                <Popup><strong style={{ color: '#2ed573' }}>Ruta de evacuación norte</strong></Popup>
              </Polyline>
              <Polyline positions={routeSouth} pathOptions={{ color: '#2ed573', weight: 5, dashArray: '8, 8' }}>
                <Popup><strong style={{ color: '#2ed573' }}>Ruta de evacuación sur</strong></Popup>
              </Polyline>
            </LayerGroup>
          </LayersControl.Overlay>

          <LayersControl.Overlay checked name="Zonas de riesgo de incendio">
            <LayerGroup>
              {riskZones.map((zone) => (
                <Circle
                  key={zone.id}
                  center={[zone.lat, zone.lng]}
                  radius={zone.radius}
                  pathOptions={{ color: '#ff4757', fillColor: '#ff4757', fillOpacity: 0.15, weight: 2 }}
                >
                  <Popup>
                    <strong style={{ color: '#ff4757' }}>{zone.name}</strong> <br />
                    <strong>Radio:</strong> {zone.radius}m <br />
                    <span style={{ color: '#ff7f50' }}>Zona de alto riesgo por vegetación densa.</span>
                  </Popup>
                </Circle>
              ))}
            </LayerGroup>
          </LayersControl.Overlay>
        </LayersControl>
      </MapContainer>
    </div>
  );
};
