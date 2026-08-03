import React, { useState, useEffect } from 'react';
import { UserService, RoleService } from '../services/api';
import { Plus, Edit, Trash2, X, ShieldAlert } from 'lucide-react';

const UserManager = ({ currentUser }) => {
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState('create'); // 'create', 'edit'
  const [selectedUser, setSelectedUser] = useState(null);

  // Form State
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    roleName: '',
    password: '',
  });

  useEffect(() => {
    fetchUsersAndRoles();
  }, []);

  const fetchUsersAndRoles = async () => {
    try {
      setLoading(true);
      const [usersData, rolesData] = await Promise.all([
        UserService.getAll(),
        RoleService.getAll()
      ]);
      setUsers(usersData || []);
      setRoles(rolesData || []);
      
      if (rolesData.length > 0 && !formData.roleName) {
        setFormData(prev => ({ ...prev, roleName: rolesData[0].name }));
      }
    } catch (error) {
      console.error("Erreur lors de la récupération des données", error);
    } finally {
      setLoading(false);
    }
  };

  const openModal = (mode, user = null) => {
    setModalMode(mode);
    if (user) {
      setSelectedUser(user);
      setFormData({
        username: user.username,
        email: user.email,
        roleName: user.roleName,
        password: '', // blank by default in edit mode
      });
    } else {
      setSelectedUser(null);
      setFormData({
        username: '',
        email: '',
        roleName: roles[0]?.name || 'HR',
        password: '',
      });
    }
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setSelectedUser(null);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (modalMode === 'create') {
        if (!formData.password) {
          alert("Le mot de passe est obligatoire pour la création !");
          return;
        }
        await UserService.create(formData);
      } else if (modalMode === 'edit') {
        // Send put request
        const payload = {
          id: selectedUser.id,
          username: formData.username,
          email: formData.email,
          roleName: formData.roleName,
        };
        // If password is set, add it
        if (formData.password) {
          payload.password = formData.password;
        }
        await UserService.update(selectedUser.id, payload);
      }
      fetchUsersAndRoles();
      closeModal();
    } catch (error) {
      alert("Erreur lors de la sauvegarde : " + (error.response?.data?.message || error.message));
    }
  };

  const handleDelete = async (id, username) => {
    if (username === currentUser.username) {
      alert("Vous ne pouvez pas supprimer votre propre compte !");
      return;
    }

    if (window.confirm(`Êtes-vous sûr de vouloir supprimer l'utilisateur "${username}" ?`)) {
      try {
        await UserService.delete(id);
        fetchUsersAndRoles();
      } catch (error) {
        alert("Erreur lors de la suppression");
      }
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 className="title">Gestion des Utilisateurs</h1>
          <p className="subtitle">Gérez les comptes administratifs qui accèdent à la plateforme</p>
        </div>
        <button className="btn btn-primary" onClick={() => openModal('create')}>
          <Plus size={18} />
          <span>Créer un Utilisateur</span>
        </button>
      </div>

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Chargement des comptes...</p>
        </div>
      ) : (
        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nom d'utilisateur</th>
                <th>Email</th>
                <th>Rôle</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan="5" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                    Aucun utilisateur trouvé.
                  </td>
                </tr>
              ) : (
                users.map((user) => (
                  <tr key={user.id}>
                    <td>#{user.id}</td>
                    <td style={{ fontWeight: 600 }}>{user.username}</td>
                    <td>{user.email}</td>
                    <td>
                      <span className={`badge ${
                        user.roleName === 'ADMIN' ? 'badge-danger' : 
                        user.roleName === 'HR' ? 'badge-success' : 'badge-info'
                      }`}>
                        {user.roleName}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                        <button className="btn btn-secondary btn-xs" title="Modifier" onClick={() => openModal('edit', user)}>
                          <Edit size={14} style={{ color: 'var(--primary)' }} />
                        </button>
                        <button 
                          className="btn btn-secondary btn-xs" 
                          title="Supprimer" 
                          disabled={user.username === currentUser.username}
                          style={{ opacity: user.username === currentUser.username ? 0.4 : 1 }}
                          onClick={() => handleDelete(user.id, user.username)}
                        >
                          <Trash2 size={14} style={{ color: '#EF4444' }} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Create / Edit Modal */}
      {modalOpen && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '500px' }}>
            <div className="modal-header">
              <h3 className="modal-title">
                {modalMode === 'create' ? 'Créer un utilisateur' : `Modifier l'utilisateur "${selectedUser?.username}"`}
              </h3>
              <button className="modal-close" onClick={closeModal}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Nom d'utilisateur</label>
                <input
                  type="text"
                  name="username"
                  className="form-input"
                  required
                  placeholder="ex: bilel"
                  value={formData.username}
                  onChange={handleInputChange}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Email</label>
                <input
                  type="email"
                  name="email"
                  className="form-input"
                  required
                  placeholder="ex: bilel@ooredoo.com"
                  value={formData.email}
                  onChange={handleInputChange}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Rôle</label>
                <select 
                  name="roleName" 
                  className="form-select" 
                  value={formData.roleName} 
                  onChange={handleInputChange}
                >
                  {roles.map(r => (
                    <option key={r.id} value={r.name}>{r.name}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">
                  Mot de passe {modalMode === 'edit' && <span style={{ color: 'var(--text-muted)', fontWeight: 'normal' }}>(laisser vide pour ne pas modifier)</span>}
                </label>
                <input
                  type="password"
                  name="password"
                  className="form-input"
                  required={modalMode === 'create'}
                  placeholder={modalMode === 'create' ? 'Mot de passe' : '••••••••'}
                  value={formData.password}
                  onChange={handleInputChange}
                />
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={closeModal}>Annuler</button>
                <button type="submit" className="btn btn-primary">Enregistrer</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManager;
