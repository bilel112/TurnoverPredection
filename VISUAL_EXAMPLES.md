# 🎨 Exemples Visuels - Scoring UI

**Affichage visuel et exemples de code de la nouvelle fonctionnalité**

---

## 📺 Interface Utilisateur

### **Avant** : Liste d'employés simple
```
┌─────────────────────────────────────────────────┐
│ Gestion des Employés                             │
├─────────────────────────────────────────────────┤
│ N° | Âge  | Département | Poste     | Salaire   │
│    |      |             |           |           │
│ 1  │ 32   │ Sales       │ Executive │ 4500 DT   │
│    │      │             │           │           │
│ 2  │ 28   │ HR          │ Manager   │ 3200 DT   │
│    │      │             │           │           │
│ 3  │ 45   │ R&D         │ Engineer  │ 5100 DT   │
└─────────────────────────────────────────────────┘
```

### **Après** : Avec colonne Scoring
```
┌──────────────────────────────────────────────────────────┐
│ Gestion des Employés                                      │
├──────────────────────────────────────────────────────────┤
│ N° │ Âge │ Département │ Poste     │ Salaire │ Score Dyn.│
│    │     │             │           │         │           │
│ 1  │ 32  │ Sales       │ Executive │ 4500 DT │ 45 · Moyen│
│    │     │             │           │         │           │
│ 2  │ 28  │ HR          │ Manager   │ 3200 DT │ 72 · Élevé│
│    │     │             │           │         │           │
│ 3  │ 45  │ R&D         │ Engineer  │ 5100 DT │ 15 · Faible│
└──────────────────────────────────────────────────────────┘
```

### **Boutons d'action** (avant/après)

**Avant** (sans bouton Scoring) :
```
┌─────────────────────────────┐
│ Actions                      │
├─────────────────────────────┤
│ [👁️ Voir] [✏️ Edit] [🗑️ Del] │
└─────────────────────────────┘
```

**Après** (avec bouton Scoring) :
```
┌────────────────────────────────────────┐
│ Actions                                 │
├────────────────────────────────────────┤
│ [📊 Score] [👁️ Voir] [✏️ Edit] [🗑️ Del] │
└────────────────────────────────────────┘
```

---

## 🔍 Modal de Scoring - Vue Complète

