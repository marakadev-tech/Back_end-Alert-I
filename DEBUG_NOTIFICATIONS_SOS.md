# 🔍 Debug : Notifications SOS non reçues

## ✅ Ce qui fonctionne
- Backend Spring Boot envoie bien les requêtes
- 7 tokens FCM sont récupérés depuis Supabase
- Les logs indiquent "✅ Notification SOS envoyée avec succès"

## ❌ Problème
Les notifications ne sont **pas reçues** sur l'appareil mobile malgré les logs de succès.

## 🔎 Points à vérifier

### 1. **Configuration AWS API Key**
**Problème identifié** : Dans `application.properties` ligne 38, la clé API est incorrecte :
```properties
# ❌ INCORRECT
aws.lambda.api.key=https://aywkpqdshe.execute-api.us-east-1.amazonaws.com/default/send_notification

# ✅ CORRECT (à remplacer par votre vraie clé)
aws.lambda.api.key=YOUR_ACTUAL_API_KEY_HERE
```

**Comment obtenir la clé API :**
1. Allez sur AWS Console → API Gateway
2. Sélectionnez votre API
3. Allez dans "API Keys"
4. Copiez la clé API et remplacez `YOUR_ACTUAL_API_KEY_HERE`

### 2. **Vérifier les logs AWS Lambda**
Pour voir si la Lambda reçoit les requêtes :

```bash
# Via AWS CLI
aws logs tail /aws/lambda/send_notification --follow

# Ou via AWS Console
# CloudWatch → Log groups → /aws/lambda/send_notification
```

**Ce qu'on devrait voir :**
- Les requêtes entrantes avec les tokens
- Les réponses de FCM
- Les erreurs éventuelles (tokens invalides, erreurs d'authentification Firebase, etc.)

### 3. **Vérifier les tokens FCM dans Supabase**
Assurez-vous que les tokens sont valides :

```sql
-- Vérifier les tokens actifs
SELECT 
  id, 
  user_id, 
  fcm_token, 
  localite, 
  is_active, 
  created_at,
  updated_at
FROM fcm_tokens 
WHERE is_active = true
ORDER BY updated_at DESC;
```

**Points à vérifier :**
- Les tokens ne sont pas expirés
- Les tokens correspondent bien à votre appareil
- `is_active = true`

### 4. **Tester manuellement la Lambda**
Testez directement la Lambda avec un payload :

```bash
curl -X POST https://aywkpqdshe.execute-api.us-east-1.amazonaws.com/default/send_notification \
  -H "Content-Type: application/json" \
  -H "x-api-key: YOUR_API_KEY" \
  -d '{
    "tokens": ["VOTRE_TOKEN_FCM_ICI"],
    "title": "🚨 Test SOS",
    "body": "Test de notification SOS",
    "data": {
      "type": "sos_signal",
      "signalId": "123"
    }
  }'
```

### 5. **Vérifier la configuration Firebase**
Dans votre Lambda, assurez-vous que les variables d'environnement sont correctes :
- `FIREBASE_PROJECT_ID`
- `FIREBASE_CLIENT_EMAIL`
- `FIREBASE_PRIVATE_KEY`

### 6. **Vérifier l'application Flutter**
Assurez-vous que :
- Le service FCM est bien initialisé
- Les permissions de notifications sont accordées
- Le token FCM est bien enregistré dans Supabase

```dart
// Vérifier le token actuel
FirebaseMessaging messaging = FirebaseMessaging.instance;
String? token = await messaging.getToken();
print('📱 Token FCM actuel: $token');
```

### 7. **Format du payload**
Vérifiez que le backend envoie le bon format. Dans `NotificationService.java` :

```java
// Le payload devrait ressembler à :
{
  "tokens": ["token1", "token2", ...],
  "title": "🚨 Nouveau signalement SOS",
  "body": "Signalement: accident à bamako",
  "data": {
    "type": "sos_signal",
    "signalId": "9",
    "typeUrgence": "accident",
    "localite": "bamako",
    ...
  }
}
```

## 🛠️ Actions à effectuer

### Action 1 : Corriger la clé API
```properties
# Dans application.properties
aws.lambda.api.key=VOTRE_VRAIE_CLE_API_AWS
```

### Action 2 : Ajouter des logs détaillés
Modifiez `NotificationService.java` pour logger la réponse complète de Lambda :

```java
ResponseEntity<Map> response = restTemplate.exchange(
    lambdaUrl, HttpMethod.POST, entity, Map.class);

// Ajouter ce log
System.out.println("🔍 Réponse Lambda complète: " + response.getBody());

if (response.getStatusCode().is2xxSuccessful()) {
    Map<String, Object> body = response.getBody();
    System.out.println("✅ Success count: " + body.get("successCount"));
    System.out.println("❌ Error count: " + body.get("errorCount"));
    System.out.println("📋 Results: " + body.get("results"));
    return true;
}
```

### Action 3 : Vérifier les logs Lambda
Consultez CloudWatch pour voir les erreurs exactes de FCM.

### Action 4 : Tester avec un seul token
Pour simplifier le debug, testez d'abord avec un seul token :

```bash
curl -X POST https://aywkpqdshe.execute-api.us-east-1.amazonaws.com/default/send_notification \
  -H "Content-Type: application/json" \
  -H "x-api-key: YOUR_API_KEY" \
  -d '{
    "token": "VOTRE_TOKEN_FCM",
    "title": "Test",
    "body": "Test notification"
  }'
```

## 📊 Checklist de diagnostic

- [ ] Clé API AWS correctement configurée
- [ ] Logs Lambda consultés dans CloudWatch
- [ ] Tokens FCM vérifiés dans Supabase (valides et actifs)
- [ ] Test manuel de la Lambda réussi
- [ ] Variables d'environnement Firebase vérifiées dans Lambda
- [ ] Token FCM de l'appareil vérifié dans l'app Flutter
- [ ] Permissions de notifications accordées sur l'appareil
- [ ] Format du payload vérifié

## 🎯 Erreurs courantes

### Erreur 1 : Token FCM invalide ou expiré
**Symptôme** : Lambda retourne succès mais FCM rejette le token
**Solution** : Régénérer le token dans l'app Flutter et le réenregistrer dans Supabase

### Erreur 2 : Clé API manquante ou invalide
**Symptôme** : Erreur 403 Forbidden
**Solution** : Vérifier la clé API dans `application.properties` et dans le header `x-api-key`

### Erreur 3 : Credentials Firebase invalides
**Symptôme** : Erreur 401 Unauthorized de FCM
**Solution** : Vérifier les variables d'environnement de la Lambda

### Erreur 4 : App en arrière-plan
**Symptôme** : Notifications non affichées quand l'app est ouverte
**Solution** : Implémenter `FirebaseMessaging.onMessage` dans Flutter

## 📝 Prochaines étapes

1. **Corriger la clé API** dans `application.properties`
2. **Consulter les logs Lambda** dans CloudWatch
3. **Ajouter des logs détaillés** dans `NotificationService.java`
4. **Tester manuellement** la Lambda avec curl
5. **Vérifier les tokens FCM** dans Supabase et dans l'app Flutter

