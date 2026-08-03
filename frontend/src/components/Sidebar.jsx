import React from 'react';
import { LayoutDashboard, Users, UserCheck, Shield, TrendingUp, LogOut, Bell } from 'lucide-react';
import logoOoredoo from '../../images/logoOoredoo.svg';

const Sidebar = ({ activeTab, setActiveTab, onLogout, currentUser }) => {
  const menuItems = [
    { id: 'dashboard', name: 'Dashboard', icon: LayoutDashboard },
    { id: 'alerts', name: 'Alertes', icon: Bell },
    { id: 'employees', name: 'Employés', icon: Users },
    { id: 'users', name: 'Utilisateurs', icon: UserCheck },
    { id: 'roles', name: 'Rôles', icon: Shield },
    { id: 'ml', name: 'ML & Prédictions', icon: TrendingUp },
  ];

  const visibleItems = menuItems.filter(item => {
    if ((item.id === 'users' || item.id === 'roles')) {
      return (currentUser?.roleName || '').toString().trim().toUpperCase() === 'ADMIN';
    }
    return true;
  });

  return (
    <div style={{
      width: '260px',
      height: '100vh',
      backgroundColor: '#FFFFFF',
      borderRight: '1px solid var(--border-color)',
      position: 'fixed',
      top: 0,
      left: 0,
      display: 'flex',
      flexDirection: 'column',
      zIndex: 100
    }}>
      {/* Brand Header */}
      <div style={{
        padding: '1.5rem',
        borderBottom: '1px solid var(--border-color)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '0.5rem',
        textAlign: 'center'
      }}>
        <div style={{
          width: '100px',
          height: '100px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}>
          <img
            src={logoOoredoo}
            alt="Ooredoo"
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'contain'
            }}
          />
        </div>
        <h2 style={{
          fontSize: '1.2rem',
          fontWeight: 'bold',
          color: 'var(--primary)',
          margin: 0,
          letterSpacing: '1px'
        }}>
          Turnover Analytics
        </h2>
      </div>

      {/* Navigation List */}
      <nav style={{
        flex: 1,
        padding: '1.5rem 1rem',
        display: 'flex',
        flexDirection: 'column',
        gap: '0.35rem'
      }}>
        {visibleItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.875rem',
                padding: '0.75rem 1rem',
                border: 'none',
                borderRadius: '8px',
                backgroundColor: isActive ? 'var(--primary-light)' : 'transparent',
                color: isActive ? 'var(--primary)' : 'var(--text-muted)',
                fontWeight: isActive ? 600 : 500,
                fontSize: '0.9rem',
                cursor: 'pointer',
                textAlign: 'left',
                width: '100%',
                transition: 'all 0.2s ease',
              }}
              onMouseEnter={(e) => {
                if (!isActive) {
                  e.currentTarget.style.backgroundColor = '#F9FAFB';
                  e.currentTarget.style.color = 'var(--text-main)';
                }
              }}
              onMouseLeave={(e) => {
                if (!isActive) {
                  e.currentTarget.style.backgroundColor = 'transparent';
                  e.currentTarget.style.color = 'var(--text-muted)';
                }
              }}
            >
              <Icon size={18} strokeWidth={isActive ? 2.2 : 1.8} />
              <span>{item.name}</span>
            </button>
          );
        })}
      </nav>

      {/* User profile & Logout */}
      <div style={{
        padding: '1.25rem',
        borderTop: '1px solid var(--border-color)',
        display: 'flex',
        flexDirection: 'column',
        gap: '1rem',
        backgroundColor: '#FCFCFC'
      }}>
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.75rem'
        }}>
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: '50%',
            backgroundColor: '#F3F4F6',
            border: '2px solid var(--primary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 600,
            fontSize: '0.95rem',
            color: 'var(--text-main)'
          }}>
            {currentUser?.username?.substring(0, 2).toUpperCase() || 'AD'}
          </div>
          <div style={{ minWidth: 0, flex: 1 }}>
            <h4 style={{
              fontSize: '0.875rem',
              fontWeight: 600,
              color: 'var(--text-main)',
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              margin: 0
            }}>
              {currentUser?.username || 'Admin User'}
            </h4>
            <span style={{
              fontSize: '0.75rem',
              color: 'var(--primary)',
              fontWeight: 600,
              textTransform: 'uppercase',
              letterSpacing: '0.5px'
            }}>
              {currentUser?.roleName || 'ADMIN'}
            </span>
          </div>
        </div>

        <button
          onClick={onLogout}
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '0.5rem',
            padding: '0.625rem',
            border: '1px solid #F3F4F6',
            borderRadius: '8px',
            backgroundColor: '#FFFFFF',
            color: '#EF4444',
            fontWeight: 600,
            fontSize: '0.85rem',
            cursor: 'pointer',
            width: '100%',
            transition: 'all 0.2s ease',
            boxShadow: 'var(--shadow-sm)'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.backgroundColor = '#FEF2F2';
            e.currentTarget.style.borderColor = '#FEE2E2';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.backgroundColor = '#FFFFFF';
            e.currentTarget.style.borderColor = '#F3F4F6';
          }}
        >
          <LogOut size={15} />
          <span>Déconnexion</span>
        </button>
      </div>
    </div>
  );
};

export default Sidebar;