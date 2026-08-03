import React, { useState, useEffect } from 'react';
import { RoleService } from '../services/api';
import { Plus, Trash2, ShieldAlert } from 'lucide-react';

const RoleManager = () => {
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newRoleName, setNewRoleName] = useState('');

  useEffect(() => {
    fetchRoles();
  }, []);

  const fetchRoles = async () => {
    try {
      setLoading(true);
      const data = await RoleService.getAll();
      setRoles(data || []);
    } catch (error) {
      console.error("Erreur de chargement des rôles", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!newRoleName.trim()) return;

    // Force uppercase for role naming convention
    const uppercaseName = newRoleName.trim().toUpperCase();

    try {
      await RoleService.create({ name: uppercaseName });
      setNewRoleName('');
      fetchRoles();
    } catch (error) {
      alert("Erreur lors de la création du rôle : " + (error.response?.data?.message || error.message));
    }
  };

  const handleDelete = async (id, name) => {
    if (['ADMIN', 'HR', 'MANAGER'].includes(name)) {
      alert(`Le rôle système "${name}" est protégé et ne peut pas être supprimé.`);
      return;
    }

    if (window.confirm(`Êtes-vous sûr de vouloir supprimer le rôle "${name}" ?`)) {
      try {
        await RoleService.delete(id);
        fetchRoles();
      } catch (error) {
        alert("Erreur lors de la suppression. Ce rôle est peut-être associé à des utilisateurs.");
      }
    }
  };

  return (
    <div style={{ maxWidth: '650px' }}>
      <h1 className="title">Gestion des Rôles</h1>
      <p className="subtitle">Visualisez et configurez les habilitations d'accès pour la plateforme</p>

      {/* Grid: Create Role and List Roles */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
        
        {/* Create Role card */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>Ajouter un nouveau rôle</h3>
          <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
            <div style={{ flex: 1 }}>
              <input
                type="text"
                className="form-input"
                placeholder="ex: ANALYST"
                required
                value={newRoleName}
                onChange={(e) => setNewRoleName(e.target.value)}
              />
            </div>
            <button type="submit" className="btn btn-primary">
              <Plus size={18} />
              <span>Ajouter</span>
            </button>
          </form>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginTop: '0.5rem' }}>
            Les noms des rôles seront automatiquement convertis en MAJUSCULES (ex : HR_ADMIN).
          </span>
        </div>

        {/* Roles list */}
        {loading ? (
          <div className="loading-container">
            <div className="spinner"></div>
          </div>
        ) : (
          <div className="table-container">
            <table className="custom-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nom du Rôle</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {roles.map((role) => {
                  const isProtected = ['ADMIN', 'HR', 'MANAGER'].includes(role.name);
                  return (
                    <tr key={role.id}>
                      <td>#{role.id}</td>
                      <td style={{ fontWeight: 600 }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          {role.name}
                          {isProtected && (
                            <span style={{
                              fontSize: '0.65rem',
                              padding: '2px 6px',
                              borderRadius: '4px',
                              backgroundColor: '#F3F4F6',
                              color: 'var(--text-muted)',
                              fontWeight: 500
                            }}>
                              Système
                            </span>
                          )}
                        </span>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <button
                          className="btn btn-secondary btn-xs"
                          disabled={isProtected}
                          style={{
                            opacity: isProtected ? 0.4 : 1,
                            cursor: isProtected ? 'not-allowed' : 'pointer'
                          }}
                          onClick={() => handleDelete(role.id, role.name)}
                          title={isProtected ? 'Rôle protégé' : 'Supprimer'}
                        >
                          <Trash2 size={14} style={{ color: isProtected ? 'var(--text-muted)' : '#EF4444' }} />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default RoleManager;