```
╔════════════════════════════════════════════════════════════════╗
║ 📊 Scoring Dynamique                                  [✕ Fermer]║
║ Sales Executive - #1234                                        ║
╠════════════════════════════════════════════════════════════════╣
║                                                                 ║
║  ┌────────────┐  ┌────────────┐  ┌────────────────────┐        ║
║  │    45      │  │     🟡     │  │  Seuils :          │        ║
║  │  Score     │  │            │  │  Élevé : ≥ 55      │        ║
║  │            │  │ Risque     │  │  Moyen : ≥ 30      │        ║
║  │            │  │   Moyen    │  │  Faible : < 30     │        ║
║  └────────────┘  └────────────┘  └────────────────────┘        ║
║                                                                 ║
║  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━        ║
║                                                                 ║
║  ⚠️ Facteurs de Risque                                          ║
║  ┌───────────────────────────────────────────────────────┐     ║
║  │ ① Ancienneté courte (≤1 an)                           │     ║
║  │ ② Heures supplémentaires actives                      │     ║
║  │ ③ Salaire bas (<3000 DT)                              │     ║
║  └───────────────────────────────────────────────────────┘     ║
║                                                                 ║
║  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━        ║
║                                                                 ║
║  Critères Configurables                                        ║
║  ┌───────────────────────────────────────────────────────┐     ║
║  │ • Ancienneté courte (≤1 an) : +20 points             │     ║
║  │ • Beaucoup d'emplois antérieurs (≥4) : +15 points   │     ║
║  │ • Heures supplémentaires : +12 points                │     ║
║  │ • Salaire bas (<3000) : +14 points                   │     ║
║  │ • Faible satisfaction travail (≤2) : +12 points      │     ║
║  │ • Faible satisfaction environnement : +8 points      │     ║
║  │ • Pas de promotion récente (≥3 ans) : +10 points    │     ║
║  │ • Faible niveau d'options (≤0) : +9 points           │     ║
║  └───────────────────────────────────────────────────────┘     ║
║                                                                 ║
║  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━        ║
║                                                                 ║
║  Historique des Scores                                         ║
║  ┌───────────────────────────────────────────────────────┐     ║
║  │ 45 pts - 🟡 Risque Moyen                      [⌄]   │     ║
║  │ 26/07/2026 à 17:06                                    │     ║
║  │                                                       │     ║
║  │ 48 pts - 🟡 Risque Moyen                      [⌄]   │     ║
║  │ 25/07/2026 à 14:32                                    │     ║
║  │   → Ancienneté courte                                 │     ║
║  │   → Heures supplémentaires                            │     ║
║  │   → Salaire bas                                       │     ║
║  │                                                       │     ║
║  │ 52 pts - 🟡 Risque Moyen                      [⌄]   │     ║
║  │ 24/07/2026 à 09:15                                    │     ║
║  └───────────────────────────────────────────────────────┘     ║
║                                                                 ║
║  [Fermer]                                                       ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 🎯 Exemples de Scores Différents

### **Score FAIBLE (< 30) - Employé Stable**
```
╔════════════════════════════════════════╗
║ Score : 18                              ║
║ Risque : 🟢 FAIBLE                     ║
║ Label : "Risque Faible"                ║
║                                         ║
║ Facteurs : (Aucun - employé stable)    ║
╚════════════════════════════════════════╝
```
**Couleur de fond** : Vert clair
**Action suggérée** : Maintenir le bonheur

---

### **Score MOYEN (30-54) - À Surveiller**
```
╔════════════════════════════════════════╗
║ Score : 42                              ║
║ Risque : 🟡 MOYEN                      ║
║ Label : "Risque Moyen"                 ║
║                                         ║
║ Facteurs :                              ║
║ ① Heures supplémentaires                ║
║ ② Faible satisfaction environnement     ║
╚════════════════════════════════════════╝
```
**Couleur de fond** : Jaune clair
**Action suggérée** : Entretien RH recommandé

---

### **Score ÉLEVÉ (≥ 55) - Critique**
```
╔════════════════════════════════════════╗
║ Score : 68                              ║
║ Risque : 🔴 ÉLEVÉ                      ║
║ Label : "Risque Élevé"                 ║
║                                         ║
║ Facteurs :                              ║
║ ① Ancienneté courte                     ║
║ ② Salaire bas                           ║
║ ③ Heures supplémentaires                ║
║ ④ Basse satisfaction travail            ║
║ ⑤ Pas de promotion récente              ║
╚════════════════════════════════════════╝
```
**Couleur de fond** : Rouge clair
**Action suggérée** : Action RH urgente

---

## 💻 Code React - Exemple d'Utilisation

### **Import dans EmployeeManager.jsx**
```jsx
import ScoringDetails from './ScoringDetails';
import { BarChart3 } from 'lucide-react';

// Dans le state du composant
const [scoringModalOpen, setScoringModalOpen] = useState(false);
const [scoringEmployee, setScoringEmployee] = useState(null);

// Dans le tableau, bouton d'action
<button 
  className="btn btn-secondary btn-xs" 
  title="Scoring"
  onClick={() => {
    setScoringEmployee(emp);
    setScoringModalOpen(true);
  }}
>
  <BarChart3 size={14} />
</button>

// Render de la modal
{scoringModalOpen && scoringEmployee && (
  <ScoringDetails
    employeeId={scoringEmployee.id}
    employeeName={`${scoringEmployee.jobRole} - #${scoringEmployee.employeeNumber}`}
    onClose={() => {
      setScoringModalOpen(false);
      setScoringEmployee(null);
    }}
  />
)}
```

### **Composant ScoringDetails.jsx - Structure**
```jsx
const ScoringDetails = ({ employeeId, employeeName, onClose }) => {
  // État
  const [score, setScore] = useState(null);      // Score actuel
  const [history, setHistory] = useState([]);    // 10 derniers scores
  const [loading, setLoading] = useState(true);
  const [expandedHistoryItem, setExpandedHistoryItem] = useState(null);

  // Fetch data au mount
  useEffect(() => {
    loadScoringData();
  }, [employeeId]);

  // Charger score + historique en parallèle
  const loadScoringData = async () => {
    const [scoreData, historyData] = await Promise.all([
      DynamicTurnoverService.getScoreForEmployee(employeeId),
      DynamicTurnoverService.getHistoryForEmployee(employeeId)
    ]);
    setScore(scoreData);
    setHistory(historyData);
  };

  // Fonctions utilitaires de style
  const getRiskColor = (level) => {
    // Retourne classe Tailwind : 'bg-red-50', 'bg-yellow-50', 'bg-green-50'
  };

  const getRiskBadgeColor = (level) => {
    // Retourne classe badge avec couleur/texte adapté
  };

  // JSX Structure
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50">
      <div className="bg-white rounded-lg max-w-2xl w-full">
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-600 to-blue-700 text-white p-6">
          {/* Titre + bouton fermer */}
        </div>

        {/* Contenu */}
        <div className="p-6 space-y-6">
          {/* Score Card */}
          {/* Raisons */}
          {/* Config */}
          {/* Historique */}
          {/* Boutons */}
        </div>
      </div>
    </div>
  );
};
```

---

## 🔌 API Response - Exemple JSON

### **Request**
```http
GET /api/turnover-scoring/employees/42/score
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### **Response 200**
```json
{
  "score": 45,
  "riskLevel": "MEDIUM",
  "riskLabel": "Risque Moyen",
  "reasons": [
    "Ancienneté courte",
    "Heures supplémentaires",
    "Salaire bas"
  ],
  "calculatedAt": "2026-07-26T17:06:29"
}
```

