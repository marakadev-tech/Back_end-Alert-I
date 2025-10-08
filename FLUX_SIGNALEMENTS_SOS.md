# 🔄 Flux des Signalements SOS - AlertI

## 📊 Diagramme de flux

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   CITOYEN       │    │   APPLICATION    │    │   BACKEND       │
│   (Mobile/Web)  │    │   AlertI         │    │   Spring Boot   │
└─────────┬───────┘    └────────┬─────────┘    └────────┬────────┘
          │                     │                       │
          │ 1. Signalement      │                       │
          │─────────────────────►│                       │
          │                     │                       │
          │                     │ 2. POST /api/sos/     │
          │                     │    signal-anonyme     │
          │                     │──────────────────────►│
          │                     │                       │
          │                     │                       │ 3. Validation
          │                     │                       │    données
          │                     │                       │
          │                     │                       │ 4. Sauvegarde
          │                     │                       │    Supabase
          │                     │                       │
          │                     │                       │ 5. Notifications
          │                     │                       │    - Confirmation
          │                     │                       │    - Alerte générale
          │                     │                       │    - Alerte critique
          │                     │                       │
          │                     │ 6. Réponse JSON       │
          │                     │◄──────────────────────│
          │ 7. Confirmation     │                       │
          │◄────────────────────│                       │
          │                     │                       │
          │                     │                       │ 8. Notifications
          │                     │                       │    Push/SMS
          │                     │                       │
          │ 9. Notification     │                       │
          │    Push reçue       │                       │
          │◄────────────────────┼───────────────────────│
```

## 🔄 Flux détaillé

### 1. **Signalement initial**
- Le citoyen détecte une urgence
- Ouverture de l'application AlertI
- Sélection du type d'urgence
- Saisie de la description
- Géolocalisation automatique ou manuelle
- Choix de la priorité

### 2. **Envoi au backend**
```http
POST /api/sos/signal-anonyme
Content-Type: application/json

{
  "typeUrgence": "inondation",
  "description": "Rue inondée...",
  "localite": "Dakar",
  "latitude": 14.6928,
  "longitude": -17.4467,
  "priorite": "haute"
}
```

### 3. **Traitement backend**
- **Validation** : Vérification des données obligatoires
- **Sauvegarde** : Insertion dans Supabase
- **Notifications** : Envoi selon la priorité

### 4. **Notifications automatiques**

#### A. Confirmation utilisateur
```
📱 Notification Push
Title: "✅ Signalement SOS reçu"
Body: "Votre signalement d'urgence a été reçu et est en cours de traitement."
```

#### B. Alerte générale
```
📢 Notification à tous les utilisateurs
Title: "🚨 Nouveau signalement SOS"
Body: "Signalement: inondation à Dakar"
```

#### C. Alerte critique (si priorité critique)
```
🚨 Alerte aux autorités
Title: "🚨 ALERTE URGENCE CRITIQUE"
Body: "Signalement critique: accident à Thiès"
```

## 📱 Types de notifications

### Notifications Push (FCM)
- **Destinataires** : Tous les utilisateurs avec tokens FCM actifs
- **Contenu** : Titre, corps, données personnalisées
- **Délai** : Immédiat (< 5 secondes)

### Notifications SMS (Twilio)
- **Destinataires** : Numéros d'urgence configurés
- **Contenu** : Message court avec coordonnées GPS
- **Délai** : Immédiat (< 30 secondes)

## 🔄 Cycle de vie d'un signalement

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  EN_COURS   │───►│   TRAITE    │───►│   RESOLU    │
│             │    │             │    │             │
│ Signalement │    │ Prise en    │    │ Problème    │
│ reçu        │    │ charge      │    │ résolu      │
└─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │
       ▼                   ▼                   ▼
  Notification         Notification         Notification
  de création         de mise à jour        de résolution
```

### Statuts disponibles
1. **en_cours** : Signalement reçu, en attente de traitement
2. **traite** : Signalement pris en charge par les autorités
3. **resolu** : Problème résolu, signalement clos

## 🎯 Logique de priorité

```
PRIORITÉ CRITIQUE (🚨)
├── Notifications immédiates
├── Alerte aux autorités
├── SMS d'urgence
└── Traitement prioritaire

PRIORITÉ HAUTE (🔴)
├── Notifications immédiates
├── Traitement rapide
└── Suivi renforcé

PRIORITÉ MOYENNE (🟠)
├── Notifications standard
├── Traitement normal
└── Suivi standard

PRIORITÉ FAIBLE (🟢)
├── Notifications différées
├── Traitement en arrière-plan
└── Suivi minimal
```

## 🔍 Intégration avec les capteurs IoT

### Complémentarité des systèmes
```
CAPTEURS IoT (Automatique)
├── Détection préventive
├── Surveillance continue
├── Alertes automatiques
└── Données objectives

SIGNALEMENTS CITOYENS (Réactif)
├── Détection d'événements
├── Confirmation humaine
├── Détails contextuels
└── Intervention rapide
```

### Workflow combiné
1. **Capteur IoT** détecte une anomalie
2. **Système automatique** envoie une alerte
3. **Citoyens** confirment ou signalent des détails
4. **Autorités** interviennent avec informations complètes
5. **Résolution** avec suivi des deux systèmes

## 📊 Tableau de bord

### Vue temps réel
- Signalements en cours
- Priorités critiques
- Répartition géographique
- Temps de réponse moyen

### Statistiques
- Nombre total de signalements
- Répartition par type d'urgence
- Répartition par priorité
- Taux de résolution

### Alertes actives
- Signalements critiques non traités
- Délais de traitement dépassés
- Zones à risque identifiées

## 🚀 Points d'intégration futurs

### Intégrations possibles
- **Centres d'urgence** : Connexion directe aux services 911/18
- **Médias sociaux** : Partage automatique sur Twitter/Facebook
- **Cartes interactives** : Affichage en temps réel
- **IA/ML** : Prédiction et classification automatique
- **Blockchain** : Traçabilité et transparence

### Améliorations techniques
- **Géofencing** : Alertes par zone géographique
- **Reconnaissance vocale** : Signalements vocaux
- **Reconnaissance d'image** : Classification automatique des photos
- **Chatbot** : Assistant intelligent pour les signalements

---

**Le système de signalements SOS complète parfaitement l'écosystème AlertI !** 🎯
