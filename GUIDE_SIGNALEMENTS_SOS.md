# 🚨 Guide des Signalements SOS - AlertI

## 📋 Vue d'ensemble

Le système de signalements SOS permet aux citoyens de signaler des urgences directement via l'application AlertI. Cette fonctionnalité complète le système d'alertes automatiques des capteurs IoT.

## 🔧 Fonctionnalités

### ✅ Signalements citoyens
- **Signalements authentifiés** : Utilisateurs connectés
- **Signalements anonymes** : Sans authentification requise
- **Géolocalisation** : Coordonnées GPS obligatoires
- **Types d'urgence** : 8 catégories prédéfinies
- **Priorités** : 4 niveaux (faible, moyenne, haute, critique)

### 📱 Notifications intelligentes
- **Confirmation** : À l'utilisateur qui signale
- **Alerte générale** : À tous les utilisateurs
- **Alerte critique** : Aux autorités pour urgences critiques
- **Mise à jour** : Suivi du statut du signalement

### 📊 Gestion administrative
- **Suivi des statuts** : en_cours, traité, résolu
- **Statistiques** : Tableaux de bord complets
- **Recherche géographique** : Signalements par proximité

## 🗂️ Structure des données

### Modèle SosSignal
```java
- id: Identifiant unique
- userId: ID utilisateur (0 pour anonyme)
- typeUrgence: Type d'urgence
- description: Description détaillée
- localite: Localité
- latitude/longitude: Coordonnées GPS
- statut: en_cours, traite, resolu
- priorite: faible, moyenne, haute, critique
- timestamp: Date de création
- updatedAt: Dernière mise à jour
- photoUrl: URL photo (optionnel)
- numeroUrgence: Téléphone d'urgence (optionnel)
- anonyme: Signalement anonyme (boolean)
```

## 🔌 API Endpoints

### 📤 Création de signalements

#### Signalement authentifié
```bash
POST /api/sos/signal
Content-Type: application/json

{
  "userId": 123,
  "typeUrgence": "inondation",
  "description": "Rue inondée après la pluie, circulation impossible",
  "localite": "Dakar",
  "latitude": 14.6928,
  "longitude": -17.4467,
  "priorite": "haute",
  "photoUrl": "https://example.com/photo.jpg",
  "numeroUrgence": "+221771234567",
  "anonyme": false
}
```

#### Signalement anonyme
```bash
POST /api/sos/signal-anonyme
Content-Type: application/json

{
  "typeUrgence": "accident",
  "description": "Accident de voiture au carrefour",
  "localite": "Thiès",
  "latitude": 14.7886,
  "longitude": -16.9260,
  "priorite": "critique"
}
```

### 📥 Consultation des signalements

#### Tous les signalements
```bash
GET /api/sos/signaux
```

#### Signalements d'un utilisateur
```bash
GET /api/sos/signaux/user/{userId}
```

#### Signalement par ID
```bash
GET /api/sos/signal/{id}
```

#### Signalements urgents
```bash
GET /api/sos/urgents
```

### 🔧 Gestion administrative

#### Mettre à jour le statut
```bash
PATCH /api/sos/signal/{id}/statut
Content-Type: application/json

{
  "statut": "traite"
}
```

#### Obtenir les statistiques
```bash
GET /api/sos/statistiques
```

### 📋 Métadonnées

#### Types d'urgence disponibles
```bash
GET /api/sos/types-urgence
```

#### Priorités disponibles
```bash
GET /api/sos/priorites
```

#### Statuts disponibles
```bash
GET /api/sos/statuts
```

## 📊 Types d'urgence supportés

| Code | Label | Description |
|------|-------|-------------|
| `inondation` | Inondation | Risque ou présence d'inondation |
| `accident` | Accident | Accident de la route ou autre |
| `incendie` | Incendie | Début ou présence d'incendie |
| `agression` | Agression | Violence ou agression |
| `blessure` | Blessure | Personne blessée nécessitant assistance |
| `infrastructure` | Infrastructure | Problème d'infrastructure publique |
| `pollution` | Pollution | Pollution ou déversement dangereux |
| `autre` | Autre | Autre type d'urgence |

## 🎯 Niveaux de priorité

| Priorité | Description | Couleur | Action |
|----------|-------------|---------|--------|
| `faible` | Urgence mineure | 🟢 Vert | Notification standard |
| `moyenne` | Urgence modérée | 🟠 Orange | Notification + suivi |
| `haute` | Urgence importante | 🔴 Rouge | Notification prioritaire |
| `critique` | Urgence critique | ⚫ Rouge foncé | Alerte aux autorités |

## 🔄 Statuts de traitement

