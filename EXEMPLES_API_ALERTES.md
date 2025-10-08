# Exemples d'appels API pour le système d'alertes

## 🧪 Endpoints de test

### 1. Calculer le niveau d'alerte pour des valeurs données

**GET** `/api/alerts/calculate?niveauEau=75&seuilEau=100`

```bash
curl -X GET "http://localhost:8086/api/alerts/calculate?niveauEau=75&seuilEau=100"
```

**Réponse** :
```json
{
  "niveauEau": 75.0,
  "seuilEau": 100.0,
  "alertLevel": "ATTENTION",
  "alertLabel": "Attention",
  "alertDescription": "Le niveau d'eau nécessite une surveillance",
  "necessiteNotification": true,
  "pourcentageSeuil": 75.0
}
```

### 2. Obtenir le statut d'alerte actuel d'un capteur

**GET** `/api/alerts/status/{sensorId}`

```bash
curl -X GET "http://localhost:8086/api/alerts/status/sensor_001"
```

**Réponse** :
```json
{
  "sensorId": "sensor_001",
  "localite": "Dakar",
  "niveauEau": 85.0,
  "seuilEau": 100.0,
  "currentAlertLevel": "DANGER",
  "currentAlertDescription": "Le niveau d'eau est critique, risque d'inondation",
  "cachedAlertLevel": "DANGER",
  "timestamp": "2025-10-01T10:30:00.000Z",
  "pourcentageSeuil": 85.0
}
```

### 3. Tester manuellement l'alerte pour un capteur

**POST** `/api/alerts/test/{sensorId}`

```bash
curl -X POST "http://localhost:8086/api/alerts/test/sensor_001"
```

**Réponse** :
```json
{
  "success": true,
  "message": "Vérification d'alerte effectuée pour le capteur sensor_001",
  "niveauEau": 85.0,
  "seuilEau": 100.0,
  "alertLevel": "DANGER",
  "alertDescription": "Le niveau d'eau est critique, risque d'inondation"
}
```

### 4. Réinitialiser le cache d'alerte pour un capteur

**POST** `/api/alerts/reset/{sensorId}`

```bash
curl -X POST "http://localhost:8086/api/alerts/reset/sensor_001"
```

**Réponse** :
```json
{
  "success": "true",
  "message": "Cache d'alerte réinitialisé pour le capteur sensor_001"
}
```

## 📊 Scénarios de test complets

### Scénario 1 : Tester les différents niveaux d'alerte

```bash
# Niveau NORMAL (40% du seuil)
curl -X GET "http://localhost:8086/api/alerts/calculate?niveauEau=40&seuilEau=100"

# Niveau ATTENTION (70% du seuil)
curl -X GET "http://localhost:8086/api/alerts/calculate?niveauEau=70&seuilEau=100"

# Niveau DANGER (90% du seuil)
curl -X GET "http://localhost:8086/api/alerts/calculate?niveauEau=90&seuilEau=100"
```

### Scénario 2 : Simuler une montée progressive des eaux

1. **Ajouter un capteur avec un seuil**
```bash
curl -X POST "http://localhost:8086/add-dispositive" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "sensor_test_001",
    "localite": "Test City",
    "seuilniveauEau": 100.0,
    "latitude": 14.6928,
    "longitude": -17.4467,
    "statut": "active"
  }'
```

2. **Simuler des données MQTT avec niveaux croissants**

Vous pouvez publier des messages MQTT pour simuler :

```json
// Niveau NORMAL
{
  "device_id": "sensor_test_001",
  "timestamp": 1696157400,
  "niveau_eau": 50.0,
  "Temperature": 25.0,
  "vitesse_du_vent": 10.0,
  "latitude": 14.6928,
  "longitude": -17.4467
}

// Niveau ATTENTION (notification déclenchée)
{
  "device_id": "sensor_test_001",
  "timestamp": 1696157700,
  "niveau_eau": 70.0,
  "Temperature": 25.0,
  "vitesse_du_vent": 15.0,
  "latitude": 14.6928,
  "longitude": -17.4467
}

// Niveau DANGER (notification déclenchée)
{
  "device_id": "sensor_test_001",
  "timestamp": 1696158000,
  "niveau_eau": 90.0,
  "Temperature": 26.0,
  "vitesse_du_vent": 20.0,
  "latitude": 14.6928,
  "longitude": -17.4467
}
```

