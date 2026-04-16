package com.example.alerti_back.Service;

import com.example.alerti_back.Model.AlertLevel;
import com.example.alerti_back.Model.Sensors;
import com.example.alerti_back.Model.SosSignal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service pour envoyer des notifications push et SMS via AWS Lambda + API Gateway
 */
@Service
public class NotificationService {

    @Value("${aws.api-gateway.url:}")
    private String lambdaUrl;

    @Value("${aws.lambda.api.key:}")
    private String apiKey;
    
    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Envoie une notification push et SMS via l'API Gateway Lambda
     * Récupère automatiquement les tokens FCM depuis Supabase
     *
     * @param sensor Le capteur concerné
     * @param alertLevel Le niveau d'alerte
     * @param recipients Liste des numéros SMS (optionnel)
     * @return true si l'envoi est réussi
     */
    public boolean sendAlert(Sensors sensor, AlertLevel alertLevel, List<String> recipients) {
        System.out.println("🔍 DEBUG - Configuration Lambda:");
        System.out.println("   lambdaUrl: " + (lambdaUrl != null ? lambdaUrl : "NULL"));
        System.out.println("   apiKey: " + (apiKey != null && !apiKey.isEmpty() ? "CONFIGURÉ" : "NON CONFIGURÉ"));
        
        if (lambdaUrl == null || lambdaUrl.isEmpty()) {
            System.err.println("⚠️  URL de l'API Gateway Lambda non configurée");
            System.err.println("   Variable AWS_API_GATEWAY_URL manquante dans Railway");
            return false;
        }

        try {
            // 🌍 Récupérer TOUS les tokens FCM (toutes localités confondues)
            List<String> fcmTokens = getAllFcmTokens();
            
            if (fcmTokens.isEmpty()) {
                System.out.println("⚠️ Aucun token FCM trouvé dans le système");
                return false;
            }
            
            System.out.println("📢 Envoi notification à TOUS les utilisateurs (" + fcmTokens.size() + " tokens)");
            
            // Envoyer à tous les tokens en une seule requête
            return sendToAllTokens(fcmTokens, sensor, alertLevel);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de la notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envoie une notification à tous les tokens FCM
     */
    private boolean sendToAllTokens(List<String> tokens, Sensors sensor, AlertLevel alertLevel) {
        try {
            // Format compatible avec votre Lambda - envoi à tous les tokens
            Map<String, Object> payload = new HashMap<>();
            payload.put("tokens", tokens); // Liste de tous les tokens
            payload.put("title", "🚨 " + alertLevel.getLabel() + " - " + sensor.getLocalite());
            payload.put("body", buildShortMessage(sensor, alertLevel));
            
            // Données additionnelles - TOUTES les valeurs doivent être des strings pour FCM
            Map<String, String> data = new HashMap<>();
            data.put("sensorId", String.valueOf(sensor.getId()));
            data.put("localite", sensor.getLocalite() != null ? sensor.getLocalite() : "");
            data.put("alertLevel", alertLevel.name());
            data.put("niveauEau", sensor.getDernierDonneeCapniveauEau() != null ? sensor.getDernierDonneeCapniveauEau().toString() : "0");
            data.put("seuilEau", sensor.getSeuilniveauEau() != null ? sensor.getSeuilniveauEau().toString() : "0");
            data.put("latitude", sensor.getLatitude() != null ? sensor.getLatitude().toString() : "0");
            data.put("longitude", sensor.getLongitude() != null ? sensor.getLongitude().toString() : "0");
            data.put("timestamp", sensor.getTimestamp() != null ? sensor.getTimestamp().toString() : "");
            
            payload.put("data", data);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Ajouter la clé API si configurée
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.set("x-api-key", apiKey);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            System.out.println("🚀 Envoi vers Lambda URL: " + lambdaUrl);
            System.out.println("📦 Payload: " + payload.toString());

            ResponseEntity<Map> response = restTemplate.exchange(
                    lambdaUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            System.out.println("📡 Réponse Lambda - Status: " + response.getStatusCode());
            System.out.println("📡 Réponse Lambda - Body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Notification envoyée avec succès pour le capteur " + sensor.getId() + " vers " + tokens.size() + " tokens");
                return true;
            } else {
                System.err.println("⚠️ Échec de l'envoi de notification: " + response.getStatusCode());
                System.err.println("📡 Réponse complète: " + response.getBody());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi notification vers " + tokens.size() + " tokens: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Envoie une notification à un token FCM spécifique
     */
    private boolean sendToSingleToken(String token, Sensors sensor, AlertLevel alertLevel) {
        try {
            // Format compatible avec votre Lambda
            Map<String, Object> payload = new HashMap<>();
            payload.put("token", token);
            payload.put("title", "🚨 " + alertLevel.getLabel() + " - " + sensor.getLocalite());
            payload.put("body", buildShortMessage(sensor, alertLevel));
            
            // Données additionnelles - TOUTES les valeurs doivent être des strings pour FCM
            Map<String, String> data = new HashMap<>();
            data.put("sensorId", String.valueOf(sensor.getId()));
            data.put("localite", sensor.getLocalite() != null ? sensor.getLocalite() : "");
            data.put("alertLevel", alertLevel.name());
            data.put("niveauEau", sensor.getDernierDonneeCapniveauEau() != null ? sensor.getDernierDonneeCapniveauEau().toString() : "0");
            data.put("seuilEau", sensor.getSeuilniveauEau() != null ? sensor.getSeuilniveauEau().toString() : "0");
            data.put("latitude", sensor.getLatitude() != null ? sensor.getLatitude().toString() : "0");
            data.put("longitude", sensor.getLongitude() != null ? sensor.getLongitude().toString() : "0");
            data.put("timestamp", sensor.getTimestamp() != null ? sensor.getTimestamp().toString() : "");
            
            payload.put("data", data);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Ajouter la clé API si configurée
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.set("x-api-key", apiKey);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    lambdaUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Notification envoyée avec succès pour le capteur " + sensor.getId() + " vers token: " + token.substring(0, Math.min(10, token.length())) + "...");
                return true;
            } else {
                System.err.println("⚠️ Échec de l'envoi de notification: " + response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur notification pour token " + token.substring(0, Math.min(10, token.length())) + "...: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Construit un message court pour la notification
     */
    private String buildShortMessage(Sensors sensor, AlertLevel alertLevel) {
        // Utiliser les données de pluviométrie au lieu du niveau d'eau
        double pluviometrie = sensor.getPluviometrieJour() != null ? sensor.getPluviometrieJour() : 0.0;
        double seuil = sensor.getSeuilPluviometrie() != null ? sensor.getSeuilPluviometrie() : 0.0;
        double pourcentage = 0;
        
        if (seuil > 0) {
            pourcentage = (pluviometrie / seuil) * 100;
        }
        
        // Message personnalisé selon le niveau d'alerte
        String message = "";
        switch (alertLevel) {
            case NORMAL:
                message = String.format("🌤️ Pluviométrie normale: %.1f mm/jour (%.0f%% du seuil)", pluviometrie, pourcentage);
                break;
            case ATTENTION:
                message = String.format("⚠️ Attention: %.1f mm/jour (%.0f%% du seuil) - Risque d'inondation", pluviometrie, pourcentage);
                break;
            case DANGER:
                message = String.format("🚨 DANGER: %.1f mm/jour (%.0f%% du seuil) - Évacuation recommandée!", pluviometrie, pourcentage);
                break;
            default:
                message = String.format("📊 Pluviométrie: %.1f mm/jour (%.0f%% du seuil)", pluviometrie, pourcentage);
        }
        
        return message;
    }

    /**
     * Construit le payload pour l'API Gateway Lambda
     */
    private Map<String, Object> buildNotificationPayload(Sensors sensor, AlertLevel alertLevel, List<String> recipients) {
        Map<String, Object> payload = new HashMap<>();
        
        // Informations générales
        payload.put("alertLevel", alertLevel.name());
        payload.put("severity", alertLevel.getLabel());
        payload.put("message", buildAlertMessage(sensor, alertLevel));
        
        // Informations du capteur
        Map<String, Object> sensorInfo = new HashMap<>();
        sensorInfo.put("id", sensor.getId());
        sensorInfo.put("localite", sensor.getLocalite());
        sensorInfo.put("niveauEau", sensor.getDernierDonneeCapniveauEau());
        sensorInfo.put("seuilNiveauEau", sensor.getSeuilniveauEau());
        sensorInfo.put("latitude", sensor.getLatitude());
        sensorInfo.put("longitude", sensor.getLongitude());
        sensorInfo.put("timestamp", sensor.getTimestamp());
        
        payload.put("sensor", sensorInfo);
        payload.put("recipients", recipients);
        
        // Type de notification
        payload.put("notificationType", "BOTH"); // PUSH, SMS, ou BOTH
        
        return payload;
    }

    /**
     * Construit le message d'alerte personnalisé
     */
    private String buildAlertMessage(Sensors sensor, AlertLevel alertLevel) {
        String location = sensor.getLocalite() != null ? sensor.getLocalite() : "Zone " + sensor.getId();
        Double currentLevel = sensor.getDernierDonneeCapniveauEau();
        Double threshold = sensor.getSeuilniveauEau();
        
        String message = String.format(
            "🚨 ALERTE %s - %s\n" +
            "📍 Localisation: %s\n" +
            "💧 Niveau d'eau actuel: %.2f m\n" +
            "⚠️  Seuil configuré: %.2f m\n" +
            "%s",
            alertLevel.getLabel().toUpperCase(),
            location,
            location,
            currentLevel != null ? currentLevel : 0.0,
            threshold != null ? threshold : 0.0,
            alertLevel.getDescription()
        );
        
        return message;
    }

    /**
     * Envoie une notification de test
     */
    public boolean sendTestNotification(String recipient) {
        if (lambdaUrl == null || lambdaUrl.isEmpty()) {
            System.err.println("⚠️  URL de l'API Gateway Lambda non configurée");
            return false;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("message", "Test de notification - Système d'alerte Alert-I");
            payload.put("recipients", List.of(recipient));
            payload.put("notificationType", "BOTH");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.set("x-api-key", apiKey);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    lambdaUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de la notification de test: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère TOUS les tokens FCM (toutes localités confondues)
     */
    public List<String> getAllFcmTokens() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = supabaseUrl + "/rest/v1/fcm_tokens" +
                    "?is_active=eq.true" +
                    "&select=fcm_token";

            ResponseEntity<Map[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map[].class
            );

            List<String> tokens = new ArrayList<>();
            if (response.getBody() != null) {
                for (Map<String, Object> tokenData : response.getBody()) {
                    String token = (String) tokenData.get("fcm_token");
                    if (token != null && !token.isEmpty()) {
                        tokens.add(token);
                    }
                }
            }
            
            System.out.println("🌍 Récupération de " + tokens.size() + " tokens FCM (toutes localités)");
            return tokens;

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération tous les tokens FCM: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupère les tokens FCM pour une localité donnée depuis Supabase
     */
    public List<String> getFcmTokensForLocalite(String localite) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = supabaseUrl + "/rest/v1/fcm_tokens" +
                    "?localite=eq." + localite +
                    "&is_active=eq.true" +
                    "&select=fcm_token";

            ResponseEntity<Map[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map[].class
            );

            List<String> tokens = new ArrayList<>();
            if (response.getBody() != null) {
                for (Map<String, Object> tokenData : response.getBody()) {
                    String token = (String) tokenData.get("fcm_token");
                    if (token != null && !token.isEmpty()) {
                        tokens.add(token);
                    }
                }
            }

            System.out.println("📱 " + tokens.size() + " tokens FCM trouvés pour la localité: " + localite);
            return tokens;
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération tokens FCM: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Récupère tous les tokens FCM actifs (pour alertes globales)
     */
    public List<String> getAllActiveFcmTokens() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = supabaseUrl + "/rest/v1/fcm_tokens" +
                    "?is_active=eq.true" +
                    "&select=fcm_token";

            ResponseEntity<Map[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map[].class
            );

            List<String> tokens = new ArrayList<>();
            if (response.getBody() != null) {
                for (Map<String, Object> tokenData : response.getBody()) {
                    String token = (String) tokenData.get("fcm_token");
                    if (token != null && !token.isEmpty()) {
                        tokens.add(token);
                    }
                }
            }

            System.out.println("📱 " + tokens.size() + " tokens FCM actifs trouvés au total");
            return tokens;
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération tous tokens FCM: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== MÉTHODES POUR NOTIFICATIONS SOS ====================

    /**
     * Envoie une notification pour un signalement SOS
     * @param sosSignal Le signalement SOS
     * @param typeNotification Type de notification (confirmation, update, resolution)
     * @return true si l'envoi est réussi
     */
    public boolean sendSosNotification(SosSignal sosSignal, String typeNotification) {
        if (lambdaUrl == null || lambdaUrl.isEmpty()) {
            System.err.println("⚠️ URL de l'API Gateway Lambda non configurée");
            return false;
        }

        try {
            // Récupérer les tokens FCM
            List<String> fcmTokens = getAllFcmTokens();
            
            if (fcmTokens.isEmpty()) {
                System.out.println("⚠️ Aucun token FCM trouvé pour les notifications SOS");
                return false;
            }

            // Préparer le message selon le type de notification
            Map<String, Object> payload = createSosNotificationPayload(sosSignal, typeNotification);
            payload.put("tokens", fcmTokens);

            // Envoyer via Lambda
            return sendNotificationToLambda(payload, "SOS");

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi notification SOS: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envoie une notification d'urgence critique aux autorités
     * @param sosSignal Le signalement SOS critique
     * @return true si l'envoi est réussi
     */
    public boolean sendCriticalSosAlert(SosSignal sosSignal) {
        try {
            // Pour les alertes critiques, on peut aussi envoyer des SMS
            List<String> emergencyNumbers = getEmergencyNumbers();
            
            // Envoyer notification push à tous les utilisateurs
            boolean pushSent = sendSosNotification(sosSignal, "critical_alert");
            
            // Envoyer SMS aux numéros d'urgence si disponibles
            boolean smsSent = false;
            if (!emergencyNumbers.isEmpty()) {
                smsSent = sendSmsToEmergencyNumbers(sosSignal, emergencyNumbers);
            }

            return pushSent || smsSent;

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi alerte critique SOS: " + e.getMessage());
            return false;
        }
    }

    /**
     * Envoie une notification de confirmation à l'utilisateur qui a fait le signalement
     * @param sosSignal Le signalement SOS
     * @param userId ID de l'utilisateur
     * @return true si l'envoi est réussi
     */
    public boolean sendSosConfirmationToUser(SosSignal sosSignal, int userId) {
        try {
            // Récupérer le token FCM de l'utilisateur spécifique
            List<String> userTokens = getFcmTokensForUser(userId);
            
            if (userTokens.isEmpty()) {
                System.out.println("⚠️ Aucun token FCM trouvé pour l'utilisateur " + userId);
                return false;
            }

            Map<String, Object> payload = createSosNotificationPayload(sosSignal, "confirmation");
            payload.put("tokens", userTokens);

            return sendNotificationToLambda(payload, "SOS_CONFIRMATION");

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi confirmation SOS utilisateur: " + e.getMessage());
            return false;
        }
    }

    // Méthodes privées pour les notifications SOS

    private Map<String, Object> createSosNotificationPayload(SosSignal sosSignal, String typeNotification) {
        Map<String, Object> payload = new HashMap<>();
        
        String title = "";
        String body = "";
        
        switch (typeNotification) {
            case "confirmation":
                title = "✅ Signalement SOS reçu";
                body = "Votre signalement d'urgence a été reçu et est en cours de traitement.";
                break;
            case "update":
                title = "📋 Mise à jour signalement SOS";
                body = "Votre signalement a été mis à jour: " + sosSignal.getStatut();
                break;
            case "resolution":
                title = "✅ Signalement SOS résolu";
                body = "Votre signalement d'urgence a été résolu.";
                break;
            case "critical_alert":
                title = "🚨 ALERTE URGENCE CRITIQUE";
                body = "Signalement critique: " + sosSignal.getTypeUrgence() + " à " + sosSignal.getLocalite();
                break;
            default:
                title = "🚨 Nouveau signalement SOS";
                body = "Signalement: " + sosSignal.getTypeUrgence() + " à " + sosSignal.getLocalite();
        }
        
        payload.put("title", title);
        payload.put("body", body);
        
        // Données additionnelles - TOUTES les valeurs doivent être des strings pour FCM
        Map<String, String> data = new HashMap<>();
        data.put("type", "sos_signal");
        data.put("signalId", String.valueOf(sosSignal.getId()));
        data.put("typeUrgence", sosSignal.getTypeUrgence() != null ? sosSignal.getTypeUrgence() : "");
        data.put("description", sosSignal.getDescription() != null ? sosSignal.getDescription() : "");
        data.put("localite", sosSignal.getLocalite() != null ? sosSignal.getLocalite() : "");
        data.put("latitude", sosSignal.getLatitude() != null ? sosSignal.getLatitude().toString() : "0");
        data.put("longitude", sosSignal.getLongitude() != null ? sosSignal.getLongitude().toString() : "0");
        data.put("statut", sosSignal.getStatut() != null ? sosSignal.getStatut() : "");
        data.put("priorite", sosSignal.getPriorite() != null ? sosSignal.getPriorite() : "");
        data.put("timestamp", sosSignal.getSignalTimestamp() != null ? sosSignal.getSignalTimestamp().toString() : "");
        data.put("anonyme", sosSignal.isAnonyme() ? "true" : "false");
        
        payload.put("data", data);
        
        return payload;
    }

    private boolean sendNotificationToLambda(Map<String, Object> payload, String notificationType) {
        try {
            System.out.println("🔍 Envoi vers Lambda - Type: " + notificationType);
            System.out.println("🔍 URL Lambda: " + lambdaUrl);
            System.out.println("🔍 Payload: " + payload);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_ACTUAL_API_KEY_HERE")) {
                headers.set("x-api-key", apiKey);
                System.out.println("🔑 API Key configurée");
            } else {
                System.err.println("⚠️ ATTENTION: API Key AWS non configurée ou invalide!");
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                lambdaUrl, HttpMethod.POST, entity, Map.class);

            System.out.println("🔍 Status Code Lambda: " + response.getStatusCode());
            System.out.println("🔍 Réponse Lambda complète: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                if (body != null) {
                    System.out.println("✅ Success count: " + body.get("successCount"));
                    System.out.println("❌ Error count: " + body.get("errorCount"));
                    System.out.println("📋 Message: " + body.get("message"));
                    
                    // Afficher les détails des résultats
                    if (body.containsKey("results")) {
                        System.out.println("📋 Détails des envois: " + body.get("results"));
                    }
                    
                    // Considérer comme succès si au moins un token a reçu la notification
                    Object successCount = body.get("successCount");
                    if (successCount != null && ((Number) successCount).intValue() > 0) {
                        System.out.println("✅ Notification " + notificationType + " envoyée avec succès à " + successCount + " appareil(s)");
                        return true;
                    } else {
                        System.err.println("❌ Aucun appareil n'a reçu la notification " + notificationType);
                        return false;
                    }
                }
                System.out.println("✅ Notification " + notificationType + " envoyée avec succès");
                return true;
            } else {
                System.err.println("❌ Erreur envoi notification " + notificationType + ": " + response.getStatusCode());
                System.err.println("❌ Body: " + response.getBody());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur communication Lambda pour " + notificationType + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private List<String> getFcmTokensForUser(int userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = supabaseUrl + "/rest/v1/fcm_tokens" +
                    "?user_id=eq." + userId +
                    "&is_active=eq.true" +
                    "&select=fcm_token";

            ResponseEntity<Map[]> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map[].class);

            List<String> tokens = new ArrayList<>();
            if (response.getBody() != null) {
                for (Map<String, Object> tokenData : response.getBody()) {
                    String token = (String) tokenData.get("fcm_token");
                    if (token != null && !token.isEmpty()) {
                        tokens.add(token);
                    }
                }
            }

            return tokens;
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération tokens FCM utilisateur: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<String> getEmergencyNumbers() {
        // TODO: Récupérer depuis la configuration ou la base de données
        // Pour l'instant, retourner une liste vide
        return new ArrayList<>();
    }

    private boolean sendSmsToEmergencyNumbers(SosSignal sosSignal, List<String> emergencyNumbers) {
        // TODO: Implémenter l'envoi SMS via Twilio pour les numéros d'urgence
        // Pour l'instant, retourner false
        return false;
    }
}


