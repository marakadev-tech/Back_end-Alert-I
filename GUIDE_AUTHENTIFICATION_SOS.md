# 🔐 Guide d'Authentification - Signalements SOS

## ❌ **Problème identifié**
Les endpoints SOS nécessitaient un token JWT, mais l'application Flutter tentait d'y accéder sans authentification.

## ✅ **Solution implémentée**

### Configuration de sécurité mise à jour
```java
// Dans SecurityConfig.java
.requestMatchers("/api/sos/signal-anonyme").permitAll()  // Signalements anonymes sans auth
.requestMatchers("/api/sos/types-urgence").permitAll()   // Types d'urgence sans auth
.requestMatchers("/api/sos/priorites").permitAll()       // Priorités sans auth
.requestMatchers("/api/sos/statuts").permitAll()         // Statuts sans auth
.requestMatchers("/api/sos/signaux").permitAll()         // Liste publique sans auth
.anyRequest().authenticated()  // Tous les autres endpoints nécessitent un token
```

## 📋 **Endpoints disponibles sans authentification**

### ✅ **Accessibles sans token**
| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/sos/signal-anonyme` | POST | Créer un signalement anonyme |
| `/api/sos/types-urgence` | GET | Récupérer les types d'urgence |
| `/api/sos/priorites` | GET | Récupérer les niveaux de priorité |
| `/api/sos/statuts` | GET | Récupérer les statuts possibles |
| `/api/sos/signaux` | GET | Liste publique des signalements |

### 🔒 **Nécessitent un token JWT**
| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/sos/signal` | POST | Créer un signalement authentifié |
| `/api/sos/signaux/user` | GET | Signalements d'un utilisateur spécifique |
| `/api/sos/signal/{id}` | PUT | Modifier un signalement |
| `/api/sos/signal/{id}` | GET | Détail d'un signalement (si privé) |
| `/api/sos/urgents` | GET | Signalements urgents (si protégé) |
| `/api/sos/statistiques` | GET | Statistiques (si protégé) |

## 🔄 **Flux d'authentification**

### Signalement anonyme
```dart
// Flutter - Pas de token requis
static Future<SosSignal> createAnonymousSignal(SosSignal signal) async {
  final response = await http.post(
    Uri.parse('$_baseUrl/signal-anonyme'),
    headers: _defaultHeaders,  // Pas d'Authorization header
    body: json.encode(signal.toJsonForAnonymous()),
  );
}
```

### Signalement authentifié
```dart
// Flutter - Token requis
static Future<SosSignal> createAuthenticatedSignal(SosSignal signal, String token) async {
  final response = await http.post(
    Uri.parse('$_baseUrl/signal'),
    headers: _getAuthHeaders(token),  // Avec Authorization: Bearer {token}
    body: json.encode(signal.toJson()),
  );
}
```

## 🎯 **Cas d'usage**

### Signalement anonyme
- ✅ **Urgence immédiate** - Pas besoin de créer un compte
- ✅ **Confidentialité** - Aucune donnée personnelle requise
- ✅ **Rapidité** - Accès direct sans authentification
- ❌ **Suivi** - Impossible de retrouver le signalement
- ❌ **Historique** - Pas d'historique personnel

### Signalement authentifié
- ✅ **Suivi** - Historique des signalements
- ✅ **Mise à jour** - Possibilité de modifier le statut
- ✅ **Statistiques** - Compteur personnel
- ❌ **Confidentialité** - Données personnelles liées
- ❌ **Complexité** - Nécessite une authentification

## 🚀 **Test des endpoints**

### Test sans token (anonyme)
```bash
# Créer un signalement anonyme
curl -X POST https://your-api.com/api/sos/signal-anonyme \
  -H "Content-Type: application/json" \
  -d '{
    "typeUrgence": "INONDATION",
    "description": "Test anonyme",
    "localite": "Dakar",
    "latitude": 14.6937,
    "longitude": -17.4441,
    "priorite": "HAUTE",
    "anonyme": true
  }'
```

### Test avec token (authentifié)
```bash
# Créer un signalement authentifié
curl -X POST https://your-api.com/api/sos/signal \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "typeUrgence": "INONDATION",
    "description": "Test authentifié",
    "localite": "Dakar",
    "latitude": 14.6937,
    "longitude": -17.4441,
    "priorite": "HAUTE",
    "userId": 123
  }'
```

## 📱 **Configuration Flutter**

### Headers par défaut (sans auth)
```dart
static const Map<String, String> defaultHeaders = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
  'ngrok-skip-browser-warning': 'true',
};
```

### Headers avec authentification
```dart
static Map<String, String> _getAuthHeaders(String? token) {
  final headers = Map<String, String>.from(_defaultHeaders);
  if (token != null) {
    headers['Authorization'] = 'Bearer $token';
  }
  return headers;
}
```

## ✅ **Résultat**

Maintenant, les endpoints suivants fonctionnent **sans token** :
- ✅ Création de signalements anonymes
- ✅ Récupération des types d'urgence
- ✅ Récupération des priorités
- ✅ Récupération des statuts
- ✅ Liste publique des signalements

**L'application Flutter peut maintenant créer des signalements SOS sans nécessiter d'authentification !** 🎯✨
