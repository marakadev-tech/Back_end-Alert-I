package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.AlertLevel;
import com.example.alerti_back.Model.Sensors;
import com.example.alerti_back.Service.NotificationService;
import com.example.alerti_back.Service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Contrôleur pour tester les notifications push et SMS
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationTestController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SupabaseService supabaseService;

    /**
     * Test d'envoi de notification push à tous les tokens FCM actifs
     * GET /api/notifications/test-push
     */
    @GetMapping("/test-push")
    public ResponseEntity<?> testPushNotification() {
        try {
            // Récupérer tous les tokens FCM actifs
            List<String> tokens = notificationService.getAllActiveFcmTokens();
            
            if (tokens.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Aucun token FCM actif trouvé");
                response.put("tokensCount", 0);
                return ResponseEntity.ok(response);
            }

            // Créer un capteur de test
            Sensors testSensor = new Sensors();
            testSensor.setId("test_sensor_001");
            testSensor.setLocalite("Mali");
            testSensor.setDernierDonneeCapniveauEau(85.0);
            testSensor.setSeuilniveauEau(100.0);
            testSensor.setLatitude(14.6928);
            testSensor.setLongitude(-17.4467);
            testSensor.setTimestamp(new Date());

            // Envoyer notification de test
            boolean sent = notificationService.sendAlert(testSensor, AlertLevel.DANGER, new ArrayList<>());

            Map<String, Object> response = new HashMap<>();
            response.put("success", sent);
            response.put("message", sent ? "Notification push envoyée avec succès" : "Échec de l'envoi");
            response.put("tokensCount", tokens.size());
            response.put("sensorId", testSensor.getId());
            response.put("alertLevel", AlertLevel.DANGER.name());
            response.put("localite", testSensor.getLocalite());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test d'envoi de notification push pour une localité spécifique
     * GET /api/notifications/test-push/{localite}
     */
    @GetMapping("/test-push/{localite}")
    public ResponseEntity<?> testPushNotificationForLocalite(@PathVariable String localite) {
        try {
            // Récupérer les tokens FCM pour cette localité
            List<String> tokens = notificationService.getFcmTokensForLocalite(localite);
            
            if (tokens.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Aucun token FCM trouvé pour la localité: " + localite);
                response.put("tokensCount", 0);
                response.put("localite", localite);
                return ResponseEntity.ok(response);
            }

            // Créer un capteur de test
            Sensors testSensor = new Sensors();
            testSensor.setId("test_sensor_" + localite.toLowerCase());
            testSensor.setLocalite(localite);
            testSensor.setDernierDonneeCapniveauEau(75.0);
            testSensor.setSeuilniveauEau(100.0);
            testSensor.setLatitude(14.6928);
            testSensor.setLongitude(-17.4467);
            testSensor.setTimestamp(new Date());

            // Envoyer notification de test
            boolean sent = notificationService.sendAlert(testSensor, AlertLevel.ATTENTION, new ArrayList<>());

            Map<String, Object> response = new HashMap<>();
            response.put("success", sent);
            response.put("message", sent ? "Notification push envoyée avec succès" : "Échec de l'envoi");
            response.put("tokensCount", tokens.size());
            response.put("sensorId", testSensor.getId());
            response.put("alertLevel", AlertLevel.ATTENTION.name());
            response.put("localite", localite);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test d'envoi de notification SMS
     * POST /api/notifications/test-sms
     */
    @PostMapping("/test-sms")
    public ResponseEntity<?> testSmsNotification(@RequestBody Map<String, Object> request) {
        try {
            String phoneNumber = (String) request.get("phoneNumber");
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Numéro de téléphone requis");
                return ResponseEntity.badRequest().body(error);
            }

            // Créer un capteur de test
            Sensors testSensor = new Sensors();
            testSensor.setId("test_sensor_sms");
            testSensor.setLocalite("Dakar");
            testSensor.setDernierDonneeCapniveauEau(90.0);
            testSensor.setSeuilniveauEau(100.0);
            testSensor.setTimestamp(new Date());

            // Envoyer notification SMS
            List<String> recipients = Arrays.asList(phoneNumber);
            boolean sent = notificationService.sendAlert(testSensor, AlertLevel.DANGER, recipients);

            Map<String, Object> response = new HashMap<>();
            response.put("success", sent);
            response.put("message", sent ? "SMS envoyé avec succès" : "Échec de l'envoi SMS");
            response.put("phoneNumber", phoneNumber);
            response.put("sensorId", testSensor.getId());
            response.put("alertLevel", AlertLevel.DANGER.name());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtenir les statistiques des tokens FCM
     * GET /api/notifications/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getNotificationStats() {
        try {
            List<String> allTokens = notificationService.getAllActiveFcmTokens();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalActiveTokens", allTokens.size());
            stats.put("tokens", allTokens.stream().map(token -> 
                token.substring(0, Math.min(20, token.length())) + "..."
            ).toList());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test complet avec un capteur existant
     * POST /api/notifications/test-sensor/{sensorId}
     */
    @PostMapping("/test-sensor/{sensorId}")
    public ResponseEntity<?> testNotificationWithSensor(@PathVariable String sensorId) {
        try {
            // Récupérer le capteur depuis Supabase
            Optional<Sensors> sensorOpt = supabaseService.findById(sensorId);
            
            if (sensorOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Capteur non trouvé: " + sensorId);
                return ResponseEntity.notFound().build();
            }

            Sensors sensor = sensorOpt.get();
            
            // Récupérer les tokens pour cette localité
            List<String> tokens = notificationService.getFcmTokensForLocalite(sensor.getLocalite());
            
            // Envoyer notification
            boolean sent = notificationService.sendAlert(sensor, AlertLevel.DANGER, new ArrayList<>());

            Map<String, Object> response = new HashMap<>();
            response.put("success", sent);
            response.put("message", sent ? "Notification envoyée avec succès" : "Échec de l'envoi");
            response.put("sensorId", sensorId);
            response.put("localite", sensor.getLocalite());
            response.put("tokensCount", tokens.size());
            response.put("niveauEau", sensor.getDernierDonneeCapniveauEau());
            response.put("seuilEau", sensor.getSeuilniveauEau());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}

