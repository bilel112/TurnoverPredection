# Module de Scoring

## Objectif

Le module de scoring permet d’évaluer le niveau de risque de départ d’un employé à partir d’un ensemble de critères métier. Il a pour but d’aider les RH et les managers à identifier rapidement les employés potentiellement à risque de départ.

Ce module est indépendant du modèle de machine learning de prédiction. Il sert à fournir une logique métier simple, visible et configurable.

---

## Pourquoi ce module existe

Le projet comporte deux approches de risque :

- Le module ML : basé sur des données et un modèle prédictif.
- Le module scoring : basé sur des règles métiers explicites et interprétables.

Le scoring est utile parce qu’il est :
- simple à comprendre,
- facile à configurer,
- visible dans l’interface,
- exploitable par des non-techniques.

---

## Fonctionnalités actuelles

### 1. Calcul du score par employé
Le système calcule un score global entre 0 et 100, basé sur plusieurs facteurs métier.

### 2. Classification du risque
Le score est classé en trois niveaux :
- Faible : moins de 30
- Moyen : entre 30 et 54
- Élevé : 55 et plus

### 3. Facteurs de risque
Les facteurs actuellement pris en compte sont :
- ancienneté courte
- beaucoup d’emplois antérieurs
- heures supplémentaires
- salaire bas
- faible satisfaction au travail
- faible satisfaction environnementale
- pas de promotion récente
- faible niveau d’options

### 4. Historique des scores
Chaque calcul est enregistré en base pour conserver un historique des évaluations.

### 5. Interface utilisateur
Un composant de détail affiche :
- le score actuel,
- le niveau de risque,
- les facteurs déclenchés,
- les critères configurables,
- l’historique des derniers calculs.

---

## Architecture technique

### Backend
Le backend contient :
- un service de calcul de scoring,
- un contrôleur REST,
- une entité d’historique,
- un repository pour la persistance,
- une configuration Spring Boot pour les poids.

### Frontend
Le frontend contient :
- une vue dans la liste des employés,
- un bouton d’accès au scoring,
- une modal détaillée avec le score et l’historique.

---

## Flux de fonctionnement

1. L’utilisateur ouvre la gestion des employés.
2. Il clique sur le bouton de scoring d’un employé.
3. Le frontend appelle l’API backend.
4. Le backend calcule le score à partir des données de l’employé.
5. Le résultat est affiché dans la modal.
6. Le score est enregistré dans l’historique si nécessaire.

---

## Règles métier

Les règles ne sont pas codées en dur de façon absolue. Elles sont pilotées par des poids configurables dans la configuration Spring Boot.

Exemple de logique :
- si l’employé a moins d’un an d’ancienneté, un poids est ajouté,
- si le salaire est inférieur à 3000, un autre poids est ajouté,
- si la satisfaction est faible, un nouveau poids est ajouté.

Le total de ces points détermine le niveau de risque.

---

## Données utilisées

Les données utilisées proviennent de l’entité employé, par exemple :
- ancienneté,
- nombre d’employeurs précédents,
- présence d’heures supplémentaires,
- salaire,
- satisfaction travail,
- satisfaction environnement,
- ancienneté depuis dernière promotion,
- niveau d’options.

---

## API associée

Le module expose au moins ces endpoints :
- GET /api/turnover-scoring/employees/{employeeId}/score
- GET /api/turnover-scoring/employees/{employeeId}/history

Ces endpoints sont protégés et accessibles aux rôles RH, Manager et Admin.

---

## Points de configuration

Les poids de chaque facteur sont définis dans la configuration du projet. Cela permet de modifier facilement la logique sans changer le code principal.

Objectif : rendre le module modifiable par un administrateur technique ou un product owner sans réécrire le code.

---

## État actuel du module

Le module est fonctionnel sur les aspects suivants :
- calcul du score,
- classification du niveau de risque,
- facteurs de risque,
- historique des scores,
- interface de consultation.

Les évolutions prévues sont :
- graphique d’évolution du score,
- alertes automatiques,
- seuils configurables dans l’interface,
- recommandations RH.

---

## Prochaines améliorations prévues

1. Ajouter un graphique de tendance sur le score.
2. Ajouter des niveaux d’alerte plus riches.
3. Connecter le scoring aux alertes intelligentes.
4. Permettre la modification dynamique des poids depuis l’interface.
5. Ajouter des recommandations RH basées sur les facteurs déclenchés.

---

## Résumé simple

Le module de scoring sert à répondre à une question simple :

“Quel est le risque de départ d’un employé, et pourquoi ?”

Il doit être :
- compréhensible,
- transparent,
- configurable,
- utile pour l’action RH.
