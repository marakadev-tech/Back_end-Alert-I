# ✅ Correction : Notifications FCM non reçues

## 🐛 Problème identifié

**Erreur FCM** : `Invalid value at 'message.data[2].value' (TYPE_STRING), 0`

### Cause
Firebase Cloud Messaging (FCM) **exige que toutes les valeurs dans le champ `data` soient des chaînes de caractères (String)**.

Dans le code, nous envoyions des valeurs de type `Integer` :
```java
// ❌ INCORRECT
Map<String, Object> data = new HashMap<>();
data.put("signalId", sosSignal.getId());  // Integer, pas String!
```

### Logs d'erreur
```
🔍 Réponse Lambda complète: {
  success=false, 
  totalTokens=7, 
  successCount=0, 
  errorCount=7,
  results=[{
    token=e5i8IhmSRk..., 
    status=400, 
    success=false, 
    data={
      error={
        code=400, 
        message=Invalid value at 'message.data[2].value' (TYPE_STRING), 0
      }
    }
  }]
}
```

## ✅ Solution appliquée

### Modification dans `NotificationService.java`

**Avant :**
```java
Map<String, Object> data = new HashMap<>();
data.put("signalId", sosSignal.getId());  // Integer
data.put("sensorId", sensor.getId());     // Integer
```

**Après :**
```java
Map<String, String> data = new HashMap<>();  // String au lieu de Object
data.put("signalId", String.valueOf(sosSignal.getId()));  // Converti en String
data.put("sensorId", String.valueOf(sensor.getId()));     // Converti en String
```

### Fichiers modifiés

1. **`NotificationService.java`** - Méthode `createSosNotificationPayload()` (ligne ~560)
   - Changé `Map<String, Object>` en `Map<String, String>`
   - Converti `sosSignal.getId()` avec `String.valueOf()`
   - Ajouté des vérifications null pour éviter les NPE

2. **`NotificationService.java`** - Méthodes `sendToAllTokens()` et `sendToSingleToken()` (lignes ~85 et ~140)
   - Changé `Map<String, Object>` en `Map<String, String>`
   - Converti `sensor.getId()` avec `String.valueOf()`
   - Ajouté des vérifications null

## 📋 Changements détaillés

### Pour les notifications SOS
```java
// Données additionnelles - TOUTES les valeurs doivent être des strings pour FCM
Map<String, String> data = new HashMap<>();
data.put("type", "sos_signal");
data.put("signalId", String.valueOf(sosSignal.getId()));  // ✅ Converti en String
data.put("typeUrgence", sosSignal.getTypeUrgence() != null ? sosSignal.getTypeUrgence() : "");
data.put("description", sosSignal.getDescription() != null ? sosSignal.getDescription() : "");
data.put("localite", sosSignal.getLocalite() != null ? sosSignal.getLocalite() : "");
data.put("latitude", sosSignal.getLatitude() != null ? sosSignal.getLatitude().toString() : "0");
data.put("longitude", sosSignal.getLongitude() != null ? sosSignal.getLongitude().toString() : "0");
data.put("statut", sosSignal.getStatut() != null ? sosSignal.getStatut() : "");
data.put("priorite", sosSignal.getPriorite() != null ? sosSignal.getPriorite() : "");
data.put("timestamp", sosSignal.getSignalTimestamp() != null ? sosSignal.getSignalTimestamp().toString() : "");
data.put("anonyme", sosSignal.isAnonyme() ? "true" : "false");
```

### Pour les notifications de capteurs
```java
// Données additionnelles - TOUTES les valeurs doivent être des strings pour FCM
Map<String, String> data = new HashMap<>();
data.put("sensorId", String.valueOf(sensor.getId()));  // ✅ Converti en String
data.put("localite", sensor.getLocalite() != null ? sensor.getLocalite() : "");
data.put("alertLevel", alertLevel.name());
data.put("niveauEau", sensor.getDernierDonneeCapniveauEau() != null ? sensor.getDernierDonneeCapniveauEau().toString() : "0");
data.put("seuilEau", sensor.getSeuilniveauEau() != null ? sensor.getSeuilniveauEau().toString() : "0");
data.put("latitude", sensor.getLatitude() != null ? sensor.getLatitude().toString() : "0");
data.put("longitude", sensor.getLongitude() != null ? sensor.getLongitude().toString() : "0");
data.put("timestamp", sensor.getTimestamp() != null ? sensor.getTimestamp().toString() : "");
```

## 🧪 Test

### Avant la correction
```
successCount=0
errorCount=7
message=Sent to 0/7 tokens successfully
```

### Après la correction (attendu)
```
successCount=7
errorCount=0
message=Sent to 7/7 tokens successfully
```

## 📝 Prochaines étapes

1. **Redémarrer le backend Spring Boot**
   ```bash
   cd /Users/macpro/IdeaProjects/Back_end-Alert-I
   mvn spring-boot:run
   ```

2. **Créer un nouveau signalement SOS** depuis l'app Flutter

3. **Vérifier les logs** - Vous devriez maintenant voir :
   ```
   ✅ Success count: 7
   ❌ Error count: 0
   📋 Message: Sent to 7/7 tokens successfully
   ✅ Notification SOS envoyée avec succès à 7 appareil(s)
   ```

4. **Vérifier la réception** sur votre appareil mobile

## 📚 Leçon apprise

**Règle FCM importante** : Le champ `data` dans les notifications FCM ne peut contenir que des paires clé-valeur où **les valeurs sont TOUJOURS des strings**.

### Types de données FCM

| Type Java | FCM accepte ? | Solution |
|-----------|---------------|----------|
| `String` | ✅ Oui | Utiliser directement |
| `Integer` | ❌ Non | `String.valueOf(value)` |
| `Long` | ❌ Non | `String.valueOf(value)` |
| `Double` | ❌ Non | `value.toString()` |
| `Boolean` | ❌ Non | `value ? "true" : "false"` |
| `null` | ❌ Non | Fournir une valeur par défaut `""` |

### Référence officielle
[Firebase Cloud Messaging - Data messages](https://firebase.google.com/docs/cloud-messaging/concept-options#data_messages)

> "Data messages have only custom key-value pairs with no reserved key names. **Data message values are strings.**"

## ✅ Résultat

Les notifications SOS devraient maintenant être **correctement reçues** sur tous les appareils ! 🎉

