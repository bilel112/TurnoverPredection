import React, { useState, useEffect } from 'react';
import Sidebar from './components/Sidebar';
import Dashboard from './components/Dashboard';
import EmployeeManager from './components/EmployeeManager';
import UserManager from './components/UserManager';
import RoleManager from './components/RoleManager';
import Alerts from './components/Alerts';
import MLPlaceholder from './components/MLPlaceholder';
import { AuthService } from './services/api';
import { Lock, User as UserIcon } from 'lucide-react';
import logoOoredoo from '../images/logoOoredoo.svg';

const App = () => {
  const [currentUser, setCurrentUser] = useState(null);
  const [activeTab, setActiveTab] = useState('dashboard');
  
  // Login Form State
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [loginError, setLoginError] = useState('');
  const [loginLoading, setLoginLoading] = useState(false);

  const handleLoginChange = (e) => {
    const { name, value } = e.target;
    setLoginForm(prev => ({ ...prev, [name]: value }));
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setLoginError('');
    setLoginLoading(true);

    const username = loginForm.username.trim();
    const password = loginForm.password;

    if (!username || !password) {
      setLoginError('Veuillez saisir votre nom d’utilisateur et votre mot de passe.');
      setLoginLoading(false);
      return;
    }

    try {
      const response = await AuthService.login(username, password);
      setCurrentUser({
        username: response.username,
        email: response.email,
        roleName: response.roleName,
      });
      setActiveTab('dashboard');
    } catch (error) {
      console.error(error);
      if (error.response && error.response.status === 400) {
        setLoginError('Nom d’utilisateur ou mot de passe incorrect.');
      } else {
        setLoginError('Erreur de connexion au serveur backend (port 8081).');
      }
    } finally {
      setLoginLoading(false);
    }
  };

  useEffect(() => {
    const initAuth = async () => {
      const token = window.localStorage.getItem('accessToken');
      if (!token) return;
      AuthService.setToken(token);
      try {
        const me = await AuthService.me();
        setCurrentUser({ username: me.username, email: me.email, roleName: me.roleName });
      } catch (e) {
        AuthService.clearToken();
        setCurrentUser(null);
      }
    };
    initAuth();
  }, []);

  const handleLogout = () => {
    AuthService.clearToken();
    setCurrentUser(null);
    setLoginForm({ username: '', password: '' });
  };

  // If not logged in, show the beautiful Login Screen
  if (!currentUser) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #1A1A1A 0%, #0F0F0F 100%)',
        padding: '1.5rem',
        fontFamily: 'var(--font-sans)'
      }}>
        <div style={{
          width: '100%',
          maxWidth: '420px',
          background: 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(10px)',
          borderRadius: 'var(--radius-lg)',
          boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.3)',
          border: '1px solid rgba(255, 255, 255, 0.2)',
          padding: '2.5rem',
          position: 'relative',
          overflow: 'hidden'
        }}>
          {/* Top Red Bar Decor */}
          <div style={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            height: '5px',
            backgroundColor: 'var(--primary)'
          }}></div>

          {/* Logo / Header */}
          <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
            <div style={{
              width: '168px',
              height: '168px',
              borderRadius: '24px',
              backgroundColor: 'transparent',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 1rem auto'
            }}>
              <img src={logoOoredoo} alt="Ooredoo" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
            </div>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-main)' }}>
              Ooredoo <span style={{ color: 'var(--primary)' }}>HR Portal</span>
            </h2>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
              Connectez-vous pour gérer les effectifs et prédictions
            </p>
          </div>

          <form onSubmit={handleLoginSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            
            {loginError && (
              <div style={{
                padding: '0.75rem',
                backgroundColor: '#FDE8E8',
                border: '1px solid #F8B4B4',
                color: '#9B1C1C',
                borderRadius: 'var(--radius-sm)',
                fontSize: '0.85rem',
                fontWeight: 500,
                textAlign: 'center'
              }}>
                {loginError}
              </div>
            )}

            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label">Nom d'utilisateur</label>
              <div style={{ position: 'relative' }}>
                <UserIcon size={16} style={{ position: 'absolute', left: '12px', top: '13px', color: 'var(--text-muted)' }} />
                <input
                  type="text"
                  name="username"
                  className="form-input"
                  required
                  placeholder="ex: admin"
                  style={{ paddingLeft: '38px' }}
                  value={loginForm.username}
                  onChange={handleLoginChange}
                />
              </div>
            </div>

            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label">Mot de passe</label>
              <div style={{ position: 'relative' }}>
                <Lock size={16} style={{ position: 'absolute', left: '12px', top: '13px', color: 'var(--text-muted)' }} />
                <input
                  type="password"
                  name="password"
                  className="form-input"
                  required
                  placeholder="admin123"
                  style={{ paddingLeft: '38px' }}
                  value={loginForm.password}
                  onChange={handleLoginChange}
                />
              </div>
            </div>

            <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '0.75rem', marginTop: '0.5rem' }} disabled={loginLoading}>
              {loginLoading ? (
                <div className="spinner" style={{ width: '18px', height: '18px', borderLeftColor: 'white' }}></div>
              ) : (
                <span>Se Connecter</span>
              )}
            </button>
          </form>

        </div>
      </div>
    );
  }

  // If logged in, render the main layout
  return (
    <div className="app-container">
      {/* Sidebar navigation */}
      <Sidebar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab} 
        onLogout={handleLogout} 
        currentUser={currentUser}
      />

      {/* Main panel container */}
      <main className="main-content">
        {activeTab === 'dashboard' && <Dashboard />}
        {activeTab === 'alerts' && <Alerts />}
        {activeTab === 'employees' && <EmployeeManager currentUser={currentUser} />}
        {activeTab === 'users' && <UserManager currentUser={currentUser} />}
        {activeTab === 'roles' && <RoleManager currentUser={currentUser} />}
        {activeTab === 'ml' && <MLPlaceholder />}
      </main>
    </div>
  );
};

export default App;
