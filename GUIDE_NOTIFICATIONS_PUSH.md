# Guide Complet - Notifications Push Backend

## 🎯 Vue d'ensemble

Votre backend Spring Boot peut maintenant déclencher des notifications push via votre fonction Lambda Firebase. Voici comment tout fonctionne :

## 🏗️ Architecture

```
Capteur IoT → MQTT → Spring Boot → Supabase (tokens) → API Gateway Lambda → FCM → Flutter
```

## 📋 Étapes de configuration

### 1. **Créer la table dans Supabase**

Exécutez le script SQL dans `SUPABASE_FCM_TABLE.sql` dans l'éditeur SQL de Supabase :

```sql
-- Le script crée automatiquement :
-- - Table fcm_tokens avec tous les champs nécessaires
-- - Index pour les performances
-- - Triggers pour updated_at
-- - Politiques de sécurité RLS
```

### 2. **Configurer l'API Gateway**

Dans `application.properties`, remplacez l'URL par celle de votre API Gateway :

```properties
aws.api-gateway.url=https://votre-api-id.execute-api.region.amazonaws.com/prod
aws.api-gateway.push-endpoint=/notifications/push
aws.api-gateway.sms-endpoint=/notifications/sms
```

### 3. **Adapter votre fonction Lambda**

Votre fonction Lambda actuelle est déjà compatible ! Elle reçoit maintenant :

```json
{
  "token": "fcm_token_de_l_app",
  "title": "🌊 Alerte Danger - Dakar",
  "body": "Niveau: 90.0 cm (90% du seuil)",
  "data": {
    "sensorId": "sensor_001",
    "localite": "Dakar",
    "alertLevel": "DANGER",
    "niveauEau": "90.0",
    "seuilEau": "100.0",
    "latitude": "14.6928",
    "longitude": "-17.4467",
    "timestamp": "2025-10-01T10:30:00.000Z",
    "fullMessage": "Message complet de l'alerte..."
  }
}
```

## 📱 Côté Flutter

### 1. **Enregistrer le token FCM**

```dart
import 'package:firebase_messaging/firebase_messaging.dart';

class FcmService {
  static Future<void> registerFcmToken() async {
    try {
      // Obtenir le token FCM
      String? token = await FirebaseMessaging.instance.getToken();
      
      if (token != null) {
        // Envoyer au backend
        await ApiService().registerFcmToken({
          'userId': currentUser.id,
          'fcmToken': token,
          'deviceType': Platform.isAndroid ? 'android' : 'ios',
          'localite': selectedLocalite, // Localité d'intérêt
        });
        
        print('✅ Token FCM enregistré: ${token.substring(0, 20)}...');
      }
    } catch (e) {
      print('❌ Erreur enregistrement token FCM: $e');
    }
  }
}
```

### 2. **Écouter les notifications**

```dart
class NotificationHandler {
  static void initialize() {
    // Écouter les notifications en arrière-plan
    FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);
    
    // Écouter les notifications quand l'app est ouverte
    FirebaseMessaging.onMessage.listen((RemoteMessage message) {
      _handleNotification(message);
    });
    
    // Gérer les notifications tapées
    FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
      _handleNotificationTap(message);
    });
  }
  
  static void _handleNotification(RemoteMessage message) {
    // Afficher une notification locale ou dialog
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(message.notification?.title ?? 'Alerte'),
        content: Text(message.notification?.body ?? ''),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('Fermer'),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              _navigateToSensorDetails(message.data);
            },
            child: Text('Voir détails'),
          ),
        ],
      ),
    );
  }
  
  static void _navigateToSensorDetails(Map<String, dynamic> data) {
    // Naviguer vers la page de détails du capteur
    Navigator.pushNamed(
      context,
      '/sensor-details',
      arguments: {
        'sensorId': data['sensorId'],
        'alertLevel': data['alertLevel'],
        'latitude': double.parse(data['latitude']),
        'longitude': double.parse(data['longitude']),
      },
    );
  }
}

// Handler pour les notifications en arrière-plan
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  print('📱 Notification reçue en arrière-plan: ${message.messageId}');
  // Traitement en arrière-plan si nécessaire
}
```

### 3. **Service API pour Flutter**

```dart
class ApiService {
  static const String baseUrl = 'http://votre-backend:8086';
  
  Future<bool> registerFcmToken(Map<String, dynamic> tokenData) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/api/fcm/register'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(tokenData),
      );
      
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data['success'] == true;
      }
      return false;
    } catch (e) {
      print('❌ Erreur API registerFcmToken: $e');
      return false;
    }
  }
  
  Future<bool> deactivateFcmToken(String fcmToken) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/api/fcm/deactivate'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'fcmToken': fcmToken}),
      );
      
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data['success'] == true;
      }
      return false;
    } catch (e) {
      print('❌ Erreur API deactivateFcmToken: $e');
      return false;
    }
  }
}
```

## 🧪 Tests et validation

### 1. **Tester l'enregistrement de token**

```bash
# Enregistrer un token de test
curl -X POST "http://localhost:8086/api/fcm/register" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user_test_001",
    "fcmToken": "test_token_123456789",
    "deviceType": "android",
    "localite": "Dakar"
  }'
```

