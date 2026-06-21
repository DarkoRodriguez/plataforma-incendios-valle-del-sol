import React, { useState, useEffect } from 'react';
import { 
  getFireReports, 
  updateReportStatus, 
  getActiveReportsCount, 
  getAllUsers, 
  updateUserRole, 
  FireReportDTO, 
  UserDTO 
} from '../services/api';

interface DashboardProps {
  currentUser: UserDTO | null;
  onReportsUpdated: () => void;
}

export const Dashboard: React.FC<DashboardProps> = ({ currentUser, onReportsUpdated }) => {
  const [reports, setReports] = useState<FireReportDTO[]>([]);
  const [users, setUsers] = useState<UserDTO[]>([]);
  const [activeForestalCount, setActiveForestalCount] = useState(0);
  const [activeEstructuralCount, setActiveEstructuralCount] = useState(0);
  const [activeVehicularCount, setActiveVehicularCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState<'reports' | 'users'>('reports');

  const token = localStorage.getItem('token') || '';
  const isAdmin = currentUser && currentUser.role === 'ADMINISTRATOR';

  const loadData = async () => {
    setLoading(true);
    try {
      // 1. Get reports
      const r = await getFireReports();
      setReports(r);

      // 2. Get active counts via stored procedure endpoint
      const forestCount = await getActiveReportsCount('FORESTAL');
      const structCount = await getActiveReportsCount('ESTRUCTURAL');
      const vehicCount = await getActiveReportsCount('VEHICULAR');
      setActiveForestalCount(forestCount);
      setActiveEstructuralCount(structCount);
      setActiveVehicularCount(vehicCount);

      // 3. Get users if admin
      if (isAdmin && token) {
        const u = await getAllUsers(token);
        setUsers(u);
      }
    } catch (e) {
      console.error(e);
      setError('Error loading dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [currentUser]);

  const handleStatusChange = async (reportId: number, newStatus: string) => {
    if (!token) return;
    try {
      const updated = await updateReportStatus(reportId, newStatus, token);
      if (updated) {
        setReports(reports.map(r => r.id === reportId ? updated : r));
        onReportsUpdated();
        // Update stats
        const forestCount = await getActiveReportsCount('FORESTAL');
        const structCount = await getActiveReportsCount('ESTRUCTURAL');
        const vehicCount = await getActiveReportsCount('VEHICULAR');
        setActiveForestalCount(forestCount);
        setActiveEstructuralCount(structCount);
        setActiveVehicularCount(vehicCount);
      }
    } catch (e) {
      console.error(e);
      alert('Failed to update report status.');
    }
  };

  const handleRoleChange = async (userId: number, newRole: string) => {
    if (!token) return;
    try {
      const updated = await updateUserRole(userId, newRole, token);
      if (updated) {
        setUsers(users.map(u => u.id === userId ? updated : u));
        alert('Rol de usuario actualizado correctamente');
        // If current user updated their own role, alert them
        if (currentUser && currentUser.id === userId) {
          localStorage.setItem('user', JSON.stringify(updated));
          window.dispatchEvent(new Event('auth-change'));
        }
      }
    } catch (e) {
      console.error(e);
      alert('Failed to update user role');
    }
  };

  return (
    <div className="dashboard-container">
      {/* Analytics Cards Row */}
      <div className="metrics-row">
        <div className="metric-card forestal">
          <div className="metric-value">{activeForestalCount}</div>
          <div className="metric-label">Incendios forestales activos</div>
        </div>
        <div className="metric-card estructural">
          <div className="metric-value">{activeEstructuralCount}</div>
          <div className="metric-label">Incendios estructurales activos</div>
        </div>
        <div className="metric-card vehicular">
          <div className="metric-value">{activeVehicularCount}</div>
          <div className="metric-label">Incendios vehiculares activos</div>
        </div>
      </div>

      {/* Tabs Menu */}
      <div className="dashboard-tabs">
        <button 
          className={`tab-btn ${activeTab === 'reports' ? 'active' : ''}`}
          onClick={() => setActiveTab('reports')}
        >
          Registro de reportes ({reports.length})
        </button>
        {isAdmin && (
          <button 
            className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
            onClick={() => setActiveTab('users')}
          >
            Gestión de roles ({users.length})
          </button>
        )}
      </div>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <div className="loading-spinner">Cargando información del panel...</div>
      ) : (
        <div className="dashboard-content">
          {activeTab === 'reports' && (
            <div className="table-responsive">
              <table className="dashboard-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Tipo</th>
                    <th>Estado</th>
                    <th>Descripción</th>
                    <th>Coordenadas</th>
                    <th>Fecha</th>
                    <th>Adjunto</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {reports.map((report) => (
                    <tr key={report.id}>
                      <td>{report.id}</td>
                      <td>
                        <span className={`badge type-${report.type.toLowerCase()}`}>
                          {report.type}
                        </span>
                      </td>
                      <td>
                        <span className={`status-dot ${report.status.toLowerCase()}`} />
                        <span className={`status-text ${report.status.toLowerCase()}`}>
                          {report.status}
                        </span>
                      </td>
                      <td className="description-cell">{report.description}</td>
                      <td>
                        {report.latitude.toFixed(4)}, {report.longitude.toFixed(4)}
                      </td>
                      <td>
                        {report.reportDate ? new Date(report.reportDate).toLocaleString() : 'N/A'}
                      </td>
                      <td>
                        {report.mediaUrl ? (
                          <a 
                            href={report.mediaUrl} 
                            target="_blank" 
                            rel="noopener noreferrer" 
                            className="media-link"
                          >
                            Ver archivo
                          </a>
                        ) : (
                          <span style={{ color: '#555' }}>Ninguno</span>
                        )}
                      </td>
                      <td>
                        {report.id !== undefined && (
                          <select 
                            value={report.status} 
                            onChange={(e) => handleStatusChange(report.id!, e.target.value)}
                            className="action-select"
                          >
                            <option value="ACTIVE">ACTIVO</option>
                            <option value="CONTROLLED">CONTROLADO</option>
                            <option value="EXTINGUISHED">EXTINTO</option>
                          </select>
                        )}
                      </td>
                    </tr>
                  ))}
                  {reports.length === 0 && (
                    <tr>
                      <td colSpan={8} style={{ textAlign: 'center', color: '#888' }}>
                        Aún no se han enviado reportes de focos de incendio.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}

          {activeTab === 'users' && isAdmin && (
            <div className="table-responsive">
              <table className="dashboard-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Usuario</th>
                    <th>Correo</th>
                    <th>Teléfono</th>
                    <th>Región</th>
                    <th>Comuna</th>
                    <th>Rol actual</th>
                    <th>Modificar rol</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => (
                    <tr key={u.id}>
                      <td>{u.id}</td>
                      <td style={{ fontWeight: 'bold' }}>{u.username}</td>
                      <td>{u.email || 'N/A'}</td>
                      <td>{u.phone || 'N/A'}</td>
                      <td>{u.region || 'N/A'}</td>
                      <td>{u.commune || 'N/A'}</td>
                      <td>
                        <span className={`role-badge ${u.role.toLowerCase()}`}>
                          {u.role}
                        </span>
                      </td>
                      <td>
                        <select 
                          value={u.role} 
                          onChange={(e) => handleRoleChange(u.id, e.target.value)}
                          className="action-select"
                        >
                          <option value="USER">USER</option>
                          <option value="BRIGADIST">BRIGADIST</option>
                          <option value="ADMINISTRATOR">ADMINISTRATOR</option>
                        </select>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
