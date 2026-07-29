import React, { useState, useEffect } from 'react';
import { DynamicTurnoverService } from '../services/api';
import { AlertCircle, Calendar, X, ChevronDown, ChevronUp } from 'lucide-react';

const ScoringDetails = ({ employeeId, employeeName, employee = {}, onClose }) => {
  const [score, setScore] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expandedHistoryItem, setExpandedHistoryItem] = useState(null);

  useEffect(() => {
    if (employeeId) loadScoringData();
  }, [employeeId]);

  const loadScoringData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [scoreData, historyData] = await Promise.all([
        DynamicTurnoverService.getScoreForEmployee(employeeId),
        DynamicTurnoverService.getScoreHistoryForEmployee(employeeId),
      ]);
      setScore(scoreData);
      setHistory(historyData || []);
    } catch (err) {
      console.error('Erreur lors du chargement du scoring', err);
      setError(err?.response?.data?.message || err.message || 'Erreur lors du chargement');
    } finally {
      setLoading(false);
    }
  };

  const parsedReasons = (reasons) => {
    if (!reasons) return [];
    if (Array.isArray(reasons)) return reasons;
    return reasons
      .split(/;|,|\n|\||\//)
      .map((item) => item.trim())
      .filter(Boolean);
  };

  const scoringWeights = {
    shortTenure: 20,
    manyCompanies: 15,
    overtime: 12,
    lowSalary: 14,
    lowJobSatisfaction: 12,
    lowEnvironmentSatisfaction: 8,
    noRecentPromotion: 10,
    lowStockOption: 9,
  };

  const computeBreakdown = (emp = {}) => {
    const parts = [];

    const yearsAtCompany = Number(
      emp.yearsAtCompany ?? emp.tenureYears ?? (emp.tenureMonths != null ? Math.round(emp.tenureMonths / 12) : 0)
    );
    const numCompaniesWorked = Number(
      emp.numCompaniesWorked ?? emp.previousJobs ?? emp.numCompaniesWorked ?? 0
    );
    const overtime = Boolean(emp.overtime ?? emp.overtimeHours ?? false);
    const monthlyIncome = Number(emp.monthlyIncome ?? emp.salary ?? 999999);
    const jobSatisfaction = Number(emp.jobSatisfaction ?? 5);
    const environmentSatisfaction = Number(emp.environmentSatisfaction ?? 5);
    const yearsSinceLastPromotion = Number(emp.yearsSinceLastPromotion ?? emp.yearsSincePromotion ?? 0);
    const stockOptionLevel = Number(emp.stockOptionLevel ?? emp.stockOptions ?? 1);

    if (yearsAtCompany <= 1) {
      parts.push({ name: 'Ancienneté courte (≤1 an)', points: scoringWeights.shortTenure });
    }
    if (numCompaniesWorked >= 4) {
      parts.push({ name: "Beaucoup d'emplois antérieurs (≥4)", points: scoringWeights.manyCompanies });
    }
    if (overtime) {
      parts.push({ name: 'Heures supplémentaires', points: scoringWeights.overtime });
    }
    if (monthlyIncome < 3000) {
      parts.push({ name: 'Salaire bas (<3000)', points: scoringWeights.lowSalary });
    }
    if (jobSatisfaction <= 2) {
      parts.push({ name: 'Faible satisfaction travail (≤2)', points: scoringWeights.lowJobSatisfaction });
    }
    if (environmentSatisfaction <= 2) {
      parts.push({ name: 'Faible satisfaction environnement', points: scoringWeights.lowEnvironmentSatisfaction });
    }
    if (yearsSinceLastPromotion >= 3) {
      parts.push({ name: 'Pas de promotion récente (≥3 ans)', points: scoringWeights.noRecentPromotion });
    }
    if (stockOptionLevel <= 0) {
      parts.push({ name: 'Faible niveau d\'options (≤0)', points: scoringWeights.lowStockOption });
    }

    return {
      parts,
      total: parts.reduce((sum, item) => sum + item.points, 0),
    };
  };

  const getRiskStyle = (level) => {
    const normalized = String(level || '').toLowerCase();
    if (normalized.includes('high') || normalized.includes('élev') || normalized.includes('elev')) {
      return { backgroundColor: '#FEF2F2', borderColor: '#FECACA' };
    }
    if (normalized.includes('medium') || normalized.includes('moyen')) {
      return { backgroundColor: '#FEF3C7', borderColor: '#FCD34D' };
    }
    if (normalized.includes('low') || normalized.includes('faible')) {
      return { backgroundColor: '#ECFDF5', borderColor: '#A7F3D0' };
    }
    return { backgroundColor: '#F8FAFC', borderColor: '#E2E8F0' };
  };

  const getBadgeStyle = (level) => {
    const normalized = String(level || '').toLowerCase();
    if (normalized.includes('high') || normalized.includes('élev') || normalized.includes('elev')) {
      return { backgroundColor: '#FEE2E2', color: '#991B1B', borderColor: '#FCA5A5' };
    }
    if (normalized.includes('medium') || normalized.includes('moyen')) {
      return { backgroundColor: '#FEF3C7', color: '#92400E', borderColor: '#FBBF24' };
    }
    if (normalized.includes('low') || normalized.includes('faible')) {
      return { backgroundColor: '#D1FAE5', color: '#065F46', borderColor: '#6EE7B7' };
    }
    return { backgroundColor: '#E2E8F0', color: '#334155', borderColor: '#CBD5E1' };
  };

  const getRiskIcon = (level) => {
    const normalized = String(level || '').toLowerCase();
    if (normalized.includes('high') || normalized.includes('élev') || normalized.includes('elev')) return '🔴';
    if (normalized.includes('medium') || normalized.includes('moyen')) return '🟡';
    if (normalized.includes('low') || normalized.includes('faible')) return '🟢';
    return '⚪';
  };

  const styleSection = {
    background: '#FFFFFF',
    border: '1px solid var(--border-color)',
    borderRadius: 'var(--radius-md)',
    padding: '1rem',
    boxShadow: 'var(--shadow-sm)',
  };

  const computed = computeBreakdown(employee);
  const parts = computed.parts;
  const total = computed.total;

  return (
    <div className="modal-overlay">
      <div className="modal-content" style={{ maxWidth: '940px', width: '100%', padding: '1.5rem' }}>
        <div className="modal-header">
          <div>
            <div className="modal-title">Scoring Dynamique</div>
            <div className="section-subtitle">{employeeName}</div>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        {loading ? (
          <div className="loading-container">
            <div className="spinner"></div>
            <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Chargement...</p>
          </div>
        ) : (
          <>
            {error && (
              <div className="section-card modal-error">
                Erreur : {error}
              </div>
            )}

            <div className="modal-scoring-body">
              {score && (
                <div className="section-card section-card--highlight" style={{ borderWidth: '2px', borderColor: getRiskStyle(score.riskLevel).borderColor, background: getRiskStyle(score.riskLevel).backgroundColor }}>
                  <div className="score-summary">
                    <div>
                      <div className="score-summary-value">{score.score}</div>
                      <div className="score-summary-meta">Score de Risque</div>
                    </div>
                    <div>
                      <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>{getRiskIcon(score.riskLevel)}</div>
                      <div className="badge-pill" style={{ border: `1px solid ${getBadgeStyle(score.riskLevel).borderColor}`, background: getBadgeStyle(score.riskLevel).backgroundColor, color: getBadgeStyle(score.riskLevel).color }}>
                        {score.riskLabel}
                      </div>
                      <div className="score-summary-meta" style={{ marginTop: '0.5rem', fontSize: '0.8rem' }}>{score.riskLevel}</div>
                    </div>
                    <div>
                      <div className="section-title" style={{ marginBottom: '0.5rem', fontSize: '1rem' }}>Seuils</div>
                      <div className="score-legend">
                        <div>Élevé : ≥ 55</div>
                        <div>Moyen : ≥ 30</div>
                        <div>Faible : &lt; 30</div>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              <div className="modal-scoring-grid">
                <div className="section-card">
                  <div className="section-title">Détail du calcul</div>
                  <div className="section-subtitle"><strong>Total calculé :</strong> {total}</div>
                  {parts.length === 0 ? (
                    <div style={{ color: 'var(--text-muted)', marginTop: '0.75rem' }}>Aucun facteur applicable pour cet employé.</div>
                  ) : (
                    <div className="score-list" style={{ marginTop: '0.75rem' }}>
                      {parts.map((part, index) => (
                        <div key={index} className="score-item">
                          <div>{part.name}</div>
                          <div style={{ fontWeight: 700 }}>+{part.points}</div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                <aside className="section-card">
                  <div className="section-title">Critères configurables</div>
                  <ul style={{ color: 'var(--text-muted)', fontSize: '0.9rem', lineHeight: 1.7, paddingLeft: '1rem' }}>
                    <li>Ancienneté courte (&le;1 an): +20</li>
                    <li>Beaucoup d'emplois antérieurs (&ge;4): +15</li>
                    <li>Heures supplémentaires: +12</li>
                    <li>Salaire bas (&lt;3000): +14</li>
                    <li>Faible satisfaction travail (&le;2): +12</li>
                    <li>Faible satisfaction environnement: +8</li>
                    <li>Pas de promotion récente (&ge;3 ans): +10</li>
                    <li>Faible niveau d'options (&le;0): +9</li>
                  </ul>
                </aside>
              </div>

              {score && parsedReasons(score.reasons).length > 0 && (
                <div className="section-card">
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                    <AlertCircle size={18} style={{ color: '#2563EB' }} />
                    <div className="section-title" style={{ marginBottom: 0 }}>Facteurs de Risque</div>
                  </div>
                  <div className="reason-list">
                    {parsedReasons(score.reasons).map((reason, index) => (
                      <div key={index} className="reason-item">
                        <div className="reason-item-number">{index + 1}.</div>
                        <div>{reason}</div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div className="section-card">
                <div className="section-title">Historique des Scores</div>
                {history.length === 0 ? (
                  <div style={{ color: 'var(--text-muted)' }}>Aucun historique disponible.</div>
                ) : (
                  <div className="history-list">
                    {history.slice(0, 10).map((item, idx) => (
                      <div key={idx} className="history-item">
                        <div className="history-item__header">
                          <div>
                            <div style={{ fontWeight: 700, color: 'var(--text-main)' }}>
                              {item.score} pts
                              <span className="badge-pill" style={{ marginLeft: '0.65rem', border: `1px solid ${getBadgeStyle(item.riskLevel).borderColor}`, background: getBadgeStyle(item.riskLevel).backgroundColor, color: getBadgeStyle(item.riskLevel).color }}>
                                {item.riskLabel}
                              </span>
                            </div>
                            <div className="history-item__meta">
                              {item.calculatedAt ? new Date(item.calculatedAt).toLocaleString('fr-FR') : ''}
                            </div>
                          </div>
                          <button className="history-item__toggle" onClick={() => setExpandedHistoryItem(expandedHistoryItem === idx ? null : idx)}>
                            {expandedHistoryItem === idx ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                          </button>
                        </div>
                        {expandedHistoryItem === idx && parsedReasons(item.reasons).length > 0 && (
                          <div className="history-item__details">
                            {parsedReasons(item.reasons).map((reason, reasonIndex) => (
                              <div key={reasonIndex} style={{ marginBottom: '0.5rem' }}>→ {reason}</div>
                            ))}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={onClose}>
                Fermer
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default ScoringDetails;
