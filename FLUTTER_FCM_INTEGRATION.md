# Intégration Flutter - Enregistrement des tokens FCM

## 📱 Code Flutter pour enregistrer les tokens FCM dans Supabase

### 1. **Dépendances dans `pubspec.yaml`**

```yaml
dependencies:
  flutter:
    sdk: flutter
  supabase_flutter: ^2.0.0
  firebase_messaging: ^14.0.0
  firebase_core: ^2.0.0
```

### 2. **Service Flutter pour gérer les tokens FCM**

Créez le fichier `lib/services/fcm_token_service.dart` :

```dart
import 'dart:io';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:shared_preferences/shared_preferences.dart';

class FcmTokenService {
  static const String _localiteKey = 'selected_localite';
  
  /// Enregistre le token FCM dans Supabase
  static Future<void> registerFcmToken() async {
    try {
      // Obtenir le token FCM
      String? token = await FirebaseMessaging.instance.getToken();
      
      if (token == null) {
        print('❌ Impossible d\'obtenir le token FCM');
        return;
      }
      
      // Obtenir l'utilisateur actuel
      final user = Supabase.instance.client.auth.currentUser;
      
      if (user == null) {
        print('❌ Utilisateur non connecté');
        return;
      }
      
      // Récupérer la localité sélectionnée
      final prefs = await SharedPreferences.getInstance();
      String localite = prefs.getString(_localiteKey) ?? 'Dakar';
      
      // Vérifier si le token existe déjà
      final existingToken = await _getExistingToken(user.id);
      
      if (existingToken != null) {
        // Mettre à jour le token existant
        await _updateToken(user.id, token, localite);
        print('✅ Token FCM mis à jour pour l\'utilisateur ${user.id}');
      } else {
        // Créer un nouveau token
        await _createToken(user.id, token, localite);
        print('✅ Nouveau token FCM enregistré pour l\'utilisateur ${user.id}');
      }
      
    } catch (e) {
      print('❌ Erreur enregistrement token FCM: $e');
    }
  }
  
  /// Crée un nouveau token FCM
  static Future<void> _createToken(String userId, String token, String localite) async {
    await Supabase.instance.client
        .from('fcm_tokens')
        .insert({
          'user_id': userId,
          'fcm_token': token,
          'device_type': Platform.isAndroid ? 'android' : 'ios',
          'localite': localite,
          'is_active': true,
          'created_at': DateTime.now().toIso8601String(),
          'updated_at': DateTime.now().toIso8601String(),
          'last_used': DateTime.now().toIso8601String(),
        });
  }
  
  /// Met à jour un token FCM existant
  static Future<void> _updateToken(String userId, String token, String localite) async {
    await Supabase.instance.client
        .from('fcm_tokens')
        .update({
          'fcm_token': token,
          'localite': localite,
          'is_active': true,
          'updated_at': DateTime.now().toIso8601String(),
          'last_used': DateTime.now().toIso8601String(),
        })
        .eq('user_id', userId);
  }
  
  /// Récupère le token existant pour un utilisateur
  static Future<Map<String, dynamic>?> _getExistingToken(String userId) async {
    try {
      final response = await Supabase.instance.client
          .from('fcm_tokens')
          .select('*')
          .eq('user_id', userId)
          .single();
      
      return response;
    } catch (e) {
      return null; // Token n'existe pas
    }
  }
  
  /// Met à jour la localité d'intérêt de l'utilisateur
  static Future<void> updateLocalite(String localite) async {
    try {
      final user = Supabase.instance.client.auth.currentUser;
      if (user == null) return;
      
      // Sauvegarder dans les préférences locales
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_localiteKey, localite);
      
      // Mettre à jour dans Supabase
      await Supabase.instance.client
          .from('fcm_tokens')
          .update({
            'localite': localite,
            'updated_at': DateTime.now().toIso8601String(),
          })
          .eq('user_id', user.id);
      
      print('✅ Localité mise à jour: $localite');
    } catch (e) {
      print('❌ Erreur mise à jour localité: $e');
    }
  }
  
  /// Désactive le token FCM (lors de la déconnexion)
  static Future<void> deactivateToken() async {
    try {
      final user = Supabase.instance.client.auth.currentUser;
      if (user == null) return;
      
      await Supabase.instance.client
          .from('fcm_tokens')
          .update({
            'is_active': false,
            'updated_at': DateTime.now().toIso8601String(),
          })
          .eq('user_id', user.id);
      
      print('✅ Token FCM désactivé');
    } catch (e) {
      print('❌ Erreur désactivation token: $e');
    }
  }
}
```

### 3. **Configuration Firebase**

Créez le fichier `lib/firebase_options.dart` (généré par FlutterFire CLI) :

