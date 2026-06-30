import React, { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { MapView } from './components/MapView';
import { AuthModals } from './components/AuthModals';
import { ReportModal } from './components/ReportModal';
import { ProfileModal } from './components/ProfileModal';
import { Dashboard } from './views/Dashboard';
import { usePushNotifications } from './hooks/usePushNotifications';
import { 
  getFireReports, 
  getAlerts, 
  createAlert, 
  updateReportStatus, 
  FireReportDTO, 
  AlertDTO, 
  UserDTO 
} from './services/api';
import { CHILE_REGIONS, COMMUNES_BY_REGION, RegionName } from './data/chileRegions';
import L from 'leaflet';

const API_BASE = import.meta.env.VITE_API_BASE || '/api';

function App() {
  const {
    isSupported: isPushSupported,
    isSubscribed: isPushSubscribed,
    permission: pushPermission,
    isLoading: isPushLoading,
    statusMessage: pushStatusMessage,
    subscribe: subscribeToPush,
    unsubscribe: unsubscribeFromPush,
  } = usePushNotifications();
  const [reports, setReports] = useState<FireReportDTO[]>([]);
  const [alerts, setAlerts] = useState<AlertDTO[]>([]);
  const [authModal, setAuthModal] = useState<'login' | 'register' | null>(null);
  const [profileOpen, setProfileOpen] = useState(false);
  const [isReporting, setIsReporting] = useState(false);
  const [selectedLocation, setSelectedLocation] = useState<{ lat: number; lng: number } | null>(null);
  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);
  const [isPanelOpen, setIsPanelOpen] = useState(true);
  
  // Dashboard vs Map routing
  const [currentView, setCurrentView] = useState<'map' | 'dashboard'>('map');
  
  // Real-time toast notifications
  const [toasts, setToasts] = useState<AlertDTO[]>([]);

  const addNotification = (alert: AlertDTO) => {
    setAlerts(prev => [alert, ...prev.filter(a => a.id !== alert.id)]);
    setToasts(prev => {
      if (alert.id && prev.some(t => t.id === alert.id)) {
        return prev;
      }
      return [...prev, alert];
    });
  };

  const formatReportAlert = (report: FireReportDTO): AlertDTO => {
    const coordinates = `Lat ${report.latitude.toFixed(4)}, Lon ${report.longitude.toFixed(4)}`;
    const regionCommune = report.commune ? `${report.commune}${report.region ? ', ' + report.region : ''}` : report.region || 'desconocida';
    return {
      title: 'Nuevo reporte de incendio',
      message: `Se ha reportado un incendio ${report.type.toLowerCase()} en ${regionCommune} (${coordinates}). ${report.description || ''}`.trim(),
      level: 'WARNING',
      commune: report.commune || 'sin comuna',
      region: report.region,
      createdAt: new Date().toISOString(),
    };
  };
  
  // Alert creator form inputs
  const [newAlertTitle, setNewAlertTitle] = useState('');
  const [newAlertMessage, setNewAlertMessage] = useState('');
  const [newAlertLevel, setNewAlertLevel] = useState('INFO');
  const [newAlertRegion, setNewAlertRegion] = useState<RegionName | ''>('');
  const [newAlertCommune, setNewAlertCommune] = useState('');
  const [isPublishingAlert, setIsPublishingAlert] = useState(false);

  const fetchReports = async () => {
    try {
      const data = await getFireReports();
      setReports(data);
    } catch (err) {
      console.error("Error fetching reports", err);
    }
  };

  const fetchAlertList = async () => {
    try {
      const data = await getAlerts();
      setAlerts(data);
    } catch (err) {
      console.error("Error fetching alerts", err);
    }
  };

  useEffect(() => {
    fetchReports();
    fetchAlertList();

    const checkUser = () => {
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        try {
          setCurrentUser(JSON.parse(storedUser));
        } catch (e) {
          setCurrentUser(null);
        }
      } else {
        setCurrentUser(null);
      }
    };
    
    checkUser();
    window.addEventListener('auth-change', checkUser);

    let sse: EventSource | null = null;
    let reconnectTimer: number | null = null;

    const connectSse = () => {
      if (sse) {
        sse.close();
      }

      const url = `${API_BASE}/alerts/stream`;
      sse = new EventSource(url);

      sse.addEventListener('ALERT', (e: MessageEvent) => {
        try {
          const newAlert: AlertDTO = JSON.parse(e.data);
          addNotification(newAlert);
        } catch (err) {
          console.error('Failed to parse SSE alert event data', err);
        }
      });

      sse.addEventListener('INIT', (e: MessageEvent) => {
        console.log('SSE Connection Initialized:', e.data);
      });

      sse.onerror = (e) => {
        console.warn('SSE connection error, attempting reconnect...', e);
        if (sse) {
          sse.close();
          sse = null;
        }
        if (reconnectTimer) {
          window.clearTimeout(reconnectTimer);
        }
        reconnectTimer = window.setTimeout(connectSse, 5000);
      };
    };

    connectSse();

    return () => {
      window.removeEventListener('auth-change', checkUser);
      if (reconnectTimer) {
        window.clearTimeout(reconnectTimer);
      }
      if (sse) {
        sse.close();
      }
    };
  }, []);

  // Remove toast notifications after 8 seconds
  useEffect(() => {
    if (toasts.length > 0) {
      const timer = setTimeout(() => {
        setToasts(prev => prev.slice(1));
      }, 8000);
      return () => clearTimeout(timer);
    }
  }, [toasts]);

  const handleLocationSelected = (latlng: L.LatLng) => {
    setSelectedLocation({ lat: latlng.lat, lng: latlng.lng });
  };

  const handleReportSuccess = (newReport: FireReportDTO) => {
    setReports(prev => [...prev, newReport]);
    setSelectedLocation(null);
    setIsReporting(false);
    addNotification(formatReportAlert(newReport));
  };

  const handleAuthSuccess = (user: UserDTO) => {
    setAuthModal(null);
    setCurrentUser(user);
    window.dispatchEvent(new Event('auth-change'));
  };

  const handleProfileUpdateSuccess = (updatedUser: UserDTO) => {
    setCurrentUser(updatedUser);
    setProfileOpen(false);
    window.dispatchEvent(new Event('auth-change'));
  };

  const handleStatusChange = async (reportId: number, newStatus: string) => {
    const token = localStorage.getItem('token') || '';
    if (!token) return;
    try {
      const updated = await updateReportStatus(reportId, newStatus, token);
      if (updated) {
        setReports(prev => prev.map(r => r.id === reportId ? updated : r));
      }
    } catch (err) {
      console.error("Error updating fire status", err);
      alert('No se pudo actualizar el estado del reporte. Verifica permisos.');
    }
  };

  const handlePublishAlertSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const token = localStorage.getItem('token') || '';
    if (!token) {
      alert('Debes iniciar sesión para publicar alertas');
      return;
    }
    
    setIsPublishingAlert(true);
    try {
      const payload: AlertDTO = {
        title: newAlertTitle,
        message: newAlertMessage,
        level: newAlertLevel,
        region: newAlertRegion || undefined,
        commune: newAlertCommune
      };
      
      const created = await createAlert(payload, token);
      if (created) {
        setNewAlertTitle('');
        setNewAlertMessage('');
        addNotification(created);
        alert('Alerta de emergencia publicada y transmitida con éxito.');
      } else {
        alert('No se pudo publicar la alerta.');
      }
    } catch (err) {
      console.error(err);
      alert('Error al publicar la alerta.');
    } finally {
      setIsPublishingAlert(false);
    }
  };

  const isBrigadistaOrAdmin = currentUser && (
    currentUser.role === 'BRIGADIST' || currentUser.role === 'ADMINISTRATOR'
  );

  return (
    <>
      <Navbar 
        onLoginClick={() => setAuthModal('login')}
        onRegisterClick={() => setAuthModal('register')}
        onEditProfileClick={() => setProfileOpen(true)}
        isReporting={isReporting}
        setIsReporting={(val) => {
          setIsReporting(val);
          if (!val) setSelectedLocation(null);
        }}
        currentView={currentView}
        setCurrentView={setCurrentView}
      />

      {currentView === 'map' ? (
        <div className="main-layout">
          {/* Collapsible Left Side Panel: Alerts & Notifications Feed */}
          <div className={`side-panel ${isPanelOpen ? 'open' : 'collapsed'}`}>
            <div className="panel-header">
              <div>
                <h2>Alertas de la comunidad</h2>
                <div className="small-text">Mantente alerta con notificaciones locales y actualizaciones en tiempo real.</div>
              </div>
              <button
                type="button"
                className="btn btn-secondary sidebar-toggle"
                onClick={() => setIsPanelOpen(prev => !prev)}
              >
                {isPanelOpen ? 'Ocultar panel' : 'Mostrar panel'}
              </button>
            </div>

            {isPanelOpen && (
              <>
                <div className="push-control-card">
                  <div>
                    <strong>Notificaciones push</strong>
                    <div className="small-text">{pushStatusMessage}</div>
                  </div>
                  <div className="push-buttons">
                    <button
                      className="btn btn-secondary"
                      onClick={subscribeToPush}
                      disabled={!isPushSupported || isPushSubscribed || isPushLoading}
                    >
                      Activar
                    </button>
                    <button
                      className="btn btn-secondary"
                      onClick={unsubscribeFromPush}
                      disabled={!isPushSupported || !isPushSubscribed || isPushLoading}
                    >
                      Desactivar
                    </button>
                    <button onClick={() => {
                      throw new Error("¡Test de error en React + Vite!");
                    }}>
                      Romper Aplicación
                    </button>
                  </div>
                </div>

                <div className="alerts-feed">
                  {alerts.map((alert, idx) => (
                    <div key={alert.id || idx} className={`alert-card level-${alert.level.toLowerCase()}`}>
                      <div className="alert-card-header">
                        <span className="alert-badge">{alert.level === 'INFO' ? 'INFO' : alert.level === 'WARNING' ? 'ADVERTENCIA' : alert.level === 'DANGER' ? 'PELIGRO' : alert.level}</span>
                        <span className="alert-commune">{alert.commune}</span>
                      </div>
                      <h3>{alert.title}</h3>
                      <p>{alert.message}</p>
                      {alert.createdAt && (
                        <small className="alert-date">
                          {new Date(alert.createdAt).toLocaleString()}
                        </small>
                      )}
                    </div>
                  ))}
                  {alerts.length === 0 && (
                    <div className="empty-alerts">No se han transmitido alertas de emergencia. Estado comunal estable.</div>
                  )}
                </div>

                {!isBrigadistaOrAdmin && (
                  <div className="info-message">
                    Solo brigadistas y administradores pueden publicar nuevas alertas. El resto de la comunidad recibe notificaciones y actualizaciones.
                  </div>
                )}

                {isBrigadistaOrAdmin && (
                  <div className="side-panel-section alert-publisher">
                    <h2>Publicar alerta</h2>
                    <form onSubmit={handlePublishAlertSubmit}>
                      <div className="form-group">
                        <label>Título</label>
                        <input
                          type="text"
                          value={newAlertTitle}
                          onChange={e => setNewAlertTitle(e.target.value)}
                          placeholder="Ej. Evacuación preventiva"
                          required
                        />
                      </div>
                      <div className="form-group">
                        <label>Mensaje</label>
                        <textarea
                          rows={2}
                          value={newAlertMessage}
                          onChange={e => setNewAlertMessage(e.target.value)}
                          placeholder="Ingrese las instrucciones de emergencia para la comunidad..."
                          required
                        />
                      </div>
                      <div className="form-row two-columns">
                        <div className="form-group">
                          <label>Región</label>
                          <select
                            value={newAlertRegion}
                            onChange={e => {
                              const selected = e.target.value as RegionName | '';
                              setNewAlertRegion(selected);
                              setNewAlertCommune('');
                            }}
                            required
                          >
                            <option value="">Seleccione región</option>
                            {CHILE_REGIONS.map(r => (
                              <option key={r} value={r}>{r}</option>
                            ))}
                          </select>
                        </div>
                        <div className="form-group">
                          <label>Comuna</label>
                          <select
                            value={newAlertCommune}
                            onChange={e => setNewAlertCommune(e.target.value)}
                            disabled={!newAlertRegion}
                            required
                          >
                            <option value="">Seleccione comuna</option>
                            {(newAlertRegion ? COMMUNES_BY_REGION[newAlertRegion] : []).map(c => (
                              <option key={c} value={c}>{c}</option>
                            ))}
                          </select>
                        </div>
                      </div>
                      <div className="form-group">
                        <label>Gravedad</label>
                        <select value={newAlertLevel} onChange={e => setNewAlertLevel(e.target.value)}>
                          <option value="INFO">INFO</option>
                          <option value="WARNING">WARNING</option>
                          <option value="DANGER">DANGER</option>
                        </select>
                      </div>
                      <button type="submit" className="btn btn-danger btn-block" disabled={isPublishingAlert}>
                        {isPublishingAlert ? 'Enviando...' : 'Publicar alerta ⚡'}
                      </button>
                    </form>
                  </div>
                )}
              </>
            )}
          </div>

          {/* Leaflet map */}
          <div className="map-wrapper">
            <MapView 
              reports={reports} 
              isReporting={isReporting} 
              onLocationSelected={handleLocationSelected} 
              currentUser={currentUser}
              onStatusChange={handleStatusChange}
            />
          </div>
        </div>
      ) : (
        <Dashboard 
          currentUser={currentUser} 
          onReportsUpdated={fetchReports} 
        />
      )}

      {/* Floating Push Toast Notifications (SSE-triggered) */}
      <div className="toasts-container">
        {toasts.map((toast, index) => (
          <div key={index} className={`toast-card level-${toast.level.toLowerCase()}`}>
            <div className="toast-header">
              <span className="toast-badge">🚨 Alerta de Emergencia</span>
              <button 
                className="toast-close"
                onClick={() => setToasts(prev => prev.filter((_, i) => i !== index))}
              >
                &times;
              </button>
            </div>
            <h4>{toast.title}</h4>
            <p>{toast.message}</p>
            <div className="toast-commune">Comuna: {toast.commune}{toast.region ? `, ${toast.region}` : ''}</div>
          </div>
        ))}
      </div>

      {authModal && (
        <AuthModals 
          type={authModal} 
          onClose={() => setAuthModal(null)} 
          onAuthSuccess={handleAuthSuccess}
        />
      )}

      {profileOpen && currentUser && (
        <ProfileModal 
          user={currentUser}
          onClose={() => setProfileOpen(false)}
          onUpdateSuccess={handleProfileUpdateSuccess}
        />
      )}

      {selectedLocation && (
        <ReportModal 
          location={selectedLocation} 
          onClose={() => setSelectedLocation(null)}
          onReportSuccess={handleReportSuccess}
        />
      )}
    </>
  );
}

export default App;
