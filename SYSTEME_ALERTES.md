# Système d'Alertes par Niveau d'Eau

## Vue d'ensemble

Ce système surveille en temps réel le niveau d'eau des capteurs et déclenche automatiquement des notifications push et SMS via AWS Lambda lorsque des seuils critiques sont atteints.

## 🎯 Niveaux d'alerte

Le système définit **3 niveaux d'alerte** basés sur le ratio entre le niveau d'eau actuel et le seuil configuré :

| Niveau | Ratio | Description | Notification |
|--------|-------|-------------|--------------|
| 🟢 **NORMAL** | < 60% | Niveau d'eau normal | Non |
| 🟡 **ATTENTION** | 60% - 85% | Surveillance nécessaire | Oui |
| 🔴 **DANGER** | ≥ 85% | Risque critique d'inondation | Oui |

### Exemples de calcul

Si le **seuil d'eau** est défini à `100 cm` :
- Niveau d'eau = `50 cm` → **NORMAL** (50%)
- Niveau d'eau = `70 cm` → **ATTENTION** (70%)
- Niveau d'eau = `90 cm` → **DANGER** (90%)

## 📁 Architecture du système

### Fichiers créés

1. **`Model/AlertLevel.java`** - Énumération des niveaux d'alerte
2. **`Service/AlertNotificationService.java`** - Service de gestion des alertes et notifications
3. **`Service/MqttListenerService.java`** - Modifié pour intégrer la vérification des alertes

### Flux de données

```
Capteur IoT (ESP32)
    ↓ MQTT
MqttListenerService
    ↓ Analyse données
AlertNotificationService
    ↓ Vérifie niveau d'eau
Déclenchement alerte (si nécessaire)
    ↓
API Gateway Lambda
    ↓
[Notification Push] + [SMS]
```

## ⚙️ Configuration

### 1. Configuration de l'API Gateway dans `application.properties`

```properties
# AWS API Gateway Lambda pour les notifications
aws.api-gateway.url=https://votre-api-id.execute-api.region.amazonaws.com/prod
aws.api-gateway.push-endpoint=/notifications/push
aws.api-gateway.sms-endpoint=/notifications/sms
```

**Remplacez** :
- `votre-api-id` : L'ID de votre API Gateway
- `region` : Votre région AWS (ex: `eu-west-1`, `us-east-1`)
- `/notifications/push` et `/notifications/sms` : Les endpoints de vos fonctions Lambda

### 2. Structure des données envoyées à l'API Gateway

#### Notification Push (POST vers `/notifications/push`)

```json
{
  "sensorId": "sensor_001",
  "localite": "Dakar",
  "alertLevel": "DANGER",
  "title": "Alerte Danger",
  "message": "🌊 ALERTE DANGER - sensor_001\n📍 Localité: Dakar\n💧 Niveau d'eau: 90.00 cm (90% du seuil)...",
  "niveauEau": 90.0,
  "seuilEau": 100.0,
  "latitude": 14.6928,
  "longitude": -17.4467,
  "timestamp": "2025-10-01T10:30:00.000Z"
}
```

#### SMS (POST vers `/notifications/sms`)

```json
{
  "sensorId": "sensor_001",
  "localite": "Dakar",
  "alertLevel": "DANGER",
  "message": "🌊 ALERTE DANGER - sensor_001\n📍 Localité: Dakar\n💧 Niveau d'eau: 90.00 cm (90% du seuil)...",
  "niveauEau": 90.0,
  "seuilEau": 100.0,
  "timestamp": "2025-10-01T10:30:00.000Z"
}
```

### 3. Configuration du seuil d'eau par capteur

Le seuil d'eau (`seuilniveauEau`) doit être configuré pour chaque capteur dans Supabase. 

Vous pouvez le faire via :
- L'interface Supabase directement
- L'endpoint POST `/add-dispositive` de votre API

Exemple de requête :
```json
{
  "id": "sensor_001",
  "localite": "Dakar",
  "seuilniveauEau": 100.0,
  "latitude": 14.6928,
  "longitude": -17.4467
}
```

## 🔔 Logique des notifications

### Anti-spam
Le système évite les notifications répétées grâce à un **cache mémoire** :
- Une notification est envoyée **uniquement** si le niveau d'alerte **change**
- Exemple : Si le capteur reste en niveau DANGER, il ne renvoie pas de notification tant que le niveau ne change pas

