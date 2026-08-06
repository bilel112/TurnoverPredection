# Plan de travail Power BI - Turnover Dashboard

## Objectif
Ce fichier sert de guide de travail pour construire le dashboard Power BI de façon progressive, précise et reproductible. Il doit être suffisamment détaillé pour qu’une personne puisse suivre chaque étape dans Power BI sans ambiguïté.

## Orientation du travail
Le dashboard doit répondre à 3 questions RH simples :
- Qui est à risque ?
- Pourquoi ?
- Quelles actions faut-il prendre ?

## Version de départ
La première version doit rester simple, claire et utile :
- 1 page principale
- 4 cards KPI
- 1 graphique simple
- 1 tableau de détail

---

## Checklist Power BI

### 1. Préparer le projet Power BI
- [x] Définir le sujet du dashboard : Turnover Risk Monitoring
- [x] Choisir une structure simple et claire
- [x] Définir l’objectif principal du dashboard
- [x] Ouvrir Power BI et créer le fichier de travail

Progression : 100%

### 2. Importer les données
- [x] Charger les fichiers dans Power BI
- [x] Vérifier les colonnes disponibles dans chaque table
- [x] Renommer les tables pour plus de clarté
- [x] Vérifier les types de données de base

Progression : 100%

### 3. Construire le modèle de données
- [x] Créer les relations entre les tables
- [x] Utiliser la colonne EmployeeID comme clé de jointure
- [x] Choisir Cross filter direction = Single pour démarrer
- [x] Vérifier que les tables sont liées de façon logique

Progression : 100%

### 4. Ajouter les 4 cards KPI sur la première page
- [x] Créer la première card : Total employees
  - Source : Employees
  - Colonne utilisée : EmployeeID
  - Mesure : Count of EmployeeID
  - But : afficher le nombre total d’employés

- [x] Créer la deuxième card : Average salary
  - Source : Employees
  - Colonne utilisée : MonthlyIncome
  - Mesure : Average of MonthlyIncome
  - But : afficher le salaire moyen

- [x] Créer la troisième card : Average job satisfaction
  - Source : Employees
  - Colonne utilisée : JobSatisfaction
  - Mesure : Average of JobSatisfaction
  - But : afficher la satisfaction moyenne

- [x] Créer la quatrième card : Active alerts
  - Source : Alerts
  - Colonne utilisée : AlertID
  - Mesure : Count of AlertID
  - But : afficher le nombre d’alertes actives

Progression : 100%

### 5. Créer la première page - Vue générale
- [ ] Ajouter un titre de page : Turnover Overview
- [ ] Organiser les 4 cards en grille simple
- [ ] Ajouter un slicer par Department pour filtrer les données
- [ ] Ajouter un graphique en barres par Department
- [ ] Ajouter un tableau de détail avec les employés à risque

Progression : 0%

### 6. Créer la page 2 - Analyse des risques
- [ ] Ajouter une visualisation des employés avec le plus haut score
- [ ] Ajouter un graphique sur les facteurs de risque fréquents
- [ ] Ajouter une vue par niveau de risque
- [ ] Ajouter un filtre par RiskLevel si possible

Progression : 0%

### 7. Créer la page 3 - Actions RH
- [ ] Ajouter une liste d’actions recommandées
- [ ] Ajouter une priorité d’intervention
- [ ] Ajouter les employés à contacter en priorité
- [ ] Transformer cette page en vue décision RH

Progression : 0%

### 8. Finaliser le rendu Power BI
- [ ] Harmoniser les couleurs
- [ ] Vérifier la lisibilité du dashboard
- [ ] Simplifier si le dashboard devient trop chargé
- [ ] Préparer une version propre pour démonstration

Progression : 0%

---

## Détails techniques à respecter dans Power BI

### A. Tables à utiliser
- Employees
  - Colonnes utiles : EmployeeID, Department, MonthlyIncome, JobSatisfaction, Age, Attrition
- RiskScores
  - Colonnes utiles : EmployeeID, score, risk_level, risk_label, reasons
- Alerts
  - Colonnes utiles : EmployeeID, severity, status, title, created_at

### B. Colonnes de jointure
- Employees[EmployeeID]
- RiskScores[EmployeeID]
- Alerts[EmployeeID]

### C. Mesures à créer pour les cards KPI

1. Total employees
```DAX
Total Employees = COUNT(Employees[EmployeeID])
```

2. Average salary
```DAX
Average Salary = AVERAGE(Employees[MonthlyIncome])
```

3. Average job satisfaction
```DAX
Average Job Satisfaction = AVERAGE(Employees[JobSatisfaction])
```

4. Active alerts
```DAX
Active Alerts = COUNT(Alerts[AlertID])
```

### D. Visuals à ajouter ensuite
- Card visual
- Slicer visual
- Bar chart
- Table visual

### E. Bon ordre de travail
1. Créer les cards KPI
2. Ajouter un slicer par Department
3. Ajouter un graphique en barres par Department
4. Ajouter un tableau de détail
5. Ensuite seulement ajouter une page de risque et une page d’actions RH

---

## Ce qu’il faut éviter pour l’instant
- Trop de pages trop tôt
- Trop de graphiques trop compliqués
- Trop de couleurs ou de styles inutiles
- Des mesures trop complexes avant la base du dashboard

---

## Prochaine étape concrète
La prochaine étape consiste à :
1. Ajouter un titre à la page
2. Organiser les 4 cards KPI
3. Ajouter un slicer par Department
4. Ajouter un bar chart simple par Department
5. Ajouter un tableau de détail avec les employés et leur risque