### 2. **Vérifier les statistiques**

```bash
# Voir les statistiques des tokens
curl -X GET "http://localhost:8086/api/fcm/stats"
```

### 3. **Tester les notifications**

```bash
# Déclencher un test de notification
curl -X POST "http://localhost:8086/api/fcm/test"
```

### 4. **Tester une alerte réelle**

```bash
# Tester une alerte pour un capteur
curl -X POST "http://localhost:8086/api/alerts/test/sensor_001"
```

## 📊 Endpoints disponibles

### **FCM Token Management**

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/fcm/register` | POST | Enregistrer un token FCM |
| `/api/fcm/deactivate` | POST | Désactiver un token FCM |
| `/api/fcm/stats` | GET | Statistiques des tokens |
| `/api/fcm/tokens/{localite}` | GET | Tokens pour une localité |
| `/api/fcm/test` | POST | Test de notification |

### **Alertes**

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/alerts/status/{sensorId}` | GET | Statut d'alerte d'un capteur |
| `/api/alerts/test/{sensorId}` | POST | Tester une alerte |
| `/api/alerts/calculate` | GET | Calculer un niveau d'alerte |
| `/api/alerts/reset/{sensorId}` | POST | Réinitialiser le cache |

## 🔄 Flux complet de test

### 1. **Enregistrer un token FCM**
```bash
curl -X POST "http://localhost:8086/api/fcm/register" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user_001",
    "fcmToken": "real_fcm_token_from_flutter",
    "deviceType": "android",
    "localite": "Dakar"
  }'
```

### 2. **Vérifier l'enregistrement**
```bash
curl -X GET "http://localhost:8086/api/fcm/stats"
```

### 3. **Simuler des données MQTT avec alerte**
Publiez un message MQTT avec un niveau d'eau élevé :
```json
{
  "device_id": "sensor_001",
  "timestamp": 1696158000,
  "niveau_eau": 90.0,
  "Temperature": 25.0,
  "vitesse_du_vent": 10.0,
  "latitude": 14.6928,
  "longitude": -17.4467
}
```

### 4. **Vérifier les logs**
Le backend devrait afficher :
```
📩 Message MQTT reçu : {...}
🚨 Alerte déclenchée pour le capteur sensor_001 - Niveau: Danger
📱 1 tokens FCM trouvés pour la localité: Dakar
✅ Notification push envoyée pour sensor_001 vers token: real_fcm_t...
📱 Notifications push envoyées: 1/1 pour sensor_001
```

### 5. **Vérifier la réception**
L'application Flutter devrait recevoir la notification push.

## 🚨 Gestion des erreurs

### **Tokens FCM invalides**
- Le backend détecte automatiquement les tokens invalides
- Les tokens échoués sont loggés mais n'interrompent pas le processus
- Utilisez les logs pour identifier les tokens problématiques

### **API Gateway indisponible**
- Les erreurs sont loggées mais n'interrompent pas le traitement MQTT
- Les données sont toujours sauvegardées dans Supabase
- Les alertes sont mises en cache pour retry ultérieur

### **Localité sans tokens**
- Aucune notification n'est envoyée si aucun token n'est trouvé
- Un message d'avertissement est affiché dans les logs
- Les SMS peuvent toujours être envoyés via l'autre endpoint

## 📈 Monitoring et logs

### **Logs importants à surveiller**

```
✅ Token FCM enregistré pour utilisateur: user_001 - Localité: Dakar
📱 3 tokens FCM trouvés pour la localité: Dakar
🚨 Alerte déclenchée pour le capteur sensor_001 - Niveau: Danger
✅ Notification push envoyée pour sensor_001 vers token: abc123...
📱 Notifications push envoyées: 3/3 pour sensor_001
⚠️ Aucun token FCM trouvé pour la localité: Thiès
❌ Erreur notification push pour token def456...: Connection timeout
```

### **Métriques à surveiller**

- Nombre de tokens FCM actifs par localité
- Taux de succès des notifications push
- Temps de réponse de l'API Gateway
- Nombre d'alertes déclenchées par jour

## 🔐 Sécurité

### **Bonnes pratiques**

1. **Ne jamais exposer les tokens FCM** dans les logs complets
2. **Utiliser HTTPS** pour toutes les communications
3. **Valider les tokens FCM** avant enregistrement
4. **Implémenter un rate limiting** sur les endpoints
5. **Auditer régulièrement** les tokens actifs

### **Nettoyage des tokens**

```sql
-- Désactiver les tokens anciens (plus de 30 jours sans utilisation)
UPDATE fcm_tokens 
SET is_active = false 
WHERE last_used < NOW() - INTERVAL '30 days' 
AND is_active = true;
```

## 🎯 Prochaines étapes

1. **Déployer la table** dans Supabase
2. **Configurer l'URL** de l'API Gateway
3. **Tester l'enregistrement** de tokens depuis Flutter
4. **Simuler des alertes** avec des données MQTT
5. **Vérifier la réception** des notifications
6. **Optimiser les performances** selon les besoins

---

**Votre système de notifications push backend est maintenant prêt ! 🚀**
