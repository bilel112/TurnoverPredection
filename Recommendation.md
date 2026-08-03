# Module de Recommandations RH + Assistant IA

## 1. Objectif du module

État actuel : le module est en cours d’implémentation et a déjà atteint une première version fonctionnelle côté backend et frontend.

### ✅ Réalisé
- ajout d’un endpoint backend pour générer des recommandations RH à partir d’un employé,
- intégration d’un service IA via OpenRouter,
- construction d’un prompt RH avec contexte employé et raisons du score,
- parse JSON des réponses IA,
- ajout d’un bouton dans la vue de détail de scoring,
- affichage d’un résumé IA et de recommandations actionnables dans l’UI.

### ⚠️ À valider
- la réponse IA en runtime avec les données réelles de l’application,
- le parcours complet depuis le frontend jusqu’au backend,
- la gestion des erreurs si l’API IA ne répond pas ou renvoie un JSON non standard.

Le module de recommandations RH a pour but de transformer un simple signal de risque en une action concrète pour l’entreprise.

L’objectif n’est pas seulement de dire : “cet employé est à risque”, mais de répondre à la question suivante :

> “Qu’est-ce que les RH devraient faire maintenant pour réduire ce risque ?”

Ce module doit permettre de :

- analyser un employé à risque élevé,
- lire ses données métier et ses facteurs de risque,
- générer des recommandations RH pertinentes,
- proposer un plan d’action priorisé,
- intégrer un assistant IA conversationnel pour expliquer les raisons et proposer des actions.

---

## 2. Vision globale

Le module va s’appuyer sur trois couches :

1. Données métier de l’employé
   - informations RH,
   - score dynamique,
   - facteurs de risque,
   - historique des scores,
   - alertes associées.

2. Moteur de recommandations
   - logique métier simple et explicable,
   - règle métier par défaut,
   - possibilité d’enrichissement par IA.

3. Assistant IA / chat bot RH
   - lit les colonnes de l’employé,
   - comprend les raisons du score élevé,
   - propose des recommandations en langage naturel,
   - aide les RH à prendre une décision rapide.

---

## 3. Cas d’usage principal

### Cas d’usage : employé à risque élevé

Exemple :

- employé sélectionné dans la liste,
- score dynamique = 78,
- niveau de risque = High,
- raisons détectées :
  - ancienneté courte,
  - salaire bas,
  - faible satisfaction au travail,
  - pas de promotion récente,
  - heures supplémentaires fréquentes.

Dans ce cas, le système doit pouvoir répondre automatiquement à la question :

> “Que recommander aux RH pour cet employé ?”

L’IA doit produire une réponse du style :

- entretien RH recommandé,
- revalorisation salariale possible,
- suivi managérial à organiser,
- formation / montée en compétences à proposer,
- mobilité interne à explorer.

---

## 4. Ce que le module doit faire

### 4.1 Analyser les signaux de risque

Le système doit prendre en compte :

- le score dynamique,
- le niveau de risque,
- les raisons du score,
- les données de l’employé,
- les alertes liées à l’employé,
- l’historique des scores.

### 4.2 Générer des recommandations RH

Les recommandations doivent être :

- actionnables,
- compréhensibles,
- priorisées,
- adaptées au contexte.

### 4.3 Expliquer la logique

Chaque recommandation doit pouvoir répondre à :

- pourquoi cette action est proposée,
- sur quelle donnée elle se base,
- quelle priorité elle a,
- quel impact attendu elle peut avoir.

---

## 5. Données utilisées par le module

### 5.1 Données issues de l’employé

Le module devra lire les champs déjà présents dans l’entité Employee, par exemple :

- age
- department
- jobRole
- monthlyIncome
- yearsAtCompany
- yearsSinceLastPromotion
- jobSatisfaction
- environmentSatisfaction
- overtime
- stockOptionLevel
- numCompaniesWorked
- totalWorkingYears
- yearsInCurrentRole
- yearsWithCurrManager
- businessTravel
- maritalStatus
- gender
- performanceRating
- trainingTimesLastYear

### 5.2 Données issues du scoring

Le module exploitera aussi :

- score actuel,
- niveau de risque,
- raisons du score,
- historique du score,
- alertes actives.

