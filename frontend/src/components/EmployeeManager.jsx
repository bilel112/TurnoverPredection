import React, { useState, useEffect } from 'react';
import { DynamicTurnoverService, EmployeeService } from '../services/api';
import { Search, Plus, Edit, Trash2, Eye, X, Filter, BarChart3, Zap } from 'lucide-react';
import ScoringDetails from './ScoringDetails';

const EmployeeManager = ({ currentUser }) => {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [employeeScores, setEmployeeScores] = useState({});

  // Search & Filter State
  const [searchQuery, setSearchQuery] = useState('');
  const [deptFilter, setDeptFilter] = useState('');
  const [attritionFilter, setAttritionFilter] = useState('');
  const [salaryFilter, setSalaryFilter] = useState('');
  const [ageFilter, setAgeFilter] = useState('');

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState('view');
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [scoringModalOpen, setScoringModalOpen] = useState(false);
  const [scoringEmployee, setScoringEmployee] = useState(null);
  const [showScoringRules, setShowScoringRules] = useState(false);

  // Form State
  const [formData, setFormData] = useState({
    employeeNumber: '',
    age: '',
    gender: 'Male',
    department: 'Sales',
    jobRole: 'Sales Executive',
    monthlyIncome: '',
    yearsAtCompany: '',
    distanceFromHome: '',
    overtime: false,
    jobSatisfaction: 3,
    environmentSatisfaction: 3,
    attrition: false,
    businessTravel: 'Travel_Rarely',
    maritalStatus: 'Single',
    jobLevel: 1,
    totalWorkingYears: '',
    yearsInCurrentRole: '',
    yearsWithCurrManager: '',
    yearsSinceLastPromotion: '',
    stockOptionLevel: 0,
    numCompaniesWorked: 1,
    trainingTimesLastYear: 3,
    workLifeBalance: 3,
    educationField: 'Life Sciences',
    education: 3,
  });

  useEffect(() => {
    if (!searchQuery && !deptFilter && !attritionFilter && !salaryFilter && !ageFilter) {
      fetchEmployees();
    }
  }, [currentPage, pageSize]);

  const hasRole = (roles) => {
    const r = (currentUser?.roleName || '').toString().trim().toUpperCase();
    return roles.map(x => x.toUpperCase()).includes(r);
  };

  const loadScoresForEmployees = async (employeeList) => {
    if (!employeeList || employeeList.length === 0) {
      setEmployeeScores({});
      return;
    }

    const scoreMap = {};
    await Promise.allSettled(employeeList.map(async (emp) => {
      if (!emp?.id) return;
      try {
        const score = await DynamicTurnoverService.getScoreForEmployee(emp.id);
        scoreMap[emp.id] = score;
      } catch (error) {
        scoreMap[emp.id] = null;
      }
    }));

    setEmployeeScores(prev => ({ ...prev, ...scoreMap }));
  };

  const fetchEmployees = async () => {
    try {
      setLoading(true);
      const data = await EmployeeService.getPaginated(currentPage, pageSize);
      const nextEmployees = data.content || [];
      setEmployees(nextEmployees);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
      await loadScoresForEmployees(nextEmployees);
    } catch (error) {
      console.error("Erreur lors de la récupération des employés", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      const all = await EmployeeService.getAll();
      let filtered = all;

      if (searchQuery) {
        filtered = filtered.filter(emp =>
          (emp.employeeNumber && emp.employeeNumber.toString().includes(searchQuery)) ||
          (emp.jobRole && emp.jobRole.toLowerCase().includes(searchQuery.toLowerCase()))
        );
      }

      if (deptFilter) {
        filtered = filtered.filter(emp => emp.department === deptFilter);
      }

      if (attritionFilter) {
        const isAttrition = attritionFilter === 'Yes';
        filtered = filtered.filter(emp => emp.attrition === isAttrition);
      }

      if (salaryFilter) {
        filtered = filtered.filter(emp => emp.monthlyIncome >= Number(salaryFilter));
      }

      if (ageFilter) {
        filtered = filtered.filter(emp => emp.age >= Number(ageFilter));
      }

      const startIndex = currentPage * pageSize;
      const nextEmployees = filtered.slice(startIndex, startIndex + pageSize);
      setEmployees(nextEmployees);
      setTotalPages(Math.ceil(filtered.length / pageSize));
      setTotalElements(filtered.length);
      setCurrentPage(0);
      await loadScoresForEmployees(nextEmployees);
    } catch (error) {
      console.error("Erreur filtrage", error);
    } finally {
      setLoading(false);
    }
  };

  const handleResetFilters = () => {
    setSearchQuery('');
    setDeptFilter('');
    setAttritionFilter('');
    setSalaryFilter('');
    setAgeFilter('');
    setCurrentPage(0);
    fetchEmployees();
  };

  const openModal = (mode, employee = null) => {
    setModalMode(mode);
    if (employee) {
      setSelectedEmployee(employee);
      setFormData({ ...employee });
    } else {
      setSelectedEmployee(null);
      setFormData({
        employeeNumber: Math.floor(1000 + Math.random() * 9000),
        age: 30,
        gender: 'Male',
        department: 'Sales',
        jobRole: 'Sales Executive',
        monthlyIncome: 4500,
        yearsAtCompany: 3,
        distanceFromHome: 5,
        overtime: false,
        jobSatisfaction: 3,
        environmentSatisfaction: 3,
        attrition: false,
        businessTravel: 'Travel_Rarely',
        maritalStatus: 'Single',
        jobLevel: 1,
        totalWorkingYears: 5,
        yearsInCurrentRole: 2,
        yearsWithCurrManager: 2,
        yearsSinceLastPromotion: 1,
        stockOptionLevel: 0,
        numCompaniesWorked: 1,
        trainingTimesLastYear: 3,
        workLifeBalance: 3,
        educationField: 'Life Sciences',
        education: 3,
      });
    }
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setSelectedEmployee(null);
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : (type === 'number' ? Number(value) : value)
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (modalMode === 'create') {
        await EmployeeService.create(formData);
      } else if (modalMode === 'edit') {
        await EmployeeService.update(selectedEmployee.id, formData);
      }
      fetchEmployees();
      closeModal();
    } catch (error) {
      alert("Erreur lors de la sauvegarde : " + (error.response?.data?.message || error.message));
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm("Êtes-vous sûr de vouloir supprimer cet employé ?")) {
      try {
        await EmployeeService.delete(id);
        fetchEmployees();
      } catch (error) {
        alert("Erreur de suppression");
      }
    }
  };

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 className="title">Gestion des Employés</h1>
          <p className="subtitle">Consultez, modifiez et ajoutez les fiches employés</p>
        </div>

        {hasRole(['HR','ADMIN']) && (
          <button className="btn btn-primary" onClick={() => openModal('create')}>
            <Plus size={18} />
            <span>Ajouter un Employé</span>
          </button>
        )}
      </div>

      {/* Filters Card */}
      <form onSubmit={handleSearch} className="card" style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end', marginBottom: '2rem' }}>
        <div style={{ flex: 1, minWidth: '200px' }}>
          <label className="form-label">Recherche</label>
          <div style={{ position: 'relative' }}>
            <Search size={16} style={{ position: 'absolute', left: '10px', top: '12px', color: 'var(--text-muted)' }} />
            <input
              type="text"
              placeholder="Numéro ou Rôle..."
              className="form-input"
              style={{ paddingLeft: '32px' }}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </div>

        <div style={{ minWidth: '150px' }}>
          <label className="form-label">Département</label>
          <select className="form-select" value={deptFilter} onChange={(e) => setDeptFilter(e.target.value)}>
            <option value="">Tous</option>
            <option value="Sales">Sales</option>
            <option value="Research & Development">Research & Development</option>
            <option value="Human Resources">Human Resources</option>
          </select>
        </div>

        <div style={{ minWidth: '130px' }}>
          <label className="form-label">Attrition (Départ)</label>
          <select className="form-select" value={attritionFilter} onChange={(e) => setAttritionFilter(e.target.value)}>
            <option value="">Tous</option>
            <option value="Yes">Oui</option>
            <option value="No">Non</option>
          </select>
        </div>

        <div style={{ minWidth: '150px' }}>
          <label className="form-label">Salaire (min)</label>
          <input
            type="number"
            className="form-input"
            placeholder="Ex: 3000"
            value={salaryFilter}
            onChange={(e) => setSalaryFilter(e.target.value)}
          />
        </div>

        <div style={{ minWidth: '150px' }}>
          <label className="form-label">Âge (min)</label>
          <input
            type="number"
            className="form-input"
            placeholder="Ex: 25"
            value={ageFilter}
            onChange={(e) => setAgeFilter(e.target.value)}
          />
        </div>

        <button type="submit" className="btn btn-secondary">
          <Filter size={16} />
          <span>Filtrer</span>
        </button>

        {(searchQuery || deptFilter || attritionFilter || salaryFilter || ageFilter) && (
          <button type="button" className="btn btn-secondary" onClick={handleResetFilters}>
            Réinitialiser
          </button>
        )}
      </form>

      {/* Grid / Table */}
      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Chargement des employés...</p>
        </div>
      ) : (
        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>N° Employé</th>
                <th>Âge / Genre</th>
                <th>Département</th>
                <th>Poste</th>
                <th>Salaire Mensuel</th>
                <th>Score Dyn.</th>
                <th>Attrition</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {employees.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                    Aucun employé trouvé.
                  </td>
                </tr>
              ) : (
                employees.map((emp) => (
                  <tr key={emp.id}>
                    <td style={{ fontWeight: 600 }}>#{emp.employeeNumber || emp.id}</td>
                    <td>{emp.age} ans / {emp.gender === 'Female' ? 'F' : 'H'}</td>
                    <td>{emp.department}</td>
                    <td style={{ fontWeight: 500 }}>{emp.jobRole}</td>
                    <td>{emp.monthlyIncome?.toLocaleString()} DT</td>
                    <td>
                      {employeeScores[emp.id] ? (
                        <span
                          className={`badge ${employeeScores[emp.id].riskLevel === 'HIGH' ? 'badge-danger' : employeeScores[emp.id].riskLevel === 'MEDIUM' ? 'badge-warning' : 'badge-success'}`}
                          style={{ minWidth: '110px', justifyContent: 'center' }}
                        >
                          {employeeScores[emp.id].score} · {employeeScores[emp.id].riskLabel}
                        </span>
                      ) : (
                        <span style={{ color: 'var(--text-muted)' }}>—</span>
                      )}
                    </td>
                    <td>
                      <span className={`badge ${emp.attrition ? 'badge-danger' : 'badge-success'}`}>
                        {emp.attrition ? 'Oui' : 'Non'}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                        {hasRole(['HR','MANAGER','ADMIN']) && (
                          <>
                            <button className="btn btn-secondary btn-xs" title="Scoring" onClick={() => {
                              setScoringEmployee(emp);
                              setScoringModalOpen(true);
                            }}>
                              <BarChart3 size={14} />
                            </button>
                            <button className="btn btn-secondary btn-xs" title="Forcer scoring" onClick={async () => {
                              try {
                                setLoading(true);
                                await DynamicTurnoverService.getScoreForEmployee(emp.id);
                                fetchEmployees();
                              } catch (error) {
                                console.error('Erreur lors du scoring forcé', error);
                                alert('Impossible de forcer le scoring. Vérifie le backend.');
                              } finally {
                                setLoading(false);
                              }
                            }}>
                              <Zap size={14} />
                            </button>
                            <button className="btn btn-secondary btn-xs" title="Voir détails" onClick={() => openModal('view', emp)}>
                              <Eye size={14} />
                            </button>
                          </>
                        )}
                        {hasRole(['HR','ADMIN']) && (
                          <>
                            <button className="btn btn-secondary btn-xs" title="Modifier" onClick={() => openModal('edit', emp)}>
                              <Edit size={14} style={{ color: 'var(--primary)' }} />
                            </button>
                            <button className="btn btn-secondary btn-xs" title="Supprimer" onClick={() => handleDelete(emp.id)}>
                              <Trash2 size={14} style={{ color: '#EF4444' }} />
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          {/* Scoring Rules Button and Section */}
          <div style={{ marginTop: '1.5rem', textAlign: 'left' }}>
            <button
              className="btn btn-ghost"
              onClick={() => setShowScoringRules(prev => !prev)}
              style={{
                padding: '0.75rem 1.5rem',
                fontWeight: 600,
                border: '1px dashed var(--border-color)',
                borderRadius: 'var(--radius-md)',
                transition: 'all 0.2s ease'
              }}
            >
              {showScoringRules ? 'Masquer les règles de scoring' : 'Afficher les règles de scoring'}
            </button>

            {showScoringRules && (
              <div
                className="card"
                style={{
                  marginTop: '1rem',
                  padding: '1.5rem',
                  maxWidth: '100%',
                  boxShadow: '0 4px 6px rgba(0, 0, 0, 0.05)',
                  border: '1px solid var(--border-color)',
                  fontSize: '1rem'
                }}
              >
                <h4 style={{
                  fontSize: '1.2rem',
                  fontWeight: 700,
                  marginBottom: '1.5rem',
                  color: 'var(--text-primary)',
                  borderBottom: '2px solid var(--primary)',
                  paddingBottom: '0.75rem'
                }}>
                  Critères de calcul du score dynamique
                </h4>
                <ul style={{
                  fontSize: '1rem',
                  margin: 0,
                  paddingLeft: '1.5rem',
                  lineHeight: '1.8'
                }}>
                  <li><strong>Ancienneté courte (≤1 an) :</strong> +20 points</li>
                  <li><strong>Beaucoup d'emplois antérieurs (≥4) :</strong> +15 points</li>
                  <li><strong>Heures supplémentaires :</strong> +12 points</li>
                  <li><strong>Salaire bas (&lt;3000 DT) :</strong> +14 points</li>
                  <li><strong>Faible satisfaction travail (≤2) :</strong> +12 points</li>
                  <li><strong>Faible satisfaction environnement (≤2) :</strong> +8 points</li>
                  <li><strong>Pas de promotion récente (≥3 ans) :</strong> +10 points</li>
                  <li><strong>Faible niveau d'options (≤0) :</strong> +9 points</li>
                </ul>
                <p style={{
                  marginTop: '1.5rem',
                  fontSize: '0.95rem',
                  color: 'var(--text-muted)',
                  fontStyle: 'italic'
                }}>
                  <strong>Note :</strong> Plus le score est élevé, plus le risque d'attrition est grand.
                </p>
              </div>
            )}
          </div>

          {/* Pagination Footer */}
          <div className="pagination">
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
              Affichage de <strong>{employees.length}</strong> sur <strong>{totalElements}</strong> employés
            </span>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button
                className="btn btn-secondary btn-xs"
                disabled={currentPage === 0}
                onClick={() => handlePageChange(currentPage - 1)}
              >
                Précédent
              </button>
              <span style={{ display: 'flex', alignItems: 'center', fontSize: '0.875rem', padding: '0 0.5rem' }}>
                Page {currentPage + 1} sur {totalPages || 1}
              </span>
              <button
                className="btn btn-secondary btn-xs"
                disabled={currentPage >= (totalPages || 1) - 1}
                onClick={() => handlePageChange(currentPage + 1)}
              >
                Suivant
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Scoring Details Modal */}
      {scoringModalOpen && scoringEmployee && (
        <ScoringDetails
          employeeId={scoringEmployee.id}
          employeeName={`${scoringEmployee.jobRole} - #${scoringEmployee.employeeNumber}`}
          employee={scoringEmployee}
          onClose={() => {
            setScoringModalOpen(false);
            setScoringEmployee(null);
          }}
        />
      )}

      {/* CRUD Modal Dialog */}
      {modalOpen && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '800px' }}>
            <div className="modal-header">
              <h3 className="modal-title">
                {modalMode === 'view' && `Fiche Employé #${formData.employeeNumber}`}
                {modalMode === 'create' && 'Ajouter un nouvel employé'}
                {modalMode === 'edit' && `Modifier l'employé #${formData.employeeNumber}`}
              </h3>
              <button className="modal-close" onClick={closeModal}>
                <X size={20} />
              </button>
            </div>

            {modalMode === 'view' ? (
              /* View Mode Detailed Grid */
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(3, 1fr)',
                  gap: '1rem',
                  padding: '1rem',
                  backgroundColor: 'var(--bg-primary)',
                  borderRadius: 'var(--radius-md)'
                }}>
                  <div>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>Numéro</span>
                    <strong style={{ fontSize: '1rem' }}>#{formData.employeeNumber}</strong>
                  </div>
                  <div>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>Âge & Genre</span>
                    <strong style={{ fontSize: '1rem' }}>{formData.age} ans ({formData.gender})</strong>
                  </div>
                  <div>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>Statut Marital</span>
                    <strong style={{ fontSize: '1rem' }}>{formData.maritalStatus}</strong>
                  </div>
                </div>

                <div>
                  <h4 style={{ fontSize: '0.9rem', fontWeight: 700, borderBottom: '1px solid var(--border-color)', paddingBottom: '0.25rem', marginBottom: '0.75rem' }}>
                    Informations Professionnelles
                  </h4>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '0.75rem', fontSize: '0.875rem' }}>
                    <div><strong>Département :</strong> {formData.department}</div>
                    <div><strong>Poste :</strong> {formData.jobRole}</div>
                    <div><strong>Salaire Mensuel :</strong> {formData.monthlyIncome?.toLocaleString()} DT</div>
                    <div><strong>Niveau de Poste :</strong> Niveau {formData.jobLevel}</div>
                    <div><strong>Années dans la boîte :</strong> {formData.yearsAtCompany} ans</div>
                    <div><strong>Années d'expérience total :</strong> {formData.totalWorkingYears} ans</div>
                  </div>
                </div>

                <div>
                  <h4 style={{ fontSize: '0.9rem', fontWeight: 700, borderBottom: '1px solid var(--border-color)', paddingBottom: '0.25rem', marginBottom: '0.75rem' }}>
                    Enquêtes de Satisfaction & Conditions
                  </h4>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '0.75rem', fontSize: '0.875rem' }}>
                    <div><strong>Satisfaction au travail :</strong> {formData.jobSatisfaction} / 4</div>
                    <div><strong>Satisfaction environnement :</strong> {formData.environmentSatisfaction} / 4</div>
                    <div><strong>Équilibre vie pro/perso :</strong> {formData.workLifeBalance} / 4</div>
                    <div><strong>Heures supplémentaires :</strong> {formData.overtime ? 'Oui' : 'Non'}</div>
                    <div><strong>Distance du domicile :</strong> {formData.distanceFromHome} km</div>
                    <div><strong>Voyage d'affaires :</strong> {formData.businessTravel}</div>
                  </div>
                </div>

                <div style={{
                  padding: '1rem',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: formData.attrition ? 'var(--primary-light)' : '#E6F4EA',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}>
                  <div>
                    <strong style={{ fontSize: '0.9rem', color: formData.attrition ? 'var(--primary)' : '#137333' }}>
                      Statut d'Attrition (Départ de l'employé)
                    </strong>
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.15rem' }}>
                      Indique si le collaborateur a quitté l'entreprise.
                    </p>
                  </div>
                  <span className={`badge ${formData.attrition ? 'badge-danger' : 'badge-success'}`} style={{ padding: '0.5rem 1rem', fontSize: '0.9rem' }}>
                    {formData.attrition ? 'Oui (Départ)' : 'Non (Actif)'}
                  </span>
                </div>

                <div className="modal-footer">
                  <button className="btn btn-secondary" onClick={closeModal}>Fermer</button>
                  {hasRole(['HR','ADMIN']) && (
                    <button className="btn btn-primary" onClick={() => setModalMode('edit')}>Modifier</button>
                  )}
                </div>
              </div>
            ) : (
              /* Create or Edit Form */
              <form onSubmit={handleSubmit}>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>

                  {/* General details */}
                  <div className="form-group">
                    <label className="form-label">Numéro d'Employé</label>
                    <input
                      type="number"
                      name="employeeNumber"
                      className="form-input"
                      required
                      value={formData.employeeNumber}
                      onChange={handleInputChange}
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Âge</label>
                    <input
                      type="number"
                      name="age"
                      className="form-input"
                      required
                      value={formData.age}
                      onChange={handleInputChange}
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Genre</label>
                    <select name="gender" className="form-select" value={formData.gender} onChange={handleInputChange}>
                      <option value="Male">Homme</option>
                      <option value="Female">Femme</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Statut Marital</label>
                    <select name="maritalStatus" className="form-select" value={formData.maritalStatus} onChange={handleInputChange}>
                      <option value="Single">Célibataire</option>
                      <option value="Married">Marié(e)</option>
                      <option value="Divorced">Divorcé(e)</option>
                    </select>
                  </div>

                  {/* Professional details */}
                  <div className="form-group">
                    <label className="form-label">Département</label>
                    <select name="department" className="form-select" value={formData.department} onChange={handleInputChange}>
                      <option value="Sales">Sales</option>
                      <option value="Research & Development">Research & Development</option>
                      <option value="Human Resources">Human Resources</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Poste</label>
                    <input
                      type="text"
                      name="jobRole"
                      className="form-input"
                      required
                      value={formData.jobRole}
                      onChange={handleInputChange}
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Salaire Mensuel (DT)</label>
                    <input
                      type="number"
                      name="monthlyIncome"
                      className="form-input"
                      required
                      value={formData.monthlyIncome}
                      onChange={handleInputChange}
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Années dans la boîte</label>
                    <input
                      type="number"
                      name="yearsAtCompany"
                      className="form-input"
                      required
                      value={formData.yearsAtCompany}
                      onChange={handleInputChange}
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Années d'expérience total</label>
                    <input
                      type="number"
                      name="totalWorkingYears"
                      className="form-input"
                      required
                      value={formData.totalWorkingYears}
                      onChange={handleInputChange}
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Distance du domicile (km)</label>
                    <input
                      type="number"
                      name="distanceFromHome"
                      className="form-input"
                      required
                      value={formData.distanceFromHome}
                      onChange={handleInputChange}
                    />
                  </div>

                  {/* Satisfaction sliders or dropdowns */}
                  <div className="form-group">
                    <label className="form-label">Satisfaction Professionnelle (1-4)</label>
                    <select name="jobSatisfaction" className="form-select" value={formData.jobSatisfaction} onChange={handleInputChange}>
                      <option value={1}>1 - Très Insatisfait</option>
                      <option value={2}>2 - Insatisfait</option>
                      <option value={3}>3 - Satisfait</option>
                      <option value={4}>4 - Très Satisfait</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Satisfaction Environnement (1-4)</label>
                    <select name="environmentSatisfaction" className="form-select" value={formData.environmentSatisfaction} onChange={handleInputChange}>
                      <option value={1}>1 - Très Insatisfait</option>
                      <option value={2}>2 - Insatisfait</option>
                      <option value={3}>3 - Satisfait</option>
                      <option value={4}>4 - Très Satisfait</option>
                    </select>
                  </div>

                  <div className="form-group" style={{ gridColumn: 'span 2', display: 'flex', gap: '2rem', marginTop: '0.5rem' }}>
                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontSize: '0.9rem' }}>
                      <input
                        type="checkbox"
                        name="overtime"
                        checked={formData.overtime}
                        onChange={handleInputChange}
                        style={{ accentColor: 'var(--primary)', width: '16px', height: '16px' }}
                      />
                      Fait des heures supplémentaires (Overtime)
                    </label>

                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontSize: '0.9rem' }}>
                      <input
                        type="checkbox"
                        name="attrition"
                        checked={formData.attrition}
                        onChange={handleInputChange}
                        style={{ accentColor: 'var(--primary)', width: '16px', height: '16px' }}
                      />
                      A quitté l'entreprise (Attrition)
                    </label>
                  </div>
                </div>

                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={closeModal}>Annuler</button>
                  <button type="submit" className="btn btn-primary">Enregistrer</button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default EmployeeManager;