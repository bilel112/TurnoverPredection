# Contexte du Projet - Turnover Prediction

**Date** : 2026-08-02  
**État global** : ~36% du cahier de charge implémenté

---

## ✅ Ce qui a été réalisé

### 1. **Backend Java Spring Boot** (En cours)
- Structure MVC complète
- Entités : `User`, `Employee`, `Role`
- **CRUD complet implémenté** pour les 3 entités :
  - UserManager.jsx → endpoints CRUD utilisateurs
  - EmployeeManager.jsx → endpoints CRUD employés
  - RoleManager.jsx → endpoints CRUD rôles
- Repositories JPA
- Controllers REST
- Services
- DTO pour validation
- Endpoints :
  - `/api/users/*` (GET, POST, PUT, DELETE)
  - `/api/employees/*` (GET, POST, PUT, DELETE)
  - `/api/roles/*` (GET, POST, PUT, DELETE)

### 2. **Frontend React + Vite**
- Stack : React 19.2.7, Vite 8.1.1, Axios 1.18.1, Lucide React 1.24.0
- Composants :
  - **Sidebar** : navigation
  - **UserManager** : tableau + CRUD users
  - **EmployeeManager** : tableau + CRUD employés
  - **RoleManager** : tableau + CRUD rôles
  - **Dashboard** : placeholder initial
  - **MLPlaceholder** : formulaire prédiction (détaillé ci-dessous)

### 3. **Module Machine Learning** (Fonctionnel)
- **FastAPI** : 2 endpoints prédiction
  - `POST /predict/svm` → probabilité + classification
  - `POST /predict/logistic-regression` → même interface
- **Modèles ML** : SVM + Logistic Regression (joblib)
- **Preprocessing** :
  - Normalisation données
  - Encodage catégories
  - Imputation valeurs manquantes
- **Validation** : Pydantic avec 24 champs contraints
  - Age : 18-70
  - StockOptionLevel : 0-3
  - MonthlyIncome : ≥1000
  - Etc.

### 4. **Prédiction Turnover (MLPlaceholder.jsx)**
- Formulaire complet 24 champs
- 2 sections :
  - **Main** : 12 champs essentiels (collapsible)
  - **Avancée** : 12 champs additionnels
- 5 niveaux de risque :
  - Very Low (<20%)
  - Low (20-39%)
  - Moderate (40-59%)
  - High (60-79%)
  - Very High (≥80%)
- 5 profils exemple (populables en 1 clic)
- Résultats affichent :
  - Probabilité
  - Niveau risque + couleur
  - Classification (Leave/Stay)
  - Modèle utilisé
  - Messages détaillés

### 5. **Validation & Erreurs**
- Validation client-side (champs vides, NaN)
- Erreurs Pydantic affichées côté frontend
- 422 Unprocessable Entity gérés
- Messages utilisateur détaillés

### 6. **Persistance & BD**
- MySQL/MariaDB (configuré dans `application.properties`)
- 3 tables : `users`, `employees`, `roles`
- Migrations/init SQL (via Spring Data JPA)

---

## ❌ Ce qui manque (Cahier de charge)

### 1. **Module de Scoring Dynamique** (90%)
- ✓ Calcul de score par employé (faible/moyen/élevé)
- ✓ Règles configurables via propriétés Spring Boot (`application.properties`)
- ✓ Pondération de critères métier (ancienneté, heures supplémentaires, salaire, satisfaction, promotion, options)
- ✓ Historisation des scores dans la base
- ✓ Score distinct du modèle ML
- ✓ **UI détaillée** : Composant `ScoringDetails.jsx` affiche :
  - Score actuel avec codage couleur (rouge/orange/vert)
  - Facteurs de risque numérotés et explicites
  - Configuration des critères
  - Historique des 10 derniers calculs avec raisons détaillées
  - Dates et heures précises
- ✓ Bouton "Scoring" intégré dans la liste des employés pour accéder à la modal
- ⚠️ À enrichir : graphique d'évolution du score, modification dynamique des poids depuis l'UI (vs properties file)

