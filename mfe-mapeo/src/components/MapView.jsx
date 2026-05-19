import React, { useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Fix para los íconos de Leaflet en React
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
let DefaultIcon = L.icon({
    iconUrl: icon,
    shadowUrl: iconShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

// Custom Fire Icon
const fireIcon = new L.Icon({
    iconUrl: 'https://cdn-icons-png.flaticon.com/512/785/785116.png',
    iconSize: [30, 30],
    iconAnchor: [15, 30]
});

const LocationMarker = ({ onLocationSelected }) => {
    useMapEvents({
        click(e) {
            onLocationSelected(e.latlng);
        },
    });
    return null;
};

export const MapView = ({ reportes, onReportar }) => {
    const [selectedPos, setSelectedPos] = useState(null);
    const [descripcion, setDescripcion] = useState('');

    const handleReportar = () => {
        if (selectedPos && descripcion) {
            onReportar({
                latitud: selectedPos.lat,
                longitud: selectedPos.lng,
                descripcion: descripcion,
                tipo: 'URBANO',
                estado: 'ACTIVO'
            });
            setSelectedPos(null);
            setDescripcion('');
        }
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100%', borderRadius: '12px', overflow: 'hidden', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}>
            <div style={{ padding: '15px', backgroundColor: '#d32f2f', color: 'white' }}>
                <h3 style={{ margin: 0 }}>📍 Monitoreo y Reporte de Incendios</h3>
                <p style={{ margin: 0, fontSize: '14px' }}>Haz clic en el mapa para reportar un nuevo foco</p>
            </div>
            
            <MapContainer center={[-33.4569, -70.6483]} zoom={12} style={{ flexGrow: 1, height: '400px' }}>
                <TileLayer
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    attribution='&copy; OpenStreetMap contributors'
                />
                
                {reportes.map(r => (
                    <Marker key={r.id} position={[r.latitud, r.longitud]} icon={fireIcon}>
                        <Popup>
                            <strong>{r.tipo}</strong><br/>
                            {r.descripcion}<br/>
                            <small>{r.estado}</small>
                        </Popup>
                    </Marker>
                ))}

                <LocationMarker onLocationSelected={(pos) => setSelectedPos(pos)} />

                {selectedPos && (
                    <Marker position={selectedPos}>
                        <Popup>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                <strong>Reportar aquí</strong>
                                <input 
                                    type="text" 
                                    placeholder="Descripción..." 
                                    value={descripcion} 
                                    onChange={(e) => setDescripcion(e.target.value)}
                                    style={{ padding: '5px', borderRadius: '4px', border: '1px solid #ccc' }}
                                />
                                <button 
                                    onClick={handleReportar}
                                    style={{ backgroundColor: '#d32f2f', color: 'white', border: 'none', padding: '8px', borderRadius: '4px', cursor: 'pointer' }}
                                >
                                    Enviar Reporte
                                </button>
                            </div>
                        </Popup>
                    </Marker>
                )}
            </MapContainer>
        </div>
    );
};
