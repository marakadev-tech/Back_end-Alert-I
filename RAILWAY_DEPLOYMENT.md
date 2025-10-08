# 🚂 Déploiement sur Railway

## Variables d'Environnement Requises

Configurez ces variables dans Railway Dashboard :

### 1. Supabase
```
SUPABASE_URL=YOUR_SUPABASE_URL
SUPABASE_KEY=YOUR_SUPABASE_KEY
```

### 2. MQTT
```
MQTT_BROKER=YOUR_MQTT_BROKER
MQTT_PORT=8883
MQTT_USERNAME=YOUR_MQTT_USERNAME
MQTT_PASSWORD=YOUR_MQTT_PASSWORD
MQTT_TOPIC=sensors/data
```

### 3. Twilio
```
TWILIO_ACCOUNT_SID=YOUR_TWILIO_ACCOUNT_SID
TWILIO_AUTH_TOKEN=YOUR_TWILIO_AUTH_TOKEN
TWILIO_FROM_NUMBER=+YOUR_TWILIO_PHONE_NUMBER
```

### 4. AWS Lambda
```
AWS_API_GATEWAY_URL=YOUR_AWS_API_GATEWAY_URL
```

### 5. OpenWeatherMap
```
OPENWEATHERMAP_API_KEY=YOUR_OPENWEATHERMAP_API_KEY
```

### 6. Firebase (optionnel, si vous uploadez le fichier)
```
GOOGLE_APPLICATION_CREDENTIALS=/app/src/main/resources/firebase/serviceAccountKey.json
```

## Configuration Spring Boot

Railway définit automatiquement la variable `PORT`. Spring Boot l'utilisera via :
```
server.port=${PORT:8080}
```

## Commandes de Build

Railway exécute automatiquement :
```bash
./mvnw clean package -DskipTests
```

## Commande de Démarrage

```bash
java -Dserver.port=$PORT -jar target/alerti_back-0.0.1-SNAPSHOT.jar
```

## URL de l'Application

Après déploiement : `https://votre-app.up.railway.app`

## Logs

Accédez aux logs en temps réel depuis le Railway Dashboard.