### 2. **Module de Simulation & Scénarios** (0%)
- ✗ Pas de simulation augmentation salariale
- ✗ Pas de simulation changement poste
- ✗ Pas de simulation formation
- ✗ Pas de comparaison avant/après
- ✗ Pas de graphiques dynamiques

### 3. **Module d'Alertes Intelligentes** (72%)
- ✓ Repository d’alertes créé
- ✓ Service de génération d’alertes implémenté
- ✓ Page d’alertes dédiée existante
- ✓ Actions utilisateur : marquer lu / résoudre
- ✓ Création d’alerte manuelle ajoutée
- ✓ Seuils paramétrables (runtime) implémentés
- ✗ Notifications (mail/dashboard) non implémentées
- ✓ Rapport PDF par mail amélioré (prefixe, logo Ooredoo, en-têtes visibles)
- ✓ Tâches planifiées / batch jobs implémentées (scheduler)
- ✓ Détection de tendance simple ajoutée
- ⚠️ Authentification / 401 à stabiliser pour l’accès des alertes

### 4. **Module de Recommandations RH** (70%)
- ✓ Endpoint backend ajouté pour générer des recommandations RH
- ✓ Service IA intégré via OpenRouter
- ✓ Prompt RH structuré avec contexte employé et raisons du score
- ✓ Parsing JSON robuste pour les réponses IA
- ✓ Bouton UI ajouté dans la vue de détail de scoring
- ✓ Résumé IA + actions recommandées affichés dans l’interface
- ✓ Backend compilé avec les derniers correctifs IA
- ⚠️ À valider en runtime avec la vraie réponse de l’API et les permissions d’accès

### 5. **Module RBAC & Permissions** (En cours de stabilisation)
- ✓ Composants existent (UserManager, RoleManager, etc.)
- ✓ CRUD fonctionne (GET/POST/PUT/DELETE)
- ✓ JWT backend implémenté
- ✓ `@PreAuthorize`/RBAC backend appliqué sur les endpoints
- ⚠️ Frontend token management et envoi du header `Authorization` à vérifier
- ⚠️ Middleware frontend d’authentification manquant
- ⚠️ Différenciation RH / Manager / Admin côté UI non stabilisée
- ⚠️ Des `401` peuvent encore apparaître selon les appels

### 6. **Dashboard BI** (Très basique)
- ✓ Structure composant "Dashboard.jsx" existe
- ✗ Pas de graphiques
- ✗ Pas de KPIs
- ✗ Pas de statistiques employés
- ✗ Pas de vue alertes
- ✗ Pas de drilldown données

---

## 🔧 État technique par composant

| Composant | État | CRUD | Erreurs | API |
|-----------|------|------|---------|-----|
| UserManager | ✓ Fonctionnel | ✓✓✓ | ✓ Gérées | ✓ Spring |
| EmployeeManager | ✓ Fonctionnel | ✓✓✓ | ✓ Gérées | ✓ Spring |
| RoleManager | ✓ Fonctionnel | ✓✓✓ | ✓ Gérées | ✓ Spring |
| MLPlaceholder | ✓ Fonctionnel | - | ✓ Gérées | ✓ FastAPI |
| Dashboard | ⚠️ Basique | - | - | - |
| Sidebar | ✓ Fonctionnel | - | - | - |
| Authentification | ✗ Absente | - | - | - |
| Alertes | ⚠️ Partiel | - | - | - |
| Recommandations | ✓ Partiel | - | ✓ Gérées | ✓ Spring/IA |
| Simulation | ✗ Absente | - | - | - |

---

## 📊 Couverture Cahier de Charge

```
Module ML ........................... ✓✓✓ 100%
Module CRUD (Entités) ............... ✓✓✓ 100%
Module Prédiction Turnover .......... ✓✓ 80% (prédiction OK, profils OK)
Module Scoring Dynamique ............ ✓✓ 90% (calcul + historique + UI détaillée)
Module RBAC (Structure) ............. ✓ 10% (CRUD seul, pas sécurité)
Module Dashboard .................... ⚠️ 10% (structure + badge scoring)
Module Alertes ...................... ⚠️ 40%
Module Recommandations .............. ✓ 70%
Module Simulation ................... ✗ 0%

TOTAL : ~35% ≈ 3.2/9 modules principaux
```