### 5.3 Données contextuelles

À plus long terme, le module pourra aussi utiliser :

- historique d’absentéisme,
- évaluations de performance,
- données de promotion,
- données de mobilité,
- notes manager,
- retour d’expérience RH.

---

## 6. Logique de recommandation

### 6.1 Version initiale : règles métier simples

La première version doit être transparente et robuste.

Exemples de règles :

- si score élevé et salaire bas → recommandation : révision salariale / prime de rétention.
- si score élevé et pas de promotion récente → recommandation : entretien de carrière.
- si score élevé et faible satisfaction → recommandation : accompagnement manager / entretien RH.
- si score élevé et overtime fréquent → recommandation : amélioration du charge de travail / équilibre travail-vie personnelle.
- si score élevé et ancienneté courte → recommandation : intégration renforcée / parcours d’onboarding.
- si score élevé et faible niveau d’options → recommandation : revue de la politique de rémunération / avantages.

### 6.2 Version avancée : IA + règles

Le meilleur modèle est un hybride :

- règles métier pour garantir la logique RH,
- IA pour reformuler, enrichir, personnaliser la recommandation,
- explicabilité maintenue grâce au contexte fourni à l’IA.

---

## 7. Rôle de l’IA / LLM

L’IA doit agir comme un assistant RH intelligent.

### 7.1 Ce que l’IA doit lire

Quand un employé est sélectionné, l’IA doit recevoir :

- le nom et l’identifiant de l’employé,
- ses colonnes métier principales,
- le score dynamique,
- le niveau de risque,
- les raisons du score,
- les alertes associées,
- éventuellement l’historique du score.

### 7.2 Ce que l’IA doit produire

L’IA doit générer :

- 3 à 5 recommandations RH,
- une priorité pour chaque recommandation,
- un résumé en langage naturel,
- une justification détaillée,
- un plan d’action possible.

### 7.3 Exemple de réponse attendue

Pour un employé à risque élevé, l’IA peut répondre :

- “Cet employé présente un risque élevé principalement à cause d’un faible salaire, d’une faible satisfaction au travail et d’un manque de promotion récente.”
- “Recommandation prioritaire 1 : organiser un entretien RH dans les 7 jours.”
- “Recommandation prioritaire 2 : revoir la structure salariale ou proposer une prime de rétention.”
- “Recommandation prioritaire 3 : proposer une évolution de poste ou une formation.”
- “Recommandation prioritaire 4 : suivre le manager de proximité pour améliorer l’environnement de travail.”

---

## 8. Architecture implémentée

### 8.1 Backend
Un service dédié a été ajouté :
- RecommendationService
- RecommendationServiceImpl

Il récupère :
- l’employé via EmployeeService,
- le score dynamique via DynamicTurnoverScoringService,
- les raisons de score,
- un contexte complet de l’employé.

Il envoie ensuite ce contexte à OpenRouter et transforme la réponse en objet métier.

### 8.2 API backend
Un endpoint a été ajouté :
- POST /api/recommendations/employees/{employeeId}

Il retourne un objet contenant :
- employeeId,
- score,
- riskLevel,
- riskLabel,
- summary,
- aiSummary,
- recommendations.

### 8.3 Frontend
Le composant ScoringDetails affiche maintenant :
- un bouton “Générer des recommandations”
- un résumé IA
- la liste des actions recommandées

---

## 9. Statut actuel

### Fonctionnel
- endpoint backend présent,
- intégration IA validée sur le plan réseau,
- parsing et fallback robustes mis en place,
- backend compilé avec les derniers correctifs IA,
- interface utilisateur connectée au flux.

### À finaliser
- vérifier le flux complet depuis le frontend avec un employé réel,
- confirmer que la réponse IA est bien formatée et affichée sans fallback,
- valider les droits d’accès selon les rôles RH/Manager/Admin.

---

## 10. Prochaine étape recommandée

1. lancer l’application backend,
2. ouvrir la vue de scoring d’un employé,
3. cliquer sur “Générer des recommandations”,
4. vérifier que la réponse IA remplace bien le fallback.

### 8.1 Backend

Créer un service dédié :

- RecommendationService

Ce service doit :