3. **Vérifier le statut après chaque envoi**
```bash
curl -X GET "http://localhost:8086/api/alerts/status/sensor_test_001"
```

### Scénario 3 : Test de l'anti-spam des notifications

```bash
# 1. Réinitialiser le cache
curl -X POST "http://localhost:8086/api/alerts/reset/sensor_001"

# 2. Déclencher une première alerte (notification envoyée)
curl -X POST "http://localhost:8086/api/alerts/test/sensor_001"

# 3. Déclencher une seconde alerte immédiatement (pas de notification si même niveau)
curl -X POST "http://localhost:8086/api/alerts/test/sensor_001"

# 4. Vérifier les logs du serveur pour confirmer l'anti-spam
```

## 🔌 Tests avec Postman

### Collection Postman

Créez une collection Postman avec les requêtes suivantes :

#### 1. Get Alert Status
- **Method**: GET
- **URL**: `http://localhost:8086/api/alerts/status/{{sensorId}}`
- **Variables**: 
  - `sensorId`: sensor_001

#### 2. Calculate Alert Level
- **Method**: GET
- **URL**: `http://localhost:8086/api/alerts/calculate`
- **Params**:
  - `niveauEau`: 75
  - `seuilEau`: 100

#### 3. Test Alert
- **Method**: POST
- **URL**: `http://localhost:8086/api/alerts/test/{{sensorId}}`

#### 4. Reset Alert Cache
- **Method**: POST
- **URL**: `http://localhost:8086/api/alerts/reset/{{sensorId}}`

## 🐛 Debug et logs

Pour voir les logs du système d'alertes :

```bash
# Démarrer l'application en mode debug
mvn spring-boot:run -Dspring-boot.run.arguments=--logging.level.com.example.alerti_back=DEBUG
```

### Logs attendus :

```
✅ Connecté et abonné à Mosquitto (sans sécurité)
📩 Message MQTT reçu : {"device_id":"sensor_001",...}
🚨 Alerte déclenchée pour le capteur sensor_001 - Niveau: Danger
✅ Notification push envoyée avec succès pour sensor_001
✅ SMS envoyé avec succès pour sensor_001
```

## 🧾 Checklist de validation

Avant de déployer en production, vérifiez :

- [ ] L'URL de l'API Gateway est configurée dans `application.properties`
- [ ] Les seuils sont définis pour tous les capteurs actifs
- [ ] Les fonctions Lambda sont déployées et fonctionnelles
- [ ] Les endpoints de l'API Gateway répondent correctement
- [ ] Les notifications push arrivent sur les appareils mobiles
- [ ] Les SMS sont reçus sur les numéros configurés
- [ ] Le cache d'alertes évite les notifications répétées
- [ ] Les trois niveaux d'alerte sont correctement déclenchés
- [ ] Les logs sont clairs et permettent le debug

## 📱 Intégration Mobile

### Réception des notifications push

L'application mobile devrait écouter les notifications push et afficher :
- Le niveau d'alerte (couleur : vert/jaune/rouge)
- La localité concernée
- Le niveau d'eau actuel et le pourcentage du seuil
- La position GPS du capteur (carte)
- Un bouton pour voir l'historique

### Exemple de payload FCM (Firebase Cloud Messaging)

```json
{
  "notification": {
    "title": "Alerte Danger - Dakar",
    "body": "Niveau d'eau: 90 cm (90% du seuil)"
  },
  "data": {
    "sensorId": "sensor_001",
    "alertLevel": "DANGER",
    "niveauEau": "90.0",
    "seuilEau": "100.0",
    "latitude": "14.6928",
    "longitude": "-17.4467"
  }
}
```

---

**Pour plus d'informations**, consultez le fichier `SYSTEME_ALERTES.md`


