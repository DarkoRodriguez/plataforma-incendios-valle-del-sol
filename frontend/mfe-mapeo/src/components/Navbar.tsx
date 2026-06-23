import React, { useState, useEffect } from 'react';
import { UserDTO } from '../services/api';

interface NavbarProps {
  onLoginClick: () => void;
  onRegisterClick: () => void;
  onEditProfileClick: () => void;
  isReporting: boolean;
  setIsReporting: (val: boolean) => void;
  currentView: 'map' | 'dashboard';
  setCurrentView: (view: 'map' | 'dashboard') => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  onLoginClick,
  onRegisterClick,
  onEditProfileClick,
  isReporting,
  setIsReporting,
  currentView,
  setCurrentView,
}) => {
  const [user, setUser] = useState<UserDTO | null>(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const checkUser = () => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        setUser(null);
      }
    } else {
      setUser(null);
    }
  };

  useEffect(() => {
    checkUser();
    window.addEventListener('auth-change', checkUser);
    return () => window.removeEventListener('auth-change', checkUser);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setDropdownOpen(false);
    setCurrentView('map');
    window.dispatchEvent(new Event('auth-change'));
  };

  const showDashboardBtn = user && (user.role === 'ADMINISTRATOR' || user.role === 'BRIGADIST');

  return (
    <div className="navbar">
      <div className="nav-actions">
        {currentView === 'map' ? (
          <button 
            className={`btn ${isReporting ? 'btn-danger' : 'btn-primary'}`}
            onClick={() => setIsReporting(!isReporting)}
          >
            {isReporting ? 'Cancelar reporte' : 'Reportar incendio'}
          </button>
        ) : (
          <div className="dashboard-title-badge">Panel administrativo</div>
        )}
      </div>
      
      <div className="nav-brand">
        <h1 onClick={() => setCurrentView('map')} style={{ cursor: 'pointer' }}>
          Valle del Sol
        </h1>
        {showDashboardBtn && (
          <div className="nav-tabs">
            <button 
              className={`nav-tab ${currentView === 'map' ? 'active' : ''}`}
              onClick={() => setCurrentView('map')}
            >
              Mapa
            </button>
            <button 
              className={`nav-tab ${currentView === 'dashboard' ? 'active' : ''}`}
              onClick={() => setCurrentView('dashboard')}
            >
              Panel
            </button>
          </div>
        )}
      </div>
      
      <div className="nav-actions">
        {user ? (
          <div className="avatar-wrapper">
            <div 
              className="avatar" 
              onClick={() => setDropdownOpen(!dropdownOpen)}
            >
              {user.username.charAt(0).toUpperCase()}
            </div>
            {dropdownOpen && (
              <div className="avatar-dropdown">
                <div className="dropdown-header">
                  Hola, <b>{user.username}</b>
                  <div className="dropdown-role">{user.role === 'ADMINISTRATOR' ? 'Administrador' : user.role === 'BRIGADIST' ? 'Brigadista' : 'Usuario'}</div>
                </div>
                <button className="dropdown-item" onClick={() => {
                  setDropdownOpen(false);
                  onEditProfileClick();
                }}>Editar perfil</button>
                <button className="dropdown-item logout-btn" onClick={handleLogout}>Cerrar sesión</button>
              </div>
            )}
          </div>
        ) : (
          <>
            <button className="btn btn-secondary" onClick={onLoginClick}>Iniciar sesión</button>
            <button className="btn btn-primary" onClick={onRegisterClick}>Registrarse</button>
          </>
        )}
      </div>
    </div>
  );
};