- récupérer l’employé depuis la base,
- récupérer le dernier score calculé,
- récupérer les raisons du score,
- récupérer les alertes liées,
- construire un payload de contexte,
- envoyer ce payload à l’IA,
- retourner une réponse structurée de recommandations.

### 8.2 API backend

Proposer un endpoint tel que :

- GET /api/recommendations/employees/{employeeId}
- POST /api/recommendations/employees/{employeeId}/generate

Réponse attendue :

```json
{
  "employeeId": 12,
  "riskLevel": "HIGH",
  "score": 78,
  "summary": "L'employé présente un risque élevé lié à un faible salaire, une faible satisfaction et un manque de promotion récente.",
  "recommendations": [
    {
      "priority": "high",
      "title": "Entretien RH à programmer",
      "reason": "Faible satisfaction et absence de progression récente",
      "action": "Organiser un entretien RH dans les 7 jours"
    },
    {
      "priority": "high",
      "title": "Révision salariale",
      "reason": "Salaire bas comparé à la structure de poste",
      "action": "Évaluer une augmentation ou une prime de rétention"
    }
  ],
  "aiSummary": "L'IA recommande un plan d'action orienté RH, manager et rémunération."
}
```

### 8.3 Frontend

Ajouter dans la vue de détail du scoring un bloc :

- Recommandations RH
- Assistant IA
- Liste des actions proposées
- Bouton “Générer recommandations”

Le composant pourra être intégré dans :

- la modal ScoringDetails,
- la fiche employé,
- la page d’alertes,
- éventuellement le dashboard.

---

## 9. Prompt IA proposé

Le prompt doit être clair, structuré et orienté métier.

### Prompt de base

```text
Tu es un assistant RH spécialisé dans la rétention des talents.
Analyse cet employé à partir de ses informations métier et de son score de risque.

Contexte :
- score de risque : {score}
- niveau de risque : {riskLevel}
- raisons du score : {reasons}
- données employé : {employeeData}
- alertes associées : {alerts}

Ta mission :
1. Résumer le risque en 2 à 3 phrases.
2. Proposer 3 à 5 recommandations RH concrètes.
3. Classer chaque recommandation par priorité : haute, moyenne ou basse.
4. Expliquer pourquoi chaque recommandation est pertinente.
5. Proposer un plan d'action simple et réaliste.

Réponds en JSON structuré.
```

### Pourquoi ce prompt est important

Il permet à l’IA de :

- rester centrée sur les RH,
- éviter les réponses vagues,
- fournir des recommandations concrètes,
- structurer la sortie pour l’interface.

---

## 10. Exemple concret de scénario

### Employé sélectionné

- Nom : Ahmed Ben Ali
- Department : Sales
- JobRole : Senior Specialist
- MonthlyIncome : 2800
- YearsAtCompany : 1.2
- JobSatisfaction : 2
- EnvironmentSatisfaction : 2
- Overtime : true
- YearsSinceLastPromotion : 4
- StockOptionLevel : 0
- NumCompaniesWorked : 4
- Score : 76
- Risque : High

### Raisons du score

- ancienneté courte,
- salaire bas,
- faible satisfaction au travail,
- pas de promotion récente,
- heures supplémentaires.

### Réponse attendue du module

```json
{
  "summary": "Cet employé présente un risque élevé lié à un faible niveau de rémunération, un environnement perçu comme peu satisfaisant et une absence de progression récente.",
  "recommendations": [
    {
      "priority": "high",
      "title": "Entretien RH et manager",
      "reason": "Faible satisfaction + besoin de clarification sur la trajectoire de carrière",
      "action": "Programmer un entretien RH dans les 7 jours"
    },
    {
      "priority": "high",
      "title": "Révision salariale",
      "reason": "Salaire bas et forte sensibilité au niveau de rémunération",
      "action": "Évaluer une augmentation ou une prime de rétention"
    },
    {
      "priority": "medium",
      "title": "Formation ou montée en compétences",
      "reason": "Absence de promotion récente et besoin de visibilité",
      "action": "Proposer une formation ou une évolution de poste"
    },
    {
      "priority": "medium",
      "title": "Suivi managérial renforcé",
      "reason": "Heures supplémentaires et faible satisfaction environnementale",
      "action": "Mettre en place un suivi mensuel avec le manager"
    }
  ]
}
```