| Statut | Description |
|--------|-------------|
| `en_cours` | Signalement en cours de traitement |
| `traite` | Signalement pris en charge |
| `resolu` | Signalement résolu |

## 📱 Notifications

### Types de notifications

#### 1. Confirmation utilisateur
```json
{
  "title": "✅ Signalement SOS reçu",
  "body": "Votre signalement d'urgence a été reçu et est en cours de traitement.",
  "data": {
    "type": "sos_signal",
    "signalId": 123,
    "typeUrgence": "inondation",
    "statut": "en_cours"
  }
}
```

#### 2. Alerte générale
```json
{
  "title": "🚨 Nouveau signalement SOS",
  "body": "Signalement: inondation à Dakar",
  "data": {
    "type": "sos_signal",
    "signalId": 123,
    "localite": "Dakar",
    "priorite": "haute"
  }
}
```

#### 3. Alerte critique
```json
{
  "title": "🚨 ALERTE URGENCE CRITIQUE",
  "body": "Signalement critique: accident à Thiès",
  "data": {
    "type": "sos_signal",
    "signalId": 124,
    "priorite": "critique"
  }
}
```

## 🗄️ Base de données

### Installation
```sql
-- Exécuter le script CREATE_SOS_TABLE.sql dans Supabase
-- Crée automatiquement :
-- - Table sos_signals
-- - Index pour optimiser les requêtes
-- - Triggers pour updated_at
-- - Vue sos_signals_with_user
-- - Fonction get_sos_signals_in_radius
-- - Politiques RLS
```

### Requêtes utiles

#### Signalements dans un rayon
```sql
SELECT * FROM get_sos_signals_in_radius(
    14.6928,  -- latitude centre
    -17.4467, -- longitude centre
    5         -- rayon en km
);
```

#### Statistiques par localité
```sql
SELECT 
    localite,
    COUNT(*) as total,
    COUNT(*) FILTER (WHERE priorite = 'critique') as critiques
FROM sos_signals 
WHERE timestamp > NOW() - INTERVAL '24 hours'
GROUP BY localite
ORDER BY total DESC;
```

## 🧪 Tests

### Scénarios de test

#### 1. Signalement anonyme d'inondation
```bash
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{
    "typeUrgence": "inondation",
    "description": "Rue principale inondée",
    "localite": "Dakar",
    "latitude": 14.6928,
    "longitude": -17.4467,
    "priorite": "haute"
  }'
```

#### 2. Signalement critique d'accident
```bash
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{
    "typeUrgence": "accident",
    "description": "Accident grave avec blessés",
    "localite": "Thiès",
    "latitude": 14.7886,
    "longitude": -16.9260,
    "priorite": "critique",
    "numeroUrgence": "+221771234567"
  }'
```

#### 3. Mise à jour du statut
```bash
curl -X PATCH "http://localhost:8086/api/sos/signal/1/statut" \
  -H "Content-Type: application/json" \
  -d '{"statut": "traite"}'
```

## 🔒 Sécurité

### Politiques RLS (Row Level Security)
- **Lecture** : Tous les signalements sont publics
- **Insertion** : Autorisée pour tous (signalements anonymes)
- **Mise à jour** : Seulement par l'auteur ou les administrateurs

### Validation des données
- Coordonnées GPS obligatoires
- Description minimum 10 caractères
- Types d'urgence dans la liste autorisée
- Priorités dans la liste autorisée

## 📈 Intégration avec l'écosystème AlertI

### Complémentarité avec les capteurs IoT
- **Capteurs automatiques** : Détection préventive
- **Signalements citoyens** : Détection réactive
- **Notifications unifiées** : Même système de notification

### Workflow complet
1. **Détection automatique** par capteurs IoT
2. **Signalement citoyen** pour confirmation/complément
3. **Traitement coordonné** par les autorités
4. **Suivi et résolution** avec notifications

## 🚀 Déploiement

### Prérequis
1. Exécuter `CREATE_SOS_TABLE.sql` dans Supabase
2. Configurer les notifications AWS Lambda
3. Tester les endpoints avec Postman/curl

### Checklist de validation
- [ ] Table `sos_signals` créée
- [ ] Index et triggers fonctionnels
- [ ] Politiques RLS actives
- [ ] Endpoints API répondent
- [ ] Notifications push fonctionnelles
- [ ] Signalements anonymes autorisés
- [ ] Géolocalisation précise
- [ ] Statistiques calculées

## 📞 Support

Pour toute question ou problème :
1. Vérifier les logs de l'application
2. Consulter les statistiques via `/api/sos/statistiques`
3. Tester les endpoints avec les exemples fournis

---

**Le système de signalements SOS est maintenant opérationnel et intégré à l'écosystème AlertI !** 🎯
