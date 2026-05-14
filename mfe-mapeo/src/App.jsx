import React from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';
import { AuthProvider, useAuthState } from './store/AuthContext';
import Login from './views/Login';
import Profile from './views/Profile';
import Menu from './components/Menu';

function AppContent() {
  const { user } = useAuthState();
  return (
    <div>
      <Menu />
      {!user ? <Login /> : <Profile />}
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
