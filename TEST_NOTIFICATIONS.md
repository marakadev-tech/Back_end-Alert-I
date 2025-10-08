# 🧪 Guide de Test des Notifications Push

## 🚀 **Démarrage du système**

### 1. **Démarrer le backend Spring Boot**
```bash
cd /Users/macpro/IdeaProjects/Back_end-Alert-I
mvn spring-boot:run
```

### 2. **Vérifier que l'application démarre correctement**
```
✅ Connecté et abonné à Mosquitto (sans sécurité)
✅ Application démarrée sur le port 8086
```

## 📱 **Tests des notifications push**

### **Test 1 : Vérifier les tokens FCM enregistrés**
```bash
curl -X GET "https://98aee67247ba.ngrok-free.app/api/notifications/stats" \
  -H "ngrok-skip-browser-warning: true"
```

**Réponse attendue :**
```json
{
  "totalActiveTokens": 2,
  "tokens": [
    "dGVzdF90b2tlbl8xMjM...",
    "YW5vdGhlcl90b2tlbl8..."
  ]
}
```

### **Test 2 : Envoyer notification push à tous les tokens**
```bash
curl -X GET "https://98aee67247ba.ngrok-free.app/api/notifications/test-push" \
  -H "ngrok-skip-browser-warning: true"
```

**Réponse attendue :**
```json
{
  "success": true,
  "message": "Notification push envoyée avec succès",
  "tokensCount": 2,
  "sensorId": "test_sensor_001",
  "alertLevel": "DANGER",
  "localite": "Dakar"
}
```

### **Test 3 : Envoyer notification push pour une localité spécifique**
```bash
curl -X GET "http://localhost:8086/api/notifications/test-push/Dakar"
```

**Réponse attendue :**
```json
{
  "success": true,
  "message": "Notification push envoyée avec succès",
  "tokensCount": 1,
  "sensorId": "test_sensor_dakar",
  "alertLevel": "ATTENTION",
  "localite": "Dakar"
}
```

### **Test 4 : Envoyer notification SMS**
```bash
curl -X POST "http://localhost:8086/api/notifications/test-sms" \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+221771234567"}'
```

**Réponse attendue :**
```json
{
  "success": true,
  "message": "SMS envoyé avec succès",
  "phoneNumber": "+221771234567",
  "sensorId": "test_sensor_sms",
  "alertLevel": "DANGER"
}
```

### **Test 5 : Test avec un capteur existant**
```bash
curl -X POST "http://localhost:8086/api/notifications/test-sensor/sensor_001"
```

## 🔍 **Vérification côté Lambda**

### **Logs attendus dans votre fonction Lambda :**
```json
{
  "message": "🌊 ALERTE DANGER - test_sensor_001",
  "data": {
    "sensorId": "test_sensor_001",
    "localite": "Dakar",
    "alertLevel": "DANGER",
    "niveauEau": "85.0",
    "seuilEau": "100.0",
    "latitude": "14.6928",
    "longitude": "-17.4467"
  }
}
```

## 📱 **Vérification côté Flutter**

### **Notifications push reçues :**
- ✅ **Titre** : "🌊 Alerte Danger - Dakar"
- ✅ **Corps** : "Niveau: 85.0 cm (85% du seuil)"
- ✅ **Données** : sensorId, alertLevel, niveauEau, etc.

## 🐛 **Dépannage**

### **Problème : Aucun token FCM trouvé**
```json
{
  "success": false,
  "message": "Aucun token FCM actif trouvé",
  "tokensCount": 0
}
```

**Solutions :**
1. Vérifier que Flutter a enregistré les tokens dans Supabase
2. Vérifier la table `fcm_tokens` dans Supabase
3. Vérifier que `is_active = true`

### **Problème : Erreur API Gateway**
```json
{
  "success": false,
  "message": "Échec de l'envoi"
}
```

**Solutions :**
1. Vérifier l'URL de l'API Gateway dans `application.properties`
2. Vérifier que la fonction Lambda est déployée
3. Vérifier les logs CloudWatch de Lambda

### **Problème : Erreur de connexion Supabase**
```
❌ Erreur récupération tokens FCM: Connection refused
```

**Solutions :**
1. Vérifier l'URL et la clé Supabase
2. Vérifier la connectivité internet
3. Vérifier les politiques RLS dans Supabase

## 📊 **Tests avec Postman**

### **Collection Postman :**
```json
{
  "info": {
    "name": "Alert-I Notifications Test",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get Notification Stats",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8086/api/notifications/stats",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8086",
          "path": ["api", "notifications", "stats"]
        }
      }
    },
    {
      "name": "Test Push Notification",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8086/api/notifications/test-push",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8086",
          "path": ["api", "notifications", "test-push"]
        }
      }
    },
    {
      "name": "Test SMS Notification",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"phoneNumber\": \"+221771234567\"\n}"
        },
        "url": {
          "raw": "http://localhost:8086/api/notifications/test-sms",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8086",
          "path": ["api", "notifications", "test-sms"]
        }
      }
    }
  ]
}
```

## 🎯 **Tests de charge**

### **Test avec plusieurs tokens :**
```bash
# Envoyer 10 notifications de test
for i in {1..10}; do
  curl -X GET "http://localhost:8086/api/notifications/test-push" &
done
wait
```

## 📈 **Monitoring**

### **Logs à surveiller :**
```bash
# Logs du backend
tail -f logs/application.log | grep "Notification"

# Logs Lambda (CloudWatch)
aws logs tail /aws/lambda/your-function-name --follow
```

## ✅ **Checklist de validation**

- [ ] Backend démarre sans erreur
- [ ] Tokens FCM récupérés depuis Supabase
- [ ] API Gateway Lambda répond
- [ ] Notifications push reçues sur Flutter
- [ ] SMS reçus sur les numéros de test
- [ ] Logs Lambda corrects
- [ ] Gestion des erreurs fonctionnelle

---

**🎉 Une fois tous les tests passés, votre système d'alertes est prêt pour la production !**
