import React, { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { MapView } from './components/MapView';
import { AuthModals } from './components/AuthModals';
import { ReportModal } from './components/ReportModal';
import { ProfileModal } from './components/ProfileModal';
import api from './api';

function App() {
  const [reportes, setReportes] = useState([]);
  const [authModal, setAuthModal] = useState(null); // 'login' | 'register' | null
  const [profileOpen, setProfileOpen] = useState(false);
  const [isReporting, setIsReporting] = useState(false);
  const [selectedLocation, setSelectedLocation] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);

  const fetchReportes = async () => {
    try {
      const res = await api.get('/mapeo/reportes');
      setReportes(res.data);
    } catch (err) {
      console.error("Error fetching reports", err);
    }
  };

  useEffect(() => {
    fetchReportes();

    const checkUser = () => {
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        setCurrentUser(JSON.parse(storedUser));
      } else {
        setCurrentUser(null);
      }
    };
    
    checkUser();
    window.addEventListener('auth-change', checkUser);
    return () => window.removeEventListener('auth-change', checkUser);
  }, []);

  const handleLocationSelected = (latlng) => {
    setSelectedLocation(latlng);
  };

  const handleReportSuccess = (newReport) => {
    setReportes([...reportes, newReport]);
    setSelectedLocation(null);
    setIsReporting(false);
  };

  const handleAuthSuccess = () => {
    setAuthModal(null);
    window.dispatchEvent(new Event('auth-change'));
  };

  const handleProfileUpdateSuccess = (updatedUser) => {
    setCurrentUser(updatedUser);
    setProfileOpen(false);
    window.dispatchEvent(new Event('auth-change'));
  };

  const handleStatusChange = async (reportId, newStatus) => {
    try {
      await api.put(`/mapeo/reportes/${reportId}/estado`, { estado: newStatus });
      // Update locally
      setReportes(prev => prev.map(r => r.id === reportId ? { ...r, estado: newStatus } : r));
    } catch (err) {
      console.error("Error updating fire status", err);
      alert("No se pudo actualizar el estado del incendio. Verifica tus permisos de Brigadista.");
    }
  };

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
      />
      
      <MapView 
        reportes={reportes} 
        isReporting={isReporting} 
        onLocationSelected={handleLocationSelected} 
        currentUser={currentUser}
        onStatusChange={handleStatusChange}
      />

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
