import React, { useState, useEffect } from 'react';
import { DynamicTurnoverService, EmployeeService, UserService, AlertService } from '../services/api';
import { Users, TrendingDown, DollarSign, BarChart3, Star, AlertTriangle, Target, ShieldCheck, Clock3 } from 'lucide-react';
import { normalizeAlertsResponse } from '../utils/alertResponse';

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({ department: 'all', riskLevel: 'all' });
  const [stats, setStats] = useState({
    totalEmployees: 0,
    totalUsers: 0,
    attritionRate: 0,
    avgIncome: 0,
    avgAge: 0,
    deptCounts: {},
    satisfactionAvg: 0,
    alertsCount: 0,
    averageRiskScore: 0,
    riskDistribution: { HIGH: 0, MEDIUM: 0, LOW: 0 },
    priorityEmployees: [],
    topFactors: [],
    recommendationCards: []
  });

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        setError('');

        const employees = await EmployeeService.getAll();
        let users = [];
        let alerts = [];

        try {
          users = await UserService.getAll();
        } catch (e) {
          users = [];
        }

        try {
          alerts = normalizeAlertsResponse(await AlertService.list());
        } catch (e) {
          alerts = [];
        }

        let dynamicScore = null;
        try {
          if (employees.length > 0) {
            dynamicScore = await DynamicTurnoverService.getScoreForEmployee(employees[0].id);
          }
        } catch (e) {
          dynamicScore = null;
        }

        const computedEmployees = (employees || []).map((employee) => {
          let score = 0;
          const reasons = [];

          if (employee.attrition) {
            score += 35;
            reasons.push('Historique d’attrition déjà observé');
          }
          if (employee.overtime) {
            score += 20;
            reasons.push('Heures supplémentaires fréquentes');
          }
          if (employee.monthlyIncome != null && employee.monthlyIncome < 2500) {
            score += 18;
            reasons.push('Salaire sous le seuil de confort');
          } else if (employee.monthlyIncome != null && employee.monthlyIncome < 4000) {
            score += 8;
            reasons.push('Salaire modéré');
          }
          if (employee.distanceFromHome != null && employee.distanceFromHome > 20) {
            score += 12;
            reasons.push('Distance domicile élevée');
          }
          if (employee.jobSatisfaction != null && employee.jobSatisfaction <= 2) {
            score += 18;
            reasons.push('Faible satisfaction au travail');
          }
          if (employee.environmentSatisfaction != null && employee.environmentSatisfaction <= 2) {
            score += 12;
            reasons.push('Faible satisfaction de l’environnement');
          }
          if (employee.age != null && employee.age < 30) {
            score += 5;
            reasons.push('Profil jeune et plus mobile');
          }
          if (employee.age != null && employee.age > 50) {
            score += 5;
            reasons.push('Risque de départ à la retraite');
          }

          const normalizedScore = Math.min(100, Math.round(score));
          let riskLevel = 'LOW';
          if (normalizedScore >= 70) riskLevel = 'HIGH';
          else if (normalizedScore >= 45) riskLevel = 'MEDIUM';

          return {
            ...employee,
            computedRiskScore: normalizedScore,
            riskLevel,
            reasons: reasons.slice(0, 3)
          };
        });

        const total = computedEmployees.length;
        const attritionCount = computedEmployees.filter((emp) => emp.attrition === true).length;
        const attritionRate = total > 0 ? ((attritionCount / total) * 100).toFixed(1) : '0.0';
        const totalIncome = computedEmployees.reduce((sum, emp) => sum + (emp.monthlyIncome || 0), 0);
        const avgIncome = total > 0 ? Math.round(totalIncome / total) : 0;
        const totalAge = computedEmployees.reduce((sum, emp) => sum + (emp.age || 0), 0);
        const avgAge = total > 0 ? (totalAge / total).toFixed(1) : '0.0';

        const depts = {};
        computedEmployees.forEach((emp) => {
          const department = emp.department || 'Non renseigné';
          depts[department] = (depts[department] || 0) + 1;
        });

        const totalSatisfaction = computedEmployees.reduce((sum, emp) => sum + ((emp.jobSatisfaction || 0) + (emp.environmentSatisfaction || 0)) / 2, 0);
        const satisfactionAvg = total > 0 ? (totalSatisfaction / total).toFixed(2) : '0.00';
        const averageRiskScore = total > 0 ? Math.round(computedEmployees.reduce((sum, emp) => sum + emp.computedRiskScore, 0) / total) : 0;

        const riskDistribution = { HIGH: 0, MEDIUM: 0, LOW: 0 };
        computedEmployees.forEach((emp) => {
          riskDistribution[emp.riskLevel] += 1;
        });

        const topFactors = [
          { label: 'Heures supplémentaires', value: computedEmployees.filter((emp) => emp.overtime).length, color: '#F59E0B' },
          { label: 'Faible satisfaction', value: computedEmployees.filter((emp) => (emp.jobSatisfaction || 0) <= 2 || (emp.environmentSatisfaction || 0) <= 2).length, color: '#3B82F6' },
          { label: 'Salaire faible', value: computedEmployees.filter((emp) => emp.monthlyIncome != null && emp.monthlyIncome < 2500).length, color: '#E4002B' },
          { label: 'Distance domicile', value: computedEmployees.filter((emp) => emp.distanceFromHome != null && emp.distanceFromHome > 20).length, color: '#10B981' }
        ].sort((a, b) => b.value - a.value);

        const recommendationCards = [
          {
            title: 'Entretiens ciblés',
            description: 'Prioriser les employés à risque élevé pour un entretien RH en 7 jours.',
            icon: <Target size={18} />,
            tone: 'primary'
          },
          {
            title: 'Révision du package',
            description: 'Revoir les compensations pour les profils à faible salaire dès que le risque dépasse 60%.',
            icon: <DollarSign size={18} />,
            tone: 'warning'
          },
          {
            title: 'Charge de travail',
            description: 'Limiter les heures supplémentaires pour les équipes avec forte saturation.',
            icon: <Clock3 size={18} />,
            tone: 'success'
          }
        ];

        const priorityEmployees = [...computedEmployees]
          .sort((a, b) => b.computedRiskScore - a.computedRiskScore)
          .slice(0, 6);

        setStats({
          totalEmployees: total,
          totalUsers: users.length || 0,
          attritionRate,
          avgIncome,
          avgAge,
          deptCounts: depts,
          satisfactionAvg,
          alertsCount: (alerts || []).filter((a) => a.status === 'NEW' || a.status === 'READ').length,
          averageRiskScore,
          riskDistribution,
          priorityEmployees,
          topFactors,
          recommendationCards,
          dynamicScore
        });
      } catch (e) {
        console.error('Erreur lors de la récupération des statistiques', e);
        setError('Les données ne sont pas disponibles pour le moment. Vérifiez la connexion au backend.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Chargement du tableau de bord RH...</p>
      </div>
    );
  }

  const deptEntries = Object.entries(stats.deptCounts || {});
  const maxDeptValue = Math.max(...deptEntries.map(([, count]) => count), 1);
  const visibleEmployees = (stats.priorityEmployees || []).filter((employee) => {
    const matchesDepartment = filters.department === 'all' || employee.department === filters.department;
    const matchesRisk = filters.riskLevel === 'all' || employee.riskLevel === filters.riskLevel;
    return matchesDepartment && matchesRisk;
  });

  const resetFilters = () => setFilters({ department: 'all', riskLevel: 'all' });

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem', flexWrap: 'wrap' }}>
        <div>
          <h1 className="title">Tableau de bord RH</h1>
          <p className="subtitle">Vue décisionnelle pour suivre les risques d’attrition et prioriser les actions.</p>
        </div>
        <div className="card" style={{ padding: '0.75rem 1rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <ShieldCheck size={18} color="var(--primary)" />
          <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-main)' }}>Aide à la décision RH</span>
        </div>
      </div>

      {error ? (
        <div className="card" style={{ marginBottom: '1.5rem', borderColor: '#F8B4B4', backgroundColor: '#FEF2F2' }}>
          <p style={{ color: '#9B1C1C', fontWeight: 600 }}>{error}</p>
        </div>
      ) : null}

      <div className="dashboard-toolbar card" style={{ marginBottom: '1.5rem' }}>
        <div className="form-group" style={{ marginBottom: 0 }}>
          <label className="form-label">Département</label>
          <select
            value={filters.department}
            onChange={(e) => setFilters((prev) => ({ ...prev, department: e.target.value }))}
            className="form-select"
          >
            <option value="all">Tous les départements</option>
            {deptEntries.map(([dept]) => (
              <option key={dept} value={dept}>{dept}</option>
            ))}
          </select>
        </div>

        <div className="form-group" style={{ marginBottom: 0 }}>
          <label className="form-label">Niveau de risque</label>
          <select
            value={filters.riskLevel}
            onChange={(e) => setFilters((prev) => ({ ...prev, riskLevel: e.target.value }))}
            className="form-select"
          >
            <option value="all">Tous les niveaux</option>
            <option value="HIGH">Élevé</option>
            <option value="MEDIUM">Moyen</option>
            <option value="LOW">Faible</option>
          </select>
        </div>

        <button className="btn btn-secondary" onClick={resetFilters}>Réinitialiser</button>
      </div>

      <div className="dashboard-kpis">
        <div className="card dashboard-kpi">
          <div className="dashboard-kpi-icon"><Users size={22} /></div>
          <div>
            <span>Effectif total</span>
            <strong>{stats.totalEmployees}</strong>
          </div>
        </div>

        <div className="card dashboard-kpi">
          <div className="dashboard-kpi-icon" style={{ backgroundColor: '#FEF2F2', color: '#EF4444' }}><TrendingDown size={22} /></div>
          <div>
            <span>Taux d’attrition</span>
            <strong>{stats.attritionRate}%</strong>
          </div>
        </div>

        <div className="card dashboard-kpi">
          <div className="dashboard-kpi-icon" style={{ backgroundColor: '#FFFBEB', color: '#F59E0B' }}><DollarSign size={22} /></div>
          <div>
            <span>Salaire moyen</span>
            <strong>{stats.avgIncome} DT</strong>
          </div>
        </div>

        <div className="card dashboard-kpi">
          <div className="dashboard-kpi-icon" style={{ backgroundColor: '#EFF6FF', color: '#3B82F6' }}><Star size={22} /></div>
          <div>
            <span>Satisfaction</span>
            <strong>{stats.satisfactionAvg} / 4</strong>
          </div>
        </div>

        <div className="card dashboard-kpi">
          <div className="dashboard-kpi-icon" style={{ backgroundColor: '#ECFDF5', color: '#10B981' }}><BarChart3 size={22} /></div>
          <div>
            <span>Score moyen</span>
            <strong>{stats.averageRiskScore}/100</strong>
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700 }}>Répartition par département</h3>
            <span className="badge badge-info">{stats.totalEmployees} employés</span>
          </div>
          <div className="dashboard-stack">
            {deptEntries.length === 0 ? (
              <p className="dashboard-empty">Aucune donnée disponible</p>
            ) : (
              deptEntries.map(([dept, count]) => {
                const percentage = ((count / stats.totalEmployees) * 100).toFixed(0);
                const widthPercent = ((count / maxDeptValue) * 100).toFixed(0);
                return (
                  <div key={dept} className="dashboard-progress-row">
                    <div className="dashboard-progress-labels">
                      <span>{dept}</span>
                      <span>{count} ({percentage}%)</span>
                    </div>
                    <div className="dashboard-progress-bar">
                      <div style={{ width: `${widthPercent}%` }} />
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>

        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700 }}>Facteurs répétés</h3>
            <span className="badge badge-warning">{stats.topFactors[0]?.value || 0} impacts</span>
          </div>
          <div className="dashboard-stack">
            {stats.topFactors.map((factor) => (
              <div key={factor.label} className="dashboard-factor">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span>{factor.label}</span>
                  <strong>{factor.value}</strong>
                </div>
                <div className="dashboard-progress-bar" style={{ marginTop: '0.5rem' }}>
                  <div style={{ width: `${Math.min(100, factor.value * 12)}%`, backgroundColor: factor.color }} />
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700 }}>Priorités d’action</h3>
            <span className="badge badge-danger">{visibleEmployees.length} ciblés</span>
          </div>
          {visibleEmployees.length === 0 ? (
            <p className="dashboard-empty">Aucun employé ne correspond à ce filtre. Essayez un autre niveau de risque.</p>
          ) : (
            <div className="dashboard-list">
              {visibleEmployees.map((employee) => (
                <div key={employee.id} className="dashboard-list-item">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '0.75rem' }}>
                    <div>
                      <strong>{employee.firstName} {employee.lastName}</strong>
                      <p>{employee.department || 'Département non renseigné'}</p>
                    </div>
                    <span className={`dashboard-risk-pill ${employee.riskLevel.toLowerCase()}`}>{employee.riskLevel === 'HIGH' ? 'Élevé' : employee.riskLevel === 'MEDIUM' ? 'Moyen' : 'Faible'}</span>
                  </div>
                  <div style={{ marginTop: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                    <span className="badge badge-info">Score {employee.computedRiskScore}/100</span>
                    {employee.reasons.map((reason) => (
                      <span key={reason} className="dashboard-chip">{reason}</span>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <AlertTriangle size={18} color="var(--primary)" />
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700 }}>Actions recommandées</h3>
          </div>
          <div className="dashboard-stack">
            {stats.recommendationCards.map((card) => (
              <div key={card.title} className={`dashboard-action-card ${card.tone}`}>
                <div className="dashboard-action-icon">{card.icon}</div>
                <div>
                  <strong>{card.title}</strong>
                  <p>{card.description}</p>
                </div>
              </div>
            ))}
          </div>
          <div className="dashboard-summary">
            <div>
              <span>Alertes actives</span>
              <strong>{stats.alertsCount}</strong>
            </div>
            <div>
              <span>Utilisateurs</span>
              <strong>{stats.totalUsers}</strong>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
