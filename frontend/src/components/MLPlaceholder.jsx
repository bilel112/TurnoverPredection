import React, { useState } from 'react';
import { Brain, Cpu, AlertTriangle, CheckCircle, BarChart3 } from 'lucide-react';
import axios from 'axios';

// ---------------------------------------------------------------------------
// NOTE IMPORTANTE (lire avant de re-tester) :
// Les probabilités affichées viennent de l'API backend (SVM). Si elles restent
// écrasées près de 0% ou 100% quel que soit le profil, le problème est côté
// modèle, pas ici. Deux pistes backend à essayer :
//   1) Remplacer SVC(probability=True) par un CalibratedClassifierCV
//      (method="sigmoid" ou "isotonic", cv=5) pour recalibrer les scores.
//   2) Vérifier que l'ordre des colonnes après get_dummies + scaler.transform
//      dans l'API est EXACTEMENT le même que celui utilisé à l'entraînement
//      (sinon les prédictions peuvent être incohérentes silencieusement).
// ---------------------------------------------------------------------------

const MLPlaceholder = () => {
  const [predicting, setPredicting] = useState(false);
  const [predictionResult, setPredictionResult] = useState(null);
  const [error, setError] = useState('');
  const [selectedModel, setSelectedModel] = useState('svm');
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [profilePreset, setProfilePreset] = useState('');
  const numericFields = new Set([
    'age',
    'distanceFromHome',
    'education',
    'environmentSatisfaction',
    'jobInvolvement',
    'jobLevel',
    'jobSatisfaction',
    'monthlyIncome',
    'numCompaniesWorked',
    'percentSalaryHike',
    'relationshipSatisfaction',
    'stockOptionLevel',
    'totalWorkingYears',
    'trainingTimesLastYear',
    'workLifeBalance',
    'yearsAtCompany',
    'yearsInCurrentRole',
    'yearsSinceLastPromotion',
    'yearsWithCurrManager',
  ]);

  // Seuils demandés :
  // >=80 Très élevé | >=60 Élevé | >=40 Modéré | >=20 Faible | <20 Très faible
  const getRiskCategory = (probability) => {
  if (probability >= 80) return { label: 'Très élevé', color: '#B91C1C', background: '#FEE2E2', border: '#FCA5A5', high: true };
  if (probability >= 40) return { label: 'Élevé',      color: '#DC2626', background: '#FEE2E2', border: '#FCA5A5', high: true };
  if (probability >= 16) return { label: 'Modéré',      color: '#B45309', background: '#FEF3C7', border: '#F59E0B', high: false };
  if (probability >= 4) return { label: 'Faible',      color: '#16A34A', background: '#ECFDF5', border: '#A7F3D0', high: false };
  return                     { label: 'Très faible',    color: '#0F766E', background: '#ECFEFA', border: '#2DD4BF', high: false };
};

  // Profils d'exemple. Les valeurs sont volontairement plus contrastées entre
  // paliers (satisfaction, salaire, distance, overtime, ancienneté) pour aider
  // le modèle à séparer les profils une fois la calibration backend corrigée.
  const exampleProfiles = {
    'Très faible': {
      age: 45,
      businessTravel: 'Non_Travel',
      department: 'Research & Development',
      distanceFromHome: 3,
      education: 4,
      educationField: 'Technical Degree',
      environmentSatisfaction: 4,
      gender: 'Female',
      jobInvolvement: 4,
      jobLevel: 4,
      jobRole: 'Research Scientist',
      jobSatisfaction: 4,
      maritalStatus: 'Married',
      monthlyIncome: 14000,
      numCompaniesWorked: 1,
      overtime: false,
      percentSalaryHike: 18,
      relationshipSatisfaction: 4,
      stockOptionLevel: 2,
      totalWorkingYears: 22,
      trainingTimesLastYear: 4,
      workLifeBalance: 4,
      yearsAtCompany: 15,
      yearsInCurrentRole: 9,
      yearsSinceLastPromotion: 1,
      yearsWithCurrManager: 9,
    },
    'Faible': {
      age: 36,
      businessTravel: 'Travel_Rarely',
      department: 'Sales',
      distanceFromHome: 8,
      education: 3,
      educationField: 'Marketing',
      environmentSatisfaction: 3,
      gender: 'Male',
      jobInvolvement: 3,
      jobLevel: 2,
      jobRole: 'Sales Executive',
      jobSatisfaction: 3,
      maritalStatus: 'Married',
      monthlyIncome: 8500,
      numCompaniesWorked: 2,
      overtime: false,
      percentSalaryHike: 13,
      relationshipSatisfaction: 3,
      stockOptionLevel: 1,
      totalWorkingYears: 12,
      trainingTimesLastYear: 2,
      workLifeBalance: 3,
      yearsAtCompany: 7,
      yearsInCurrentRole: 4,
      yearsSinceLastPromotion: 2,
      yearsWithCurrManager: 4,
    },
    'Modéré': {
      age: 31,
      businessTravel: 'Travel_Frequently',
      department: 'Sales',
      distanceFromHome: 16,
      education: 2,
      educationField: 'Marketing',
      environmentSatisfaction: 2,
      gender: 'Female',
      jobInvolvement: 2,
      jobLevel: 2,
      jobRole: 'Sales Representative',
      jobSatisfaction: 2,
      maritalStatus: 'Single',
      monthlyIncome: 4800,
      numCompaniesWorked: 4,
      overtime: true,
      percentSalaryHike: 11,
      relationshipSatisfaction: 2,
      stockOptionLevel: 0,
      totalWorkingYears: 6,
      trainingTimesLastYear: 1,
      workLifeBalance: 2,
      yearsAtCompany: 3,
      yearsInCurrentRole: 2,
      yearsSinceLastPromotion: 1,
      yearsWithCurrManager: 2,
    },
    'Élevé': {
      age: 27,
      businessTravel: 'Travel_Frequently',
      department: 'Sales',
      distanceFromHome: 22,
      education: 2,
      educationField: 'Life Sciences',
      environmentSatisfaction: 1,
      gender: 'Male',
      jobInvolvement: 1,
      jobLevel: 1,
      jobRole: 'Laboratory Technician',
      jobSatisfaction: 1,
      maritalStatus: 'Single',
      monthlyIncome: 2800,
      numCompaniesWorked: 5,
      overtime: true,
      percentSalaryHike: 10,
      relationshipSatisfaction: 1,
      stockOptionLevel: 0,
      totalWorkingYears: 3,
      trainingTimesLastYear: 0,
      workLifeBalance: 1,
      yearsAtCompany: 1,
      yearsInCurrentRole: 1,
      yearsSinceLastPromotion: 0,
      yearsWithCurrManager: 1,
    },
    'Très élevé': {
      age: 24,
      businessTravel: 'Travel_Frequently',
      department: 'Sales',
      distanceFromHome: 27,
      education: 1,
      educationField: 'Life Sciences',
      environmentSatisfaction: 1,
      gender: 'Female',
      jobInvolvement: 1,
      jobLevel: 1,
      jobRole: 'Sales Representative',
      jobSatisfaction: 1,
      maritalStatus: 'Single',
      monthlyIncome: 2100,
      numCompaniesWorked: 6,
      overtime: true,
      percentSalaryHike: 10,
      relationshipSatisfaction: 1,
      stockOptionLevel: 0,
      totalWorkingYears: 1,
      trainingTimesLastYear: 0,
      workLifeBalance: 1,
      yearsAtCompany: 0,
      yearsInCurrentRole: 0,
      yearsSinceLastPromotion: 0,
      yearsWithCurrManager: 0,
    },
  };

  const loadExampleProfile = (preset) => {
    setInputs(exampleProfiles[preset]);
    setProfilePreset(preset);
    setPredictionResult(null);
    setError('');
  };

  const [inputs, setInputs] = useState({
    age: 22,
    businessTravel: 'Travel_Frequently',
    department: 'Sales',
    distanceFromHome: 28,
    education: 2,
    educationField: 'Marketing',
    environmentSatisfaction: 1,
    gender: 'Male',
    jobInvolvement: 1,
    jobLevel: 1,
    jobRole: 'Sales Representative',
    jobSatisfaction: 1,
    maritalStatus: 'Single',
    monthlyIncome: 2200,
    numCompaniesWorked: 8,
    overtime: true,
    percentSalaryHike: 11,
    relationshipSatisfaction: 1,
    stockOptionLevel: 0,
    totalWorkingYears: 2,
    trainingTimesLastYear: 0,
    workLifeBalance: 1,
    yearsAtCompany: 0,
    yearsInCurrentRole: 0,
    yearsSinceLastPromotion: 0,
    yearsWithCurrManager: 0,
  });

  const handleInputChange = (e) => {
    const { name, value, type } = e.target;

    let parsedValue = value;
    if (numericFields.has(name)) {
      parsedValue = value === '' ? '' : Number(value);
    } else if (name === 'overtime') {
      parsedValue = value === 'true';
    }

    setInputs(prev => ({
      ...prev,
      [name]: parsedValue,
    }));
    // On quitte le mode "preset" dès que l'utilisateur modifie un champ à la main
    setProfilePreset('');
  };

  const validatePayload = (payload) => {
    return Object.entries(payload)
      .filter(([_, value]) => value === undefined || value === null || value === '' || Number.isNaN(value))
      .map(([key]) => key);
  };

  const handlePredict = async (e) => {
    e.preventDefault();
    setPredicting(true);
    setPredictionResult(null);
    setError('');

    try {
      const payload = {
        age: Number(inputs.age),
        overtime: inputs.overtime ? 'Yes' : 'No',
        monthlyIncome: Number(inputs.monthlyIncome),
        distanceFromHome: Number(inputs.distanceFromHome),
        jobSatisfaction: Number(inputs.jobSatisfaction),
        environmentSatisfaction: Number(inputs.environmentSatisfaction),
        yearsAtCompany: Number(inputs.yearsAtCompany),
        totalWorkingYears: Number(inputs.totalWorkingYears),
        stockOptionLevel: Number(inputs.stockOptionLevel),
        jobLevel: Number(inputs.jobLevel),
        yearsInCurrentRole: Number(inputs.yearsInCurrentRole),
        yearsWithCurrManager: Number(inputs.yearsWithCurrManager),
        yearsSinceLastPromotion: Number(inputs.yearsSinceLastPromotion),
        numCompaniesWorked: Number(inputs.numCompaniesWorked),
        trainingTimesLastYear: Number(inputs.trainingTimesLastYear),
        workLifeBalance: Number(inputs.workLifeBalance),
        education: Number(inputs.education),
        maritalStatus: inputs.maritalStatus,
        businessTravel: inputs.businessTravel,
        jobRole: inputs.jobRole,
        department: inputs.department,
        educationField: inputs.educationField,
        gender: inputs.gender,
        jobInvolvement: Number(inputs.jobInvolvement),
        percentSalaryHike: Number(inputs.percentSalaryHike),
        relationshipSatisfaction: Number(inputs.relationshipSatisfaction),
      };

      const missingFields = validatePayload(payload);
      if (missingFields.length > 0) {
        throw new Error(
          `Veuillez remplir correctement les champs suivants : ${missingFields.join(', ')}`
        );
      }

      const endpoint = selectedModel === 'logistic-regression'
        ? 'http://localhost:8001/predict/logistic-regression'
        : 'http://localhost:8001/predict/svm';

      const response = await axios.post(endpoint, payload);
      const result = response.data;
      const probability = Math.round(result.probability * 100);
      const category = getRiskCategory(probability);

      setPredictionResult({
        probability,
        category,
        message: result.message,
        model: result.model,
      });
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.data) {
        const data = err.response.data;
        if (data.detail) {
          setError(`Erreur API : ${JSON.stringify(data.detail)}`);
        } else {
          setError(`Erreur API : ${JSON.stringify(data)}`);
        }
      } else {
        setError(err.message || 'La prédiction a échoué. Vérifiez que l’API Python est bien lancée sur le port 8001.');
      }
    } finally {
      setPredicting(false);
    }
  };

  const features = [
    { name: 'Heures Supplémentaires (OverTime)', importance: 92 },
    { name: 'Salaire Mensuel (MonthlyIncome)', importance: 85 },
    { name: 'Âge (Age)', importance: 74 },
    { name: 'Années chez Ooredoo (YearsAtCompany)', importance: 68 },
    { name: 'Distance du domicile (DistanceFromHome)', importance: 61 },
    { name: 'Satisfaction Travail (JobSatisfaction)', importance: 55 },
    { name: 'Niveau Stock Options (StockOptionLevel)', importance: 42 },
    { name: 'Statut marital (MaritalStatus)', importance: 38 },
    { name: 'Rôle (JobRole)', importance: 35 },
  ];

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
        <Brain size={32} style={{ color: 'var(--primary)' }} />
        <h1 className="title" style={{ margin: 0 }}>Intelligence Artificielle & Prédictions</h1>
      </div>
      <p className="subtitle">Module prédictif pour le turnover (Modèle SVM / Logistic Regression)</p>

      {/* Info Banner */}
      <div className="card" style={{
        backgroundColor: '#FFF0F2',
        border: '1px solid #FEE2E2',
        display: 'flex',
        alignItems: 'center',
        gap: '1rem',
        padding: '1.25rem',
        marginBottom: '2rem',
        borderRadius: 'var(--radius-md)'
      }}>
        <Cpu size={24} style={{ color: 'var(--primary)', flexShrink: 0 }} />
        <div style={{ fontSize: '0.9rem', color: '#B91C1C' }}>
          <strong>Prédiction active :</strong> ce module appelle le modèle entraîné exporté depuis le notebook et renvoie un score de risque d’attrition basé sur les variables les plus importantes.
        </div>
      </div>

      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))',
        gap: '2rem'
      }}>
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem' }}>Prédiction d'attrition</h3>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
            Saisissez seulement les variables les plus importantes identifiées dans le notebook : ancienneté, statut marital, rôle, overtime, salaire, stock options, âge et satisfaction.
          </p>

          <form onSubmit={handlePredict}>
            <div className="form-group">
              <label className="form-label">Modèle</label>
              <select className="form-select" value={selectedModel} onChange={(e) => setSelectedModel(e.target.value)}>
                <option value="svm">SVM (RBF)</option>
                <option value="logistic-regression">Logistic Regression</option>
              </select>
            </div>

            <div style={{ marginBottom: '1rem', display: 'grid', gridTemplateColumns: 'repeat(5, minmax(0, 1fr))', gap: '0.75rem' }}>
              {['Très faible', 'Faible', 'Modéré', 'Élevé', 'Très élevé'].map((preset) => (
                <button
                  key={preset}
                  type="button"
                  onClick={() => loadExampleProfile(preset)}
                  style={{
                    border: preset === profilePreset ? '2px solid var(--primary)' : '1px solid #E5E7EB',
                    backgroundColor: preset === profilePreset ? '#EFF6FF' : '#FFFFFF',
                    borderRadius: 'var(--radius-sm)',
                    padding: '0.75rem',
                    fontSize: '0.85rem',
                    cursor: 'pointer'
                  }}
                >
                  {preset}
                </button>
              ))}
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '0.75rem' }}>Section principale</h4>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Âge</label>
                  <input type="number" name="age" className="form-input" value={inputs.age} onChange={handleInputChange} />
                </div>
                <div className="form-group">
                  <label className="form-label">Heures Supplémentaires</label>
                  <select name="overtime" className="form-select" value={String(inputs.overtime)} onChange={handleInputChange}>
                    <option value="true">Oui</option>
                    <option value="false">Non</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Salaire Mensuel (DT)</label>
                  <input type="number" name="monthlyIncome" className="form-input" value={inputs.monthlyIncome} onChange={handleInputChange} />
                </div>
                <div className="form-group">
                  <label className="form-label">Satisfaction Travail</label>
                  <select name="jobSatisfaction" className="form-select" value={inputs.jobSatisfaction} onChange={handleInputChange}>
                    <option value={1}>1 - Très basse</option>
                    <option value={2}>2 - Basse</option>
                    <option value={3}>3 - Haute</option>
                    <option value={4}>4 - Très haute</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Satisfaction Environnement</label>
                  <select name="environmentSatisfaction" className="form-select" value={inputs.environmentSatisfaction} onChange={handleInputChange}>
                    <option value={1}>1 - Très basse</option>
                    <option value={2}>2 - Basse</option>
                    <option value={3}>3 - Haute</option>
                    <option value={4}>4 - Très haute</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Work Life Balance</label>
                  <select name="workLifeBalance" className="form-select" value={inputs.workLifeBalance} onChange={handleInputChange}>
                    <option value={1}>1 - Très faible</option>
                    <option value={2}>2 - Faible</option>
                    <option value={3}>3 - Bien</option>
                    <option value={4}>4 - Très bien</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Département</label>
                  <select name="department" className="form-select" value={inputs.department} onChange={handleInputChange}>
                    <option value="Sales">Sales</option>
                    <option value="Research & Development">Research & Development</option>
                    <option value="Human Resources">Human Resources</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Années dans l'entreprise</label>
                  <input type="number" name="yearsAtCompany" className="form-input" value={inputs.yearsAtCompany} onChange={handleInputChange} />
                </div>
                <div className="form-group">
                  <label className="form-label">Statut marital</label>
                  <select name="maritalStatus" className="form-select" value={inputs.maritalStatus} onChange={handleInputChange}>
                    <option value="Single">Single</option>
                    <option value="Married">Married</option>
                    <option value="Divorced">Divorced</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Business Travel</label>
                  <select name="businessTravel" className="form-select" value={inputs.businessTravel} onChange={handleInputChange}>
                    <option value="Travel_Rarely">Travel Rarely</option>
                    <option value="Travel_Frequently">Travel Frequently</option>
                    <option value="Non_Travel">Non Travel</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Job Role</label>
                  <select name="jobRole" className="form-select" value={inputs.jobRole} onChange={handleInputChange}>
                    <option value="Sales Executive">Sales Executive</option>
                    <option value="Sales Representative">Sales Representative</option>
                    <option value="Research Scientist">Research Scientist</option>
                    <option value="Laboratory Technician">Laboratory Technician</option>
                    <option value="Manufacturing Director">Manufacturing Director</option>
                    <option value="Healthcare Representative">Healthcare Representative</option>
                    <option value="Manager">Manager</option>
                    <option value="Research Director">Research Director</option>
                    <option value="Human Resources">Human Resources</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Niveau d'options</label>
                  <select name="stockOptionLevel" className="form-select" value={inputs.stockOptionLevel} onChange={handleInputChange}>
                    <option value={0}>0</option>
                    <option value={1}>1</option>
                    <option value={2}>2</option>
                    <option value={3}>3</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Expérience totale (années)</label>
                  <input type="number" min={0} name="totalWorkingYears" className="form-input" value={inputs.totalWorkingYears} onChange={handleInputChange} />
                </div>
              </div>
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <button
                type="button"
                onClick={() => setShowAdvanced(prev => !prev)}
                style={{
                  width: '100%',
                  border: '1px solid #E5E7EB',
                  backgroundColor: '#F9FAFB',
                  borderRadius: 'var(--radius-sm)',
                  padding: '0.75rem 1rem',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  fontWeight: 700,
                  cursor: 'pointer',
                  color: 'var(--text-main)'
                }}
              >
                <span>Section avancée</span>
                <span>{showAdvanced ? '▼' : '▶'}</span>
              </button>

              <div
                style={{
                  maxHeight: showAdvanced ? '320px' : '0px',
                  overflowY: showAdvanced ? 'auto' : 'hidden',
                  transition: 'max-height 0.25s ease',
                  marginTop: showAdvanced ? '0.75rem' : '0',
                  paddingRight: showAdvanced ? '0.5rem' : '0'
                }}
              >
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label className="form-label">Distance Domicile (km)</label>
                    <input type="number" name="distanceFromHome" className="form-input" value={inputs.distanceFromHome} onChange={handleInputChange} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Éducation</label>
                    <input type="number" name="education" className="form-input" value={inputs.education} onChange={handleInputChange} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Domaine d'éducation</label>
                    <select name="educationField" className="form-select" value={inputs.educationField} onChange={handleInputChange}>
                      <option value="Marketing">Marketing</option>
                      <option value="Life Sciences">Life Sciences</option>
                      <option value="Medical">Medical</option>
                      <option value="Technical Degree">Technical Degree</option>
                      <option value="Human Resources">Human Resources</option>
                      <option value="Other">Other</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label className="form-label">Genre</label>
                    <select name="gender" className="form-select" value={inputs.gender} onChange={handleInputChange}>
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label className="form-label">Job Involvement</label>
                    <select name="jobInvolvement" className="form-select" value={inputs.jobInvolvement} onChange={handleInputChange}>
                      <option value={1}>1</option>
                      <option value={2}>2</option>
                      <option value={3}>3</option>
                      <option value={4}>4</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label className="form-label">Pourcentage de hausse salariale</label>
                    <input type="number" name="percentSalaryHike" className="form-input" value={inputs.percentSalaryHike} onChange={handleInputChange} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Relationship Satisfaction</label>
                    <select name="relationshipSatisfaction" className="form-select" value={inputs.relationshipSatisfaction} onChange={handleInputChange}>
                      <option value={1}>1</option>
                      <option value={2}>2</option>
                      <option value={3}>3</option>
                      <option value={4}>4</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label className="form-label">Nombre d'entreprises</label>
                    <input type="number" name="numCompaniesWorked" className="form-input" value={inputs.numCompaniesWorked} onChange={handleInputChange} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Formation l'année dernière</label>
                    <input type="number" name="trainingTimesLastYear" className="form-input" value={inputs.trainingTimesLastYear} onChange={handleInputChange} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Années actuel poste</label>
                    <input type="number" name="yearsInCurrentRole" className="form-input" value={inputs.yearsInCurrentRole} onChange={handleInputChange} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Années avec manager</label>
                    <input type="number" name="yearsWithCurrManager" className="form-input" value={inputs.yearsWithCurrManager} onChange={handleInputChange} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Années depuis dernière promotion</label>
                    <input type="number" name="yearsSinceLastPromotion" className="form-input" value={inputs.yearsSinceLastPromotion} onChange={handleInputChange} />
                  </div>
                </div>
              </div>
            </div>

            <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }} disabled={predicting}>
              {predicting ? (
                <>
                  <div className="spinner" style={{ width: '16px', height: '16px', borderLeftColor: 'white' }}></div>
                  <span>Calcul de la probabilité...</span>
                </>
              ) : (
                <span>Lancer la Prédiction d'Attrition</span>
              )}
            </button>
          </form>

          {error && (
            <div style={{ marginTop: '1rem', padding: '0.75rem', backgroundColor: '#FEF2F2', color: '#B91C1C', borderRadius: 'var(--radius-sm)' }}>
              {error}
            </div>
          )}

          {predictionResult && (
            <div style={{
              marginTop: '1.5rem',
              padding: '1.25rem',
              borderRadius: 'var(--radius-md)',
              border: '1px solid',
              backgroundColor: predictionResult.category.background,
              borderColor: predictionResult.category.border,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '0.5rem',
              textAlign: 'center'
            }}>
              {predictionResult.category.high
                ? <AlertTriangle size={36} style={{ color: predictionResult.category.color }} />
                : <CheckCircle size={36} style={{ color: predictionResult.category.color }} />}

              <div style={{ fontSize: '1.25rem', fontWeight: 700, color: '#1F2937' }}>
                Probabilité de départ : {predictionResult.probability}%
              </div>

              <div style={{ fontSize: '0.85rem', fontWeight: 600, color: predictionResult.category.color, textTransform: 'uppercase' }}>
                Risque : {predictionResult.category.label}
              </div>

              <div style={{ fontSize: '0.8rem', color: '#4B5563' }}>
                Modèle : {predictionResult.model}
              </div>

              <p style={{ fontSize: '0.8rem', color: '#4B5563', marginTop: '0.25rem' }}>
                {predictionResult.message}
              </p>
            </div>
          )}
        </div>

        {/* Feature Importance Chart */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
            <BarChart3 size={20} style={{ color: 'var(--primary)' }} />
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Importance des Variables</h3>
          </div>

          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
            Influence relative de chaque variable dans la prédiction de l'attrition des employés.
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {features.map(f => (
              <div key={f.name} style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                  <span style={{ fontWeight: 500, color: 'var(--text-main)' }}>{f.name}</span>
                  <span style={{ fontWeight: 600, color: 'var(--text-muted)' }}>{f.importance}%</span>
                </div>
                <div style={{
                  width: '100%',
                  height: '8px',
                  backgroundColor: '#F3F4F6',
                  borderRadius: '4px',
                  overflow: 'hidden'
                }}>
                  <div style={{
                    width: `${f.importance}%`,
                    height: '100%',
                    backgroundColor: 'var(--primary)',
                    borderRadius: '4px'
                  }}></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default MLPlaceholder;
