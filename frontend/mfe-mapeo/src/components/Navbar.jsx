import React, { useState, useEffect } from 'react';

export const Navbar = ({ onLoginClick, onRegisterClick, onEditProfileClick, isReporting, setIsReporting }) => {
  const [user, setUser] = useState(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  useEffect(() => {
    // Check local storage for user on mount
    const checkUser = () => {
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        setUser(JSON.parse(storedUser));
      } else {
        setUser(null);
      }
    };
    
    checkUser();
    // Simple custom event to listen to auth changes if needed
    window.addEventListener('auth-change', checkUser);
    return () => window.removeEventListener('auth-change', checkUser);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setDropdownOpen(false);
    window.dispatchEvent(new Event('auth-change'));
  };

  return (
    <div className="navbar">
      <div className="nav-actions">
        <button 
          className={`btn ${isReporting ? 'btn-secondary' : ''}`}
          onClick={() => setIsReporting(!isReporting)}
        >
          {isReporting ? 'Cancelar Reporte' : 'Reportar Incendio'}
        </button>
      </div>
      
      <h1>Valle del Sol</h1>
      
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
                <div style={{padding: '8px', borderBottom: '1px solid rgba(255,255,255,0.1)', marginBottom: '5px'}}>
                  Hola, <b>{user.username}</b>
                </div>
                {/* For now, just a placeholder for editing profile */}
                <button className="dropdown-item" onClick={() => {
                  setDropdownOpen(false);
                  onEditProfileClick();
                }}>Editar Perfil</button>
                <button className="dropdown-item" onClick={handleLogout} style={{color: '#ff4757'}}>Cerrar Sesión</button>
              </div>
            )}
          </div>
        ) : (
          <>
            <button className="btn btn-secondary" onClick={onLoginClick}>Iniciar Sesión</button>
            <button className="btn" onClick={onRegisterClick}>Registrarse</button>
          </>
        )}
      </div>
    </div>
  );
};
