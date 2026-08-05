import React, { useEffect, useState } from 'react';
import { AlertService, EmployeeService } from '../services/api';
import { normalizeAlertsResponse, normalizeAlertSummary } from '../utils/alertResponse';

const Alerts = () => {
  const [alerts, setAlerts] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState(null);
  const [newAlert, setNewAlert] = useState({ employeeId: '', title: '', message: '', severity: 'HIGH' });
  const [config, setConfig] = useState(null);
  const [configForm, setConfigForm] = useState({ highThreshold: '', mediumThreshold: '', trendDelta: '', evaluationIntervalMs: '' });
  const [filterStatus, setFilterStatus] = useState('');
  const [filterSeverity, setFilterSeverity] = useState('');
  const [summary, setSummary] = useState({ total: 0, newCount: 0, readCount: 0, resolvedCount: 0 });
  const [sendingReport, setSendingReport] = useState(false);
  const [feedback, setFeedback] = useState(null);

  // Fermeture automatique de la toast après 4.5 secondes
  useEffect(() => {
    if (!feedback) return;
    const timer = setTimeout(() => setFeedback(null), 4500);
    return () => clearTimeout(timer);
  }, [feedback]);

  const loadEmployees = async () => {
    try {
      const data = await EmployeeService.getAll();
      setEmployees(data || []);
    } catch (e) {
      console.error('Impossible de charger la liste des employés', e);
    }
  };

  const loadConfig = async () => {
    try {
      const data = await AlertService.getConfig();
      setConfig(data);
      setConfigForm({
        highThreshold: data.highThreshold,
        mediumThreshold: data.mediumThreshold,
        trendDelta: data.trendDelta,
        evaluationIntervalMs: data.evaluationIntervalMs,
      });
    } catch (e) {
      console.error('Impossible de charger la configuration d\'alerte', e);
    }
  };

  const loadAlertSummary = async () => {
    try {
      const data = await AlertService.summary();
      setSummary(normalizeAlertSummary(data));
    } catch (e) {
      console.error('Impossible de charger le résumé des alertes', e);
    }
  };

  const loadAlerts = async (status = filterStatus, severity = filterSeverity, showSpinner = true) => {
    try {
      if (showSpinner) setLoading(true);
      const data = await AlertService.list(status, severity);
      setAlerts(normalizeAlertsResponse(data));
    } catch (e) {
      console.error(e);
      if (e.response?.status === 401) {
        setError('Accès non autorisé. Veuillez vous reconnecter.');
      } else if (e.message === 'Network Error') {
        setError('Backend indisponible. Vérifiez que le serveur Spring Boot est lancé.');
      } else {
        setError('Impossible de charger les alertes.');
      }
    } finally {
      if (showSpinner) setLoading(false);
    }
  };

  useEffect(() => {
    loadAlerts();
    loadEmployees();
    loadAlertSummary();
  }, []);

  useEffect(() => {
    loadConfig();
  }, []);

  useEffect(() => {
    loadAlerts();
  }, [filterStatus, filterSeverity]);

  // Polling automatique : recharge les alertes et le résumé à l'intervalle
  // configuré (evaluationIntervalMs) pour que les alertes de risque générées
  // par le scheduler backend apparaissent sans rechargement manuel.
  // Le spinner est désactivé pendant le polling pour ne pas recharger l'écran.
  useEffect(() => {
    if (!config || !config.evaluationIntervalMs || config.evaluationIntervalMs <= 0) {
      return;
    }
    const intervalId = setInterval(() => {
      loadAlerts(filterStatus, filterSeverity, false);
      loadAlertSummary();
    }, config.evaluationIntervalMs);
    return () => clearInterval(intervalId);
  }, [config?.evaluationIntervalMs, filterStatus, filterSeverity]);

  const handleMarkRead = async (id) => {
    try {
      await AlertService.markRead(id);
      loadAlerts();
      loadAlertSummary();
    } catch (e) {
      console.error(e);
    }
  };

  const handleResolve = async (id) => {
    try {
      await AlertService.resolve(id);
      loadAlerts();
      loadAlertSummary();
    } catch (e) {
      console.error(e);
    }
  };

  const handleNewAlertChange = (e) => {
    const { name, value } = e.target;
    setNewAlert((prev) => ({ ...prev, [name]: value }));
  };

  const handleCreateAlert = async (e) => {
    e.preventDefault();
    setCreateError(null);
    if (!newAlert.employeeId || !newAlert.title || !newAlert.message) {
      setCreateError('Veuillez renseigner tous les champs.');
      return;
    }

    try {
      setCreating(true);
      await AlertService.createForEmployee(newAlert.employeeId, {
        title: newAlert.title,
        message: newAlert.message,
        severity: newAlert.severity,
      });
      setNewAlert({ employeeId: '', title: '', message: '', severity: 'HIGH' });
      loadAlerts();
      loadAlertSummary();
      setFeedback({ type: 'success', message: 'Alerte créée avec succès.' });
    } catch (e) {
      console.error('Erreur création alerte', e);
      setCreateError('Impossible de créer l’alerte. Vérifiez le backend et la connexion.');
      setFeedback({ type: 'error', message: 'Impossible de créer l’alerte.' });
    } finally {
      setCreating(false);
    }
  };

  const handleConfigChange = (e) => {
    const { name, value } = e.target;
    setConfigForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSaveConfig = async (e) => {
    e.preventDefault();
    try {
      await AlertService.updateConfig({
        highThreshold: Number(configForm.highThreshold),
        mediumThreshold: Number(configForm.mediumThreshold),
        trendDelta: Number(configForm.trendDelta),
        evaluationIntervalMs: Number(configForm.evaluationIntervalMs),
      });
      loadConfig();
      setFeedback({ type: 'success', message: 'Configuration sauvegardée avec succès.' });
    } catch (err) {
      console.error('Erreur mise à jour config', err);
      setFeedback({ type: 'error', message: 'Impossible de sauvegarder la configuration.' });
    }
  };

  const handleFilterChange = (setter) => (e) => {
    setter(e.target.value);
  };

  const handleReloadAllAlerts = async () => {
    try {
      setFilterStatus('');
      setFilterSeverity('');
      setError(null);
      await loadAlerts('', '');
      await loadAlertSummary();
    } catch (e) {
      console.error('Impossible de recharger toutes les alertes', e);
    }
  };

  const handleSendReport = async () => {
    try {
      setSendingReport(true);
      setFeedback(null);
      await AlertService.sendHighRiskReport();
      setFeedback({
        type: 'success',
        message: 'Rapport PDF envoyé avec succès à bilel.brini@esprit.tn.',
      });
    } catch (err) {
      console.error('Erreur envoi rapport', err);
      setFeedback({
        type: 'error',
        message: 'Impossible d’envoyer le rapport par mail.',
      });
    } finally {
      setSendingReport(false);
    }
  };

  // Composant Toast (la belle notification)
  const Toast = ({ feedback, onClose }) => {
    if (!feedback) return null;

    const isSuccess = feedback.type === 'success';

    return (
      <div
        style={{
          position: 'fixed',
          top: '1.5rem',
          right: '1.5rem',
          zIndex: 9999,
          minWidth: '320px',
          maxWidth: '420px',
          padding: '1rem 1.25rem',
          borderRadius: '12px',
          background: isSuccess ? '#1b5e20' : '#b71c1c',
          color: '#fff',
          boxShadow: '0 12px 40px rgba(0,0,0,0.25)',
          display: 'flex',
          alignItems: 'flex-start',
          gap: '0.85rem',
          animation: 'slideIn 0.35s cubic-bezier(0.16, 1, 0.3, 1)',
          fontFamily: 'inherit',
        }}
      >
        <div style={{ fontSize: '1.4rem', lineHeight: 1, marginTop: 2 }}>
          {isSuccess ? '✅' : '⚠️'}
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontWeight: 600, fontSize: '0.95rem', marginBottom: 2 }}>
            {isSuccess ? 'Succès' : 'Erreur'}
          </div>
          <div style={{ fontSize: '0.9rem', opacity: 0.95, lineHeight: 1.4 }}>
            {feedback.message}
          </div>
        </div>
        <button
          onClick={onClose}
          style={{
            background: 'rgba(255,255,255,0.2)',
            border: 'none',
            color: '#fff',
            width: 28,
            height: 28,
            borderRadius: 8,
            cursor: 'pointer',
            fontSize: '1.1rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
          }}
        >
          ×
        </button>

        <style>{`
          @keyframes slideIn {
            from { opacity: 0; transform: translateX(40px) scale(0.95); }
            to   { opacity: 1; transform: translateX(0) scale(1); }
          }
        `}</style>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Chargement...</p>
      </div>
    );
  }

  if (error) {
    return <div className="section-card modal-error">{error}</div>;
  }

  return (
    <div>
      {/* La belle notification */}
      <Toast feedback={feedback} onClose={() => setFeedback(null)} />

      <h1 className="title">Alertes</h1>
      <p className="subtitle">Liste des alertes générées par le scoring dynamique</p>

      {/* Créer une alerte manuelle */}
      <div className="section-card" style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ marginBottom: '1rem' }}>Créer une alerte manuelle</h2>
        <form onSubmit={handleCreateAlert} style={{ display: 'grid', gap: '1rem' }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <label className="form-label">
              Employé
              <select
                name="employeeId"
                value={newAlert.employeeId}
                onChange={handleNewAlertChange}
                className="form-select"
              >
                <option value="">Sélectionner un employé</option>
                {employees.map((emp) => (
                  <option key={emp.id} value={emp.id}>
                    {emp.employeeNumber || emp.id} — {emp.jobRole}
                  </option>
                ))}
              </select>
            </label>
            <label className="form-label">
              Gravité
              <select
                name="severity"
                value={newAlert.severity}
                onChange={handleNewAlertChange}
                className="form-select"
              >
                <option value="HIGH">HIGH</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="LOW">LOW</option>
              </select>
            </label>
          </div>

          <label className="form-label">
            Titre
            <input
              name="title"
              type="text"
              className="form-input"
              value={newAlert.title}
              onChange={handleNewAlertChange}
              placeholder="Ex: Risque élevé détecté"
            />
          </label>

          <label className="form-label">
            Message
            <textarea
              name="message"
              className="form-input"
              rows={3}
              value={newAlert.message}
              onChange={handleNewAlertChange}
              placeholder="Ex: L'employé présente plusieurs facteurs de risque..."
            />
          </label>

          {createError && <div className="modal-error">{createError}</div>}

          <button type="submit" className="btn btn-primary" disabled={creating}>
            {creating ? 'Création…' : 'Créer une alerte'}
          </button>
        </form>
      </div>

      {/* Configuration des seuils */}
      <div className="section-card" style={{ marginBottom: '1.5rem' }}>
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: '1rem',
          }}
        >
          <h2 style={{ marginBottom: '1rem' }}>Configuration des seuils</h2>
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleSendReport}
            disabled={sendingReport}
          >
            {sendingReport ? 'Envoi…' : 'Envoyer le rapport PDF par mail'}
          </button>
        </div>

        {config ? (
          <form onSubmit={handleSaveConfig} style={{ display: 'grid', gap: '0.5rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <label className="form-label">
                Seuil HIGH
                <input
                  name="highThreshold"
                  type="number"
                  className="form-input"
                  value={configForm.highThreshold}
                  onChange={handleConfigChange}
                />
              </label>
              <label className="form-label">
                Seuil MEDIUM
                <input
                  name="mediumThreshold"
                  type="number"
                  className="form-input"
                  value={configForm.mediumThreshold}
                  onChange={handleConfigChange}
                />
              </label>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <label className="form-label">
                Delta tendance
                <input
                  name="trendDelta"
                  type="number"
                  className="form-input"
                  value={configForm.trendDelta}
                  onChange={handleConfigChange}
                />
              </label>
              <label className="form-label">
                Intervalle évaluation (ms, 0 = désactivé)
                <input
                  name="evaluationIntervalMs"
                  type="number"
                  className="form-input"
                  value={configForm.evaluationIntervalMs}
                  onChange={handleConfigChange}
                />
              </label>
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
              <button className="btn btn-primary" type="submit">
                Sauvegarder
              </button>
              <button
                type="button"
                className="btn"
                onClick={() => {
                  setConfigForm({
                    highThreshold: config.highThreshold,
                    mediumThreshold: config.mediumThreshold,
                    trendDelta: config.trendDelta,
                    evaluationIntervalMs: config.evaluationIntervalMs,
                  });
                }}
              >
                Annuler
              </button>
            </div>
          </form>
        ) : (
          <div>Chargement configuration...</div>
        )}
      </div>

      {/* Notifications & filtres */}
      <div className="section-card" style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ marginBottom: '1rem' }}>Notifications & filtres</h2>
        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            gap: '1rem',
            marginBottom: '1rem',
          }}
        >
          <div className="notification-card">
            <strong>Nouvelle</strong>
            <p>{summary.newCount}</p>
          </div>
          <div className="notification-card">
            <strong>Lu</strong>
            <p>{summary.readCount}</p>
          </div>
          <div className="notification-card">
            <strong>Résolu</strong>
            <p>{summary.resolvedCount}</p>
          </div>
          <div className="notification-card">
            <strong>Total</strong>
            <p>{summary.total}</p>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: '1rem', alignItems: 'end' }}>
          <label className="form-label">
            Filtrer par statut
            <select
              className="form-select"
              value={filterStatus}
              onChange={handleFilterChange(setFilterStatus)}
            >
              <option value="">Tous</option>
              <option value="NEW">NEW</option>
              <option value="READ">READ</option>
              <option value="RESOLVED">RESOLVED</option>
            </select>
          </label>
          <label className="form-label">
            Filtrer par gravité
            <select
              className="form-select"
              value={filterSeverity}
              onChange={handleFilterChange(setFilterSeverity)}
            >
              <option value="">Toutes</option>
              <option value="HIGH">HIGH</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="LOW">LOW</option>
            </select>
          </label>
          <button type="button" className="btn btn-secondary" onClick={handleReloadAllAlerts}>
            Charger toutes les alertes
          </button>
        </div>
      </div>

      {/* Liste des alertes */}
      {(!Array.isArray(alerts) || alerts.length === 0) ? (
        <div className="section-card">Aucune alerte trouvée.</div>
      ) : (
        <div style={{ display: 'grid', gap: '0.75rem' }}>
          {alerts.map((a) => {
            const alertId = a?.id ?? a?.alertId;
            const title = a?.title || 'Alerte sans titre';
            const message = a?.message || 'Aucune description disponible';
            const severity = a?.severity || 'LOW';
            const status = a?.status || 'NEW';
            const createdAt = a?.createdAt || a?.created_at;
            const score = a?.score ?? null;
            const reasons = Array.isArray(a?.reasons)
              ? a.reasons.filter(Boolean).map((reason) => String(reason).trim())
              : [];
            const employee = a?.employee || a?.employeeEntity || null;
            const employeeLabel = employee?.id
              ? `${employee.id}${employee.jobRole ? ` - ${employee.jobRole}` : ''}`
              : 'N/A';

            return (
              <div
                key={alertId ?? `${title}-${createdAt}`}
                className="card"
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                  <div style={{ fontSize: '1.25rem' }}>
                    {severity === 'HIGH' ? '🔴' : severity === 'MEDIUM' ? '🟡' : '🟢'}
                  </div>
                  <div>
                    <div style={{ fontWeight: 700 }}>{title}</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                      {message}
                    </div>
                    {score != null && (
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginTop: '0.2rem' }}>
                        Score: <strong>{score}</strong>
                      </div>
                    )}
                    {reasons.length > 0 && (
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.2rem' }}>
                        Reasons: {reasons.join(' • ')}
                      </div>
                    )}
                    <div
                      style={{
                        color: 'var(--text-muted)',
                        fontSize: '0.8rem',
                        marginTop: '0.25rem',
                      }}
                    >
                      Employé: {employeeLabel}{' '}
                      · {createdAt ? new Date(createdAt).toLocaleString() : 'Date inconnue'}
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  {status !== 'READ' && (
                    <button
                      className="btn btn-secondary"
                      onClick={() => handleMarkRead(alertId)}
                    >
                      Marquer lu
                    </button>
                  )}
                  {status !== 'RESOLVED' && (
                    <button
                      className="btn btn-primary"
                      onClick={() => handleResolve(alertId)}
                    >
                      Résoudre
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Alerts;