---

## 11. Interface utilisateur attendue

### 11.1 Dans le détail du scoring

Ajouter une section :

- Recommandations RH
- Assistant IA

### 11.2 Composants UI

- bouton “Générer recommandations”
- carte de synthèse du risque
- liste de recommandations avec priorité
- bouton “Marquer comme planifiée”
- bouton “Voir détail”
- zone de conversation IA si on veut un vrai chatbot

### 11.3 UX souhaitée

L’utilisateur doit pouvoir :

1. ouvrir la fiche d’un employé,
2. voir son score et ses raisons,
3. cliquer sur “Recommandations RH”,
4. obtenir des suggestions explicites,
5. décider d’une action à suivre.

---

## 12. Version minimale vs version avancée

### Version minimale

- règles métier simples,
- recommandations statiques,
- interface dans ScoringDetails,
- pas encore de vrai chat IA.

### Version avancée

- assistant IA conversationnel,
- génération dynamique à partir des données,
- contexte complet avec score + alertes + historique,
- possibilité de demander : “Donne-moi une action prioritaire” ou “Propose un plan pour ce manager”.

---

## 13. Implémentation technique recommandée

### Étape 1 : backend

Créer :

- RecommendationService
- RecommendationController
- RecommendationRequest / RecommendationResponse DTO

### Étape 2 : logique de génération

Construire un payload composite :

- employee data,
- score result,
- reasons,
- alerts,
- maybe last trend.

### Étape 3 : appel IA

Intégrer un fournisseur LLM tel que :

- OpenAI API,
- Azure OpenAI,
- Ollama (si on veut une version locale),
- ou une solution plus simple en phase de preuve de concept.

### Étape 4 : frontend

Ajouter un nouveau bloc dans la modal de scoring et/ou dans la fiche employé.

### Étape 5 : tests

Valider sur plusieurs cas :

- score faible,
- score moyen,
- score élevé,
- employé avec salaire bas,
- employé avec satisfaction faible,
- employé avec ancienneté courte.

---

## 14. Critères de réussite

Le module sera considéré comme réussi si :

- un employé à risque élevé reçoit automatiquement des recommandations RH,
- les recommandations sont liées à ses données réelles,
- l’IA explique clairement pourquoi elle recommande telle action,
- l’interface permet à un RH de comprendre rapidement l’action à mener,
- le système reste transparent et non “boîte noire”.

---

## 15. Recommandation de priorité

### Priorité 1

Mettre en place la logique métier de base :

- rules-based recommendations,
- endpoint backend,
- interface dans le scoring.

### Priorité 2

Ajouter l’IA :

- prompt structuré,
- appel LLM,
- sortie JSON.

### Priorité 3

Ajouter un vrai chat assistant RH :

- conversation simple,
- questions fréquentes,
- action recommandée prioritaire,
- historique des conversations.

---

## 16. Proposition de version initiale

Pour démarrer proprement, on peut implémenter cette version initiale :

1. Quand un employé a un risque High, générer automatiquement 3 recommandations RH.
2. Afficher ces recommandations dans la vue de scoring.
3. Ajouter une zone “Assistant IA RH” avec un bouton “Générer”.
4. L’IA lit les données de l’employé, le score et les raisons du score.
5. L’IA retourne un résumé et 3 actions.

C’est une bonne base de départ, simple à développer, utile pour les RH, et facilement extensible.

---

## 17. Conclusion

Le module de recommandations RH doit être un pont entre :

- l’analyse de risque,
- l’intelligence métier,
- et l’action RH.

Le but est de permettre à un manager ou à un RH de passer de :

> “Cet employé est à risque”

à :

> “Voici ce qu’il faut faire maintenant pour réduire ce risque.”

L’intégration d’un assistant IA rend ce module plus naturel, plus rapide et plus utile dans un contexte réel de gestion des talents.

---

## 18. Prochaine étape recommandée

Implémenter d’abord :

- le moteur de recommandations basé sur les règles métier,
- puis l’interface dans le détail du scoring,
- puis l’intégration du LLM comme couche d’enrichissement.

C’est la voie la plus raisonnable pour livrer un module utile rapidement, tout en gardant une logique claire et exploitable par les RH.
