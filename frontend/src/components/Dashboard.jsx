import React, { useState, useEffect } from 'react';
import { DynamicTurnoverService, EmployeeService, UserService } from '../services/api';
import { Users, UserCheck, TrendingDown, DollarSign, BarChart3, Star } from 'lucide-react';

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalEmployees: 0,
    totalUsers: 0,
    attritionRate: 0,
    avgIncome: 0,
    avgAge: 0,
    deptCounts: {},
    satisfactionAvg: 0,
    dynamicScore: null
  });

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const employees = await EmployeeService.getAll();
        let users = [];
        try {
          users = await UserService.getAll();
        } catch (e) {
          // ignore users error (likely forbidden)
          users = [];
        }

        let dynamicScore = null;
        try {
          if (employees.length > 0) {
            dynamicScore = await DynamicTurnoverService.getScoreForEmployee(employees[0].id);
          }
        } catch (e) {
          dynamicScore = null;
        }

        if (employees.length > 0) {
          const total = employees.length;
          const attritionCount = employees.filter(emp => emp.attrition === true).length;
          const attritionRate = ((attritionCount / total) * 100).toFixed(1);
          
          const totalIncome = employees.reduce((sum, emp) => sum + (emp.monthlyIncome || 0), 0);
          const avgIncome = (totalIncome / total).toFixed(0);

          const totalAge = employees.reduce((sum, emp) => sum + (emp.age || 0), 0);
          const avgAge = (totalAge / total).toFixed(1);

          // Department distribution
          const depts = {};
          employees.forEach(emp => {
            if (emp.department) {
              depts[emp.department] = (depts[emp.department] || 0) + 1;
            }
          });

          // Environment and Job satisfaction average (1-4 scale)
          const totalSatisfaction = employees.reduce((sum, emp) => 
            sum + ((emp.jobSatisfaction || 0) + (emp.environmentSatisfaction || 0)) / 2, 0);
          const satisfactionAvg = (totalSatisfaction / total).toFixed(2);

          setStats({
            totalEmployees: total,
            totalUsers: users.length || 0,
            attritionRate,
            avgIncome,
            avgAge,
            deptCounts: depts,
            satisfactionAvg,
            dynamicScore
          });
        // Load alerts count
        try {
          const alerts = await (await import('../services/api')).AlertService.list();
          const activeCount = (alerts || []).filter(a => a.status === 'NEW' || a.status === 'READ').length;
          setStats(prev => ({ ...prev, alertsCount: activeCount }));
        } catch (e) {
          // ignore
          setStats(prev => ({ ...prev, alertsCount: 0 }));
        }
        } else {
          setStats({
            totalEmployees: 0,
            totalUsers: users.length,
            attritionRate: 0,
            avgIncome: 0,
            avgAge: 0,
            deptCounts: {},
            satisfactionAvg: 0,
            dynamicScore: null
          });
        }
      } catch (error) {
        console.error("Erreur lors de la récupération des statistiques", error);
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
        <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Chargement des statistiques en cours...</p>
      </div>
    );
  }

  // Calculate percentages for SVG bar chart
  const deptEntries = Object.entries(stats.deptCounts);
  const maxDeptValue = Math.max(...deptEntries.map(([_, count]) => count), 1);

  return (
    <div>
      <h1 className="title">Dashboard RH</h1>
      <p className="subtitle">Aperçu analytique des ressources humaines de Ooredoo</p>

      {/* KPI Cards Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
        gap: '1.5rem',
        marginBottom: '2rem'
      }}>
        {/* Total Employees */}
        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          <div style={{
            width: '48px',
            height: '48px',
            borderRadius: '12px',
            backgroundColor: 'var(--primary-light)',
            color: 'var(--primary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <Users size={24} />
          </div>
          <div>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500 }}>Effectif Total</span>
            <h3 style={{ fontSize: '1.5rem', fontWeight: 700, marginTop: '2px' }}>{stats.totalEmployees}</h3>
          </div>
        </div>

        {/* Attrition Rate */}
        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          <div style={{
            width: '48px',
            height: '48px',
            borderRadius: '12px',
            backgroundColor: stats.attritionRate > 15 ? '#FEF2F2' : '#ECFDF5',
            color: stats.attritionRate > 15 ? '#EF4444' : '#10B981',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <TrendingDown size={24} />
          </div>
          <div>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500 }}>Taux d'Attrition</span>
            <h3 style={{ fontSize: '1.5rem', fontWeight: 700, marginTop: '2px' }}>
              {stats.attritionRate}%
            </h3>
          </div>
        </div>

        {/* Avg Monthly Income */}
        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          <div style={{
            width: '48px',
            height: '48px',
            borderRadius: '12px',
            backgroundColor: '#FFFBEB',
            color: '#F59E0B',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <DollarSign size={24} />
          </div>
          <div>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500 }}>Salaire Moyen</span>
            <h3 style={{ fontSize: '1.5rem', fontWeight: 700, marginTop: '2px' }}>{stats.avgIncome} DT</h3>
          </div>
        </div>

        {/* Satisfaction Score */}
        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          <div style={{
            width: '48px',
            height: '48px',
            borderRadius: '12px',
            backgroundColor: '#EFF6FF',
            color: '#3B82F6',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <Star size={24} />
          </div>
          <div>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500 }}>Satisfaction Moyenne</span>
            <h3 style={{ fontSize: '1.5rem', fontWeight: 700, marginTop: '2px' }}>{stats.satisfactionAvg} / 4</h3>
          </div>
        </div>

        {/* Dynamic Scoring Card */}
        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          <div style={{
            width: '48px',
            height: '48px',
            borderRadius: '12px',
            backgroundColor: stats.dynamicScore?.riskLevel === 'HIGH' ? '#FEE2E2' : stats.dynamicScore?.riskLevel === 'MEDIUM' ? '#FEF3C7' : '#ECFDF5',
            color: stats.dynamicScore?.riskLevel === 'HIGH' ? '#B91C1C' : stats.dynamicScore?.riskLevel === 'MEDIUM' ? '#B45309' : '#16A34A',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <BarChart3 size={24} />
          </div>
          <div>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500 }}>Scoring Dynamique</span>
            <h3 style={{ fontSize: '1.5rem', fontWeight: 700, marginTop: '2px' }}>
              {stats.dynamicScore ? `${stats.dynamicScore.score} · ${stats.dynamicScore.riskLabel}` : 'N/A'}
            </h3>
            <div style={{ marginTop: '6px', fontSize: '0.85rem', color: 'var(--text-muted)' }}>Alertes actives : {stats.alertsCount || 0}</div>
          </div>
        </div>
      </div>

      {/* Analytics Charts & Details */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(450px, 1fr))',
        gap: '2rem'
      }}>
        {/* Department Distribution Chart */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem' }}>
            <BarChart3 size={20} style={{ color: 'var(--primary)' }} />
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Répartition par Département</h3>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            {deptEntries.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Aucune donnée disponible</p>
            ) : (
              deptEntries.map(([dept, count]) => {
                const percentage = ((count / stats.totalEmployees) * 100).toFixed(0);
                const widthPercent = ((count / maxDeptValue) * 100).toFixed(0);
                return (
                  <div key={dept} style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>{dept}</span>
                      <span style={{ color: 'var(--text-muted)' }}>{count} ({percentage}%)</span>
                    </div>
                    <div style={{
                      width: '100%',
                      height: '8px',
                      backgroundColor: '#F3F4F6',
                      borderRadius: '4px',
                      overflow: 'hidden'
                    }}>
                      <div style={{
                        width: `${widthPercent}%`,
                        height: '100%',
                        backgroundColor: 'var(--primary)',
                        borderRadius: '4px',
                        transition: 'width 1s ease-out'
                      }}></div>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>

        {/* Information and Quick Tips */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          <div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem', color: 'var(--text-main)' }}>
              Indicateurs Clés d'Attrition
            </h3>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
              Le modèle d'intelligence artificielle (à intégrer ultérieurement) utilisera les facteurs suivants pour prédire la probabilité de départ d'un employé :
            </p>
            
            <ul style={{
              fontSize: '0.875rem',
              color: '#4B5563',
              paddingLeft: '1.25rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.625rem'
            }}>
              <li><strong>Heures Supplémentaires (Overtime) :</strong> Un facteur prédominant dans les départs volontaires.</li>
              <li><strong>Distance du domicile (DistanceFromHome) :</strong> Les trajets longs augmentent significativement le taux d'attrition.</li>
              <li><strong>Salaire Mensuel :</strong> Les bas salaires présentent une sensibilité accrue au départ.</li>
              <li><strong>Satisfaction Environnementale :</strong> Une mauvaise note (1 ou 2) influe négativement sur la fidélisation.</li>
            </ul>
          </div>

          <div style={{
            marginTop: '1.5rem',
            padding: '1rem',
            backgroundColor: 'var(--primary-light)',
            borderRadius: 'var(--radius-sm)',
            borderLeft: '4px solid var(--primary)'
          }}>
            <h4 style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--primary)' }}>
              Note Importante
            </h4>
            <p style={{ fontSize: '0.8rem', color: '#B91C1C', marginTop: '0.25rem' }}>
              Les données affichées proviennent directement du dataset IBM HR Attrition stocké dans votre base de données MySQL.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