---

## 🎯 Priorités implémentation restante

### **Phase 1 : Sécurité** (Critique)
1. Authentification JWT backend + frontend
2. RBAC avec @PreAuthorize
3. Middleware frontend pour routes protégées
4. Différenciation rôles (RH, Manager, Admin)

### **Phase 2 : Intelligence métier** (Valeur)
1. Scoring dynamique (pondération critères)
2. Historisation scores
3. Module recommandations simples
4. Dashboard avec KPIs

### **Phase 3 : Interactivité** (Avancé)
1. Alertes et notifications
2. Module simulation/scénarios
3. Comparaisons avant/après
4. Graphiques dynamiques

---

## Note à revenir
Si tu as terminé le module des alertes, la suite logique est :

### 1. Sécurité / Authentification
- Implémenter et stabiliser le JWT
- Assurer que le frontend envoie bien le token sur toutes les requêtes protégées
- Vérifier les `401` et corriger les cas où le token est perdu ou invalide
- Ajouter RBAC / permissions par rôle (`@PreAuthorize`, contrôles backend)

> C’est la priorité numéro 1. Sans sécurité, le reste du système reste fragile.

### 2. Notifications internes et dashboard
- Ajouter un vrai widget d’alerte sur le dashboard
- Afficher le nombre d’alertes critiques / nouvelles
- Implémenter un “push” interne dans l’application (widget/notification dans le dashboard)
- Conserver le toast interne pour confirmer les actions (comme l’envoi du PDF), pas de popup navigateur

### 3. Scoring et intelligence métier
- Enrichir le scoring dynamique avec :
  - des graphiques d’évolution
  - des poids modifiables depuis l’UI
  - des recommandations RH simples
- Lier les alertes à des actions RH (formation, mobilité, entretien)

### 4. Recommandations RH
- Construire le module de recommandations
- Proposer des actions à partir du score et des alertes
- Faire le mapping risque → actions RH

### 5. Simulation / scénarios
- Ajouter des scénarios avant/après
- Simulation d’augmentation salariale, mobilité, formation
- Comparaison impact sur le score et les alertes

---

## 🚀 Commandes utiles

```bash
# Frontend
cd turnover/frontend
npm run dev      # Développement http://localhost:5173
npm run build    # Production build
npm run lint     # Validation code

# Backend ML (FastAPI)
cd turnover/ml/api
python -m uvicorn main:app --host 127.0.0.1 --port 8001 --reload

# Backend Java (Spring Boot)
cd turnover
./mvnw.cmd spring-boot:run
```

---

## 📁 Structure clés

```
turnover/
├── frontend/              # React + Vite
│   └── src/components/
│       ├── MLPlaceholder.jsx      ✓ Prédiction
│       ├── UserManager.jsx        ✓ CRUD Users
│       ├── EmployeeManager.jsx    ✓ CRUD Employees
│       ├── RoleManager.jsx        ✓ CRUD Roles
│       ├── Dashboard.jsx          ⚠️ À enrichir
│       └── Sidebar.jsx            ✓ Navigation
├── ml/api/                # FastAPI
│   ├── main.py            ✓ Endpoints
│   ├── schemas.py         ✓ Validation
│   └── preprocessing.py   ✓ Normalisation
└── src/main/java/         # Spring Boot
    └── com/ooredoo/turnover/
        ├── entity/        ✓ User, Employee, Role
        ├── controller/    ✓ REST endpoints
        ├── service/       ✓ Logique métier
        ├── repository/    ✓ Data access
        └── security/      ✗ À implémenter
```

---

## 🔍 Prochaines étapes suggérées

1. **Tester ensemble** les endpoints (CRUD + ML)
2. **Implémenter JWT** (authentification)
3. **Ajouter RBAC** (sécurité par rôle)
4. **Scorer dynamique** (avant alertes)
5. **Dashboard enrichi** (KPIs + graphiques)

---

*Généré le 2026-07-22 - En cours de développement*
