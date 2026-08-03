# Module d’Alerte

## Objectif

Le module d’alertes a pour objectif d’informer les RH, managers et administrateurs lorsqu’un employé présente un risque important de départ ou une évolution préoccupante.

L’idée est simple : après le calcul du scoring, le système ne se contente plus de dire “ce salarié a un score élevé”, il peut aussi déclencher une alerte utile et actionnable.

---

## Pourquoi les alertes sont importantes

Le scoring seul donne une information statique. Les alertes ajoutent une dimension proactive.

Elles permettent de :
- détecter rapidement les employés à risque,
- prévenir les managers plus tôt,
- réduire le temps de réaction RH,
- créer un suivi structuré des situations sensibles.

---

## Ce que doit faire le module d’alertes

### 1. Déclencher une alerte automatique
Une alerte doit être créée lorsqu’un employé dépasse un seuil défini.

Exemples :
- score élevé : 55 et plus,
- évolution négative du score,
- plusieurs facteurs critiques en même temps,
- maintien d’un score élevé sur plusieurs évaluations.

### 2. Classer l’alerte
Chaque alerte doit avoir un niveau de gravité :
- faible,
- moyen,
- élevé,
- critique.

### 3. Rendre l’alerte exploitable
Une alerte doit contenir :
- le nom ou l’identifiant de l’employé,
- le message clair,
- la raison de l’alerte,
- la date de création,
- le statut : nouvelle, lue, résolue.

---

## Fonctionnalités prévues

### A. Alerte basée sur le score
Le scénario principal sera :
- si le score dépasse un seuil critique, une alerte est créée.

### B. Alerte sur tendance
Si le score augmente fortement sur plusieurs calculs, l’application peut créer une alerte de tendance négative.

### C. Alerte sur facteurs critiques
Si plusieurs facteurs sont présents simultanément, par exemple :
- faible salaire,
- faible satisfaction,
- pas de promotion récente,
- ancienneté courte,

alors une alerte plus forte peut être déclenchée.

### D. Historique des alertes
Chaque alerte doit être gardée dans l’historique pour suivi et audit.

### E. Notification dans l’interface
L’alerte doit être visible dans une page dédiée et éventuellement dans un widget du dashboard.

---

## Pages et écrans à prévoir

### 1. Page des alertes
Une page avec :
- la liste des alertes actives,
- le niveau de gravité,
- le statut,
- la date,
- le nom de l’employé,
- un bouton pour marquer l’alerte comme lue ou résolue.

### 2. Widget sur le dashboard
Le tableau de bord devra afficher :
- le nombre d’alertes actives,
- les alertes critiques,
- un aperçu rapide des dernières alertes.

### 3. Intégration dans la fiche employé
Quand on consulte un employé, on doit pouvoir voir s’il a une alerte active et pourquoi.

### 4. Paramétrage des seuils
Une section de configuration devra permettre de définir :
- le seuil critique,
- le seuil d’alerte,
- la fréquence de vérification,
- l’activation ou la désactivation de certaines règles.

---

## Logique métier attendue

Le module d’alertes ne doit pas être juste une simple notification rouge. Il doit aider à agir.

Exemples de messages :
- “Cet employé a dépassé le seuil de risque critique.”
- “Le score a augmenté de manière significative sur les derniers calculs.”
- “Plusieurs facteurs de rétention ont été identifiés : salaire bas, faible satisfaction, absence de promotion récente.”

L’objectif est de donner une information claire, compréhensible et utile à l’utilisateur RH.

---

## Structure technique proposée

### Backend
Le backend devra contenir :
- une entité d’alerte,
- un repository d’alertes,
- un service de génération d’alertes,
- un service d’évaluation des seuils,
- éventuellement un service de planification.

### Frontend
Le frontend devra contenir :
- une page d’alertes,
- un composant de liste,
- un widget de résumé sur le dashboard,
- des filtres par statut et niveau.

---

## Données à stocker

Chaque alerte devrait contenir au minimum :
- id,
- employeeId,
- title,
- message,
- severity,
- status,
- createdAt,
- resolvedAt.

---

## Règles d’implémentation MVP

Pour démarrer proprement, l’implémentation MVP peut se limiter à :

1. créer une alerte quand le score est élevé,
2. afficher les alertes dans une page dédiée,
3. afficher un compteur sur le dashboard,
4. permettre de marquer une alerte comme lue ou résolue,
5. utiliser des seuils simples et configurables.

### État d'implémentation (MVP) — 2026-07-30

- Module Alertes : **70%**

- Tâches MVP
	- [x] Créer une alerte quand le score est élevé (seuil par défaut ≥55)
	- [x] Afficher les alertes dans une page dédiée (`frontend/src/components/Alerts.jsx`)
	- [x] Afficher un compteur d'alertes sur le dashboard (dashboard badge)
	- [x] Permettre de marquer une alerte comme **lue** ou **résolue**
	- [x] Ajout d'un formulaire pour créer une alerte manuelle depuis l'UI

- Fonctionnalités restantes (non implémentées)
	- [x] Seuils paramétrables via UI ou configuration (runtime)
	- [ ] Notifications (email / dashboard push)
	- [x] Rapport PDF par mail amélioré (logo Ooredoo, style plus pro, en-têtes de table visibles)
	- [x] Tâches planifiées / batch jobs pour évaluation périodique
	- [x] Détection de tendance (alertes sur évolution significative)
	- [ ] Historique avancé avec filtrage/tri des alertes

% Détail d'avancement par item (approx.)
- Backend entity/repository/service: 100% ✓
- API endpoints (list/create/patch): 100% ✓
- Frontend Alerts page: 100% ✓
- Dashboard badge: 100% ✓
- Manual alert creation UI: 100% ✓
- Auth integration & stable access (JWT + front): 20% ⚠️ (401 à stabiliser)
- Thresholds/config UI: 0% ✗
- Notifications & scheduling: 0% ✗
- Trend detection & advanced history: 0% ✗

> Remarque : l'alerte automatique est déclenchée actuellement par le `DynamicTurnoverScoringServiceImpl` lorsque le `riskLevel` calculé vaut `HIGH` (score ≥55). La logique est dans `calculateAndPersistForEmployee(...)` et appelle `AlertService.createAlertForEmployee(...)`.

---

## Évolutions futures

Après le MVP, on peut ajouter :
- notifications par email,
- tâches planifiées,
- alertes par tendance ou par fréquence,
- recommandations RH liées à chaque alerte,
- historique complet des décisions prises.

---

## Résumé simple

Le module d’alertes transforme le scoring en système d’action.

Au lieu de seulement dire qu’un employé est à risque, il permet de :
- prévenir,
- suivre,
- prioriser,
- et agir plus rapidement.

C’est la prochaine étape logique après le module de scoring.