### **History Request**
```http
GET /api/turnover-scoring/employees/42/history
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### **History Response 200**
```json
[
  {
    "score": 45,
    "riskLevel": "MEDIUM",
    "riskLabel": "Risque Moyen",
    "reasons": [
      "Ancienneté courte",
      "Heures supplémentaires"
    ],
    "calculatedAt": "2026-07-26T17:06:29"
  },
  {
    "score": 48,
    "riskLevel": "MEDIUM",
    "riskLabel": "Risque Moyen",
    "reasons": [
      "Ancienneté courte",
      "Heures supplémentaires",
      "Salaire bas"
    ],
    "calculatedAt": "2026-07-25T14:32:00"
  },
  // ... 8 autres entrées
]
```

---

## ⚙️ Configuration - application.properties

```properties
# Turnover Scoring Configuration
# Weights (poids) for 8 risk factors (total max = 100)

turnover.scoring.shortTenureWeight=20
turnover.scoring.manyCompaniesWeight=15
turnover.scoring.overtimeWeight=12
turnover.scoring.lowSalaryWeight=14
turnover.scoring.lowJobSatisfactionWeight=12
turnover.scoring.lowEnvironmentSatisfactionWeight=8
turnover.scoring.noRecentPromotionWeight=10
turnover.scoring.lowStockOptionWeight=9

# Total : 20+15+12+14+12+8+10+9 = 100 points max
```

**Modification recommandée** : Si le total ne fait pas 100, vérifier la logique métier.

---

## 🎬 Workflow Utilisateur Complet

```
1. Login
   └─> Username: rh1
   └─> Password: rh123
       └─> Role: HR (peut voir Scoring ✅)

2. Navigation
   └─> Click Sidebar "Gestion des Employés"
       └─> Page charge liste (10 employés)
           └─> Chaque ligne a bouton 📊

3. Ouvrir Scoring
   └─> Click 📊 pour employé #1234
       └─> API appelle backend
           └─> /api/turnover-scoring/employees/1234/score
           └─> /api/turnover-scoring/employees/1234/history
               └─> Données retournées
                   └─> Modal s'affiche

4. Visualiser
   └─> Score = 45
   └─> Risque = Moyen (🟡)
   └─> Facteurs listés
   └─> Historique visible

5. Fermer
   └─> Click X ou Fermer
       └─> Modal disparaît
           └─> Retour à liste employés
```

---

## 📊 Statistiques de Taille

| Élément | Taille | Notes |
|---------|--------|-------|
| **ScoringDetails.jsx** | 220 lignes | Composant React pur |
| **Modifications EmployeeManager.jsx** | +30 lignes | Import + state + JSX |
| **Frontend Build Output** | 96.24 kB | Gzippé, optimisé |
| **Backend Jar** | ~45 MB | Unchanged |
| **Database Table** | ~100 KB | Avec 1470 employés + historique |

---

## ✅ Validation Checklist Finale

- [x] Composant React créé et testé
- [x] Intégration avec EmployeeManager
- [x] Frontend build succès (0 erreurs)
- [x] Backend tests succès (3/3)
- [x] API endpoints fonctionnels
- [x] Permissions RBAC appliquées
- [x] Design responsive
- [x] Gestion erreurs
- [x] Documentation complète
- [x] Exemples fournis

**🎉 Prêt pour production !**