```dart
// Fichier généré par: flutterfire configure
import 'package:firebase_core/firebase_core.dart' show FirebaseOptions;
import 'package:flutter/foundation.dart'
    show defaultTargetPlatform, kIsWeb, TargetPlatform;

class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    if (kIsWeb) {
      return web;
    }
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return android;
      case TargetPlatform.iOS:
        return ios;
      default:
        throw UnsupportedError(
          'DefaultFirebaseOptions are not supported for this platform.',
        );
    }
  }

  static const FirebaseOptions web = FirebaseOptions(
    apiKey: 'your-web-api-key',
    appId: 'your-web-app-id',
    messagingSenderId: 'your-sender-id',
    projectId: 'your-project-id',
  );

  static const FirebaseOptions android = FirebaseOptions(
    apiKey: 'your-android-api-key',
    appId: 'your-android-app-id',
    messagingSenderId: 'your-sender-id',
    projectId: 'your-project-id',
  );

  static const FirebaseOptions ios = FirebaseOptions(
    apiKey: 'your-ios-api-key',
    appId: 'your-ios-app-id',
    messagingSenderId: 'your-sender-id',
    projectId: 'your-project-id',
  );
}
```

### 4. **Initialisation dans `main.dart`**

```dart
import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'firebase_options.dart';
import 'services/fcm_token_service.dart';

// Handler pour les notifications en arrière-plan
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
  print('📱 Notification reçue en arrière-plan: ${message.messageId}');
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Initialiser Firebase
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );
  
  // Initialiser Supabase
  await Supabase.initialize(
    url: 'https://wpmowqykjelftkptiquf.supabase.co',
    anonKey: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndwbW93cXlramVsZnRrcHRpcXVmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTM5NzQ1NDUsImV4cCI6MjA2OTU1MDU0NX0.E1fR-RYE4Eq-GACe6XWXuHhMO58ydQuTEPTgUg46bI0',
  );
  
  // Configurer le handler pour les notifications en arrière-plan
  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);
  
  // Enregistrer le token FCM
  await FcmTokenService.registerFcmToken();
  
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Alert-I',
      home: HomeScreen(),
    );
  }
}
```

### 5. **Écoute des notifications dans l'app**

```dart
class HomeScreen extends StatefulWidget {
  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  void initState() {
    super.initState();
    _setupNotificationListeners();
  }
  
  void _setupNotificationListeners() {
    // Notification reçue quand l'app est au premier plan
    FirebaseMessaging.onMessage.listen((RemoteMessage message) {
      print('📱 Notification reçue: ${message.notification?.title}');
      
      // Afficher une dialog ou snackbar
      _showNotificationDialog(message);
    });
    
    // Notification reçue quand l'app est en arrière-plan et ouverte
    FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
      print('📱 App ouverte depuis notification: ${message.notification?.title}');
      
      // Naviguer vers la page d'alerte
      _navigateToAlertPage(message.data);
    });
  }
  
  void _showNotificationDialog(RemoteMessage message) {
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
              _navigateToAlertPage(message.data);
            },
            child: Text('Voir détails'),
          ),
        ],
      ),
    );
  }
  
  void _navigateToAlertPage(Map<String, dynamic> data) {
    // Naviguer vers la page d'alerte avec les données
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => AlertDetailsPage(
          sensorId: data['sensorId'],
          alertLevel: data['alertLevel'],
          niveauEau: data['niveauEau'],
          localite: data['localite'],
        ),
      ),
    );
  }
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Alert-I')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('Application Alert-I'),
            SizedBox(height: 20),
            ElevatedButton(
              onPressed: () async {
                await FcmTokenService.registerFcmToken();
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Token FCM enregistré')),
                );
              },
              child: Text('Enregistrer Token FCM'),
            ),
            SizedBox(height: 10),
            ElevatedButton(
              onPressed: () async {
                await FcmTokenService.updateLocalite('Thiès');
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Localité mise à jour: Thiès')),
                );
              },
              child: Text('Changer Localité vers Thiès'),
            ),
          ],
        ),
      ),
    );
  }
}
```

### 6. **Configuration Android**

Dans `android/app/src/main/AndroidManifest.xml` :

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<application
    android:label="Alert-I"
    android:name="${applicationName}"
    android:icon="@mipmap/ic_launcher">
    
    <!-- Service pour les notifications FCM -->
    <service
        android:name="io.flutter.plugins.firebase.messaging.FlutterFirebaseMessagingService"
        android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
    </service>
</application>
```

### 7. **Configuration iOS**

Dans `ios/Runner/Info.plist` :

```xml
<key>UIBackgroundModes</key>
<array>
    <string>fetch</string>
    <string>remote-notification</string>
</array>
```

## 🎯 **Flux complet :**

1. **Flutter démarre** → Génère token FCM
2. **Flutter** → Enregistre token dans Supabase
3. **Capteur envoie données** → Backend analyse
4. **Backend** → Récupère tokens depuis Supabase
5. **Backend** → Appelle Lambda avec tokens
6. **Lambda** → Envoie notifications push
7. **Flutter** → Reçoit et affiche notifications

## ✅ **Avantages de cette approche :**

- ✅ **Plus simple** : Pas d'endpoint backend pour les tokens
- ✅ **Plus rapide** : Communication directe Flutter → Supabase
- ✅ **Plus fiable** : Moins de points de défaillance
- ✅ **Gestion automatique** : Mise à jour des tokens et localités

---

**Note** : N'oubliez pas de configurer Firebase Console et d'ajouter les fichiers de configuration générés par FlutterFire CLI.