### Exemple de scénario

1. **T0** : Niveau d'eau = 50 cm → NORMAL (pas de notification)
2. **T1** : Niveau d'eau = 70 cm → ATTENTION ✅ (notification envoyée)
3. **T2** : Niveau d'eau = 75 cm → ATTENTION (pas de notification, toujours en ATTENTION)
4. **T3** : Niveau d'eau = 90 cm → DANGER ✅ (notification envoyée)
5. **T4** : Niveau d'eau = 95 cm → DANGER (pas de notification, toujours en DANGER)
6. **T5** : Niveau d'eau = 50 cm → NORMAL (retour à la normale, log uniquement)

## 🧪 Fonctions Lambda AWS

### Exemple de fonction Lambda pour Push Notifications

```python
import json
import boto3

# Exemple avec AWS SNS pour les notifications push
sns_client = boto3.client('sns')

def lambda_handler(event, context):
    body = json.loads(event['body'])
    
    sensor_id = body['sensorId']
    alert_level = body['alertLevel']
    message = body['message']
    title = body['title']
    
    # Publier vers un topic SNS ou directement vers des endpoints
    response = sns_client.publish(
        TopicArn='arn:aws:sns:region:account-id:AlertTopic',
        Subject=title,
        Message=message
    )
    
    return {
        'statusCode': 200,
        'body': json.dumps({
            'success': True,
            'messageId': response['MessageId']
        })
    }
```

### Exemple de fonction Lambda pour SMS

```python
import json
import boto3

sns_client = boto3.client('sns')

def lambda_handler(event, context):
    body = json.loads(event['body'])
    
    message = body['message']
    alert_level = body['alertLevel']
    
    # Liste des numéros à notifier (à stocker dans DynamoDB ou paramètre)
    phone_numbers = ['+221771234567', '+221781234567']
    
    for phone in phone_numbers:
        sns_client.publish(
            PhoneNumber=phone,
            Message=message
        )
    
    return {
        'statusCode': 200,
        'body': json.dumps({'success': True})
    }
```

## 🛠️ Méthodes utiles

### Dans `AlertNotificationService`

```java
// Réinitialiser le cache d'alerte pour un capteur
alertNotificationService.resetAlertCache("sensor_001");

// Obtenir le dernier niveau d'alerte connu
AlertLevel lastLevel = alertNotificationService.getLastAlertLevel("sensor_001");
```

### Dans `AlertLevel`

```java
// Déterminer le niveau d'alerte
AlertLevel level = AlertLevel.determinerNiveau(niveauEau, seuilEau);

// Vérifier si une notification est nécessaire
boolean needsNotification = level.necessiteNotification();
```

## 📊 Logs et monitoring

Le système affiche des logs détaillés :

```
✅ Connecté et abonné à Mosquitto
📩 Message MQTT reçu : {...}
🚨 Alerte déclenchée pour le capteur sensor_001 - Niveau: Danger
✅ Notification push envoyée avec succès pour sensor_001
✅ SMS envoyé avec succès pour sensor_001
✅ Retour à la normale pour le capteur sensor_001
```

## 🚀 Déploiement

### Étapes de mise en production

1. **Configurer l'API Gateway AWS**
   - Créer les fonctions Lambda pour push et SMS
   - Créer l'API Gateway et le lier aux fonctions
   - Noter l'URL de l'API Gateway

2. **Mettre à jour `application.properties`**
   - Remplacer `aws.api-gateway.url` par votre URL réelle

3. **Configurer les seuils des capteurs**
   - Définir `seuilniveauEau` pour chaque capteur dans Supabase

4. **Redémarrer l'application Spring Boot**
   ```bash
   mvn clean package
   java -jar target/Alerti_back-*.jar
   ```

5. **Tester le système**
   - Envoyer des données de test via MQTT
   - Vérifier les logs
   - Confirmer la réception des notifications

## 🔐 Sécurité

- **Ne jamais commiter** l'URL et les clés de l'API Gateway dans Git
- Utiliser des **variables d'environnement** en production
- Activer l'**authentification** sur l'API Gateway (API Key, IAM, etc.)
- Mettre en place des **limites de débit** (rate limiting) sur l'API Gateway

## 📞 Support

Pour toute question ou amélioration, consultez la documentation ou contactez l'équipe de développement.

---

**Version** : 1.0  
**Date** : Octobre 2025


