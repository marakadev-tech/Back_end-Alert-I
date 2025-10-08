package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.AlertLevel;
import com.example.alerti_back.Model.Sensors;
import com.example.alerti_back.Service.AlertService;
import com.example.alerti_back.Service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Contrôleur pour tester et gérer le système d'alertes
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private SupabaseService supabaseService;

    /**
     * Endpoint de test pour déclencher manuellement une alerte
     * POST /api/alerts/test/{sensorId}
     */
    @PostMapping("/test/{sensorId}")
    public ResponseEntity<?> testAlert(@PathVariable String sensorId) {
        try {
            Optional<Sensors> sensorOpt = supabaseService.findById(sensorId);
            
            if (sensorOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Sensors sensor = sensorOpt.get();
            alertService.checkAndNotify(sensor, new ArrayList<>());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vérification d'alerte effectuée pour le capteur " + sensorId);
            response.put("niveauEau", sensor.getDernierDonneeCapniveauEau());
            response.put("seuilEau", sensor.getSeuilniveauEau());
            
            if (sensor.getDernierDonneeCapniveauEau() != null && sensor.getSeuilniveauEau() != null) {
                AlertLevel level = AlertLevel.determinerNiveau(
                    sensor.getDernierDonneeCapniveauEau(), 
                    sensor.getSeuilniveauEau()
                );
                response.put("alertLevel", level.name());
                response.put("alertDescription", level.getDescription());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtenir le statut d'alerte actuel d'un capteur
     * GET /api/alerts/status/{sensorId}
     */
    @GetMapping("/status/{sensorId}")
    public ResponseEntity<?> getAlertStatus(@PathVariable String sensorId) {
        try {
            Optional<Sensors> sensorOpt = supabaseService.findById(sensorId);
            
            if (sensorOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Sensors sensor = sensorOpt.get();
            AlertLevel currentLevel = AlertLevel.determinerNiveau(
                sensor.getDernierDonneeCapniveauEau(), 
                sensor.getSeuilniveauEau()
            );
            
            AlertLevel cachedLevel = alertService.getCurrentAlertLevel(sensor);

            Map<String, Object> response = new HashMap<>();
            response.put("sensorId", sensorId);
            response.put("localite", sensor.getLocalite());
            response.put("niveauEau", sensor.getDernierDonneeCapniveauEau());
            response.put("seuilEau", sensor.getSeuilniveauEau());
            response.put("currentAlertLevel", currentLevel.name());
            response.put("currentAlertDescription", currentLevel.getDescription());
            response.put("cachedAlertLevel", cachedLevel.name());
            response.put("timestamp", sensor.getTimestamp());
            
            if (sensor.getDernierDonneeCapniveauEau() != null && sensor.getSeuilniveauEau() != null && sensor.getSeuilniveauEau() != 0) {
                double pourcentage = (sensor.getDernierDonneeCapniveauEau() / sensor.getSeuilniveauEau()) * 100;
                response.put("pourcentageSeuil", pourcentage);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Réinitialiser le cache d'alerte pour un capteur
     * POST /api/alerts/reset/{sensorId}
     */
    @PostMapping("/reset/{sensorId}")
    public ResponseEntity<?> resetAlertCache(@PathVariable String sensorId) {
        try {
            alertService.resetAlertCache(sensorId);
            
            Map<String, String> response = new HashMap<>();
            response.put("success", "true");
            response.put("message", "Cache d'alerte réinitialisé pour le capteur " + sensorId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Calculer le niveau d'alerte pour des valeurs données (outil de test)
     * GET /api/alerts/calculate?niveauEau=75&seuilEau=100
     */
    @GetMapping("/calculate")
    public ResponseEntity<?> calculateAlertLevel(
            @RequestParam Double niveauEau,
            @RequestParam Double seuilEau) {
        
        AlertLevel level = AlertLevel.determinerNiveau(niveauEau, seuilEau);
        
        Map<String, Object> response = new HashMap<>();
        response.put("niveauEau", niveauEau);
        response.put("seuilEau", seuilEau);
        response.put("alertLevel", level.name());
        response.put("alertLabel", level.getLabel());
        response.put("alertDescription", level.getDescription());
        response.put("necessiteNotification", level.necessiteNotification());
        
        if (seuilEau != null && seuilEau != 0) {
            double pourcentage = (niveauEau / seuilEau) * 100;
            response.put("pourcentageSeuil", pourcentage);
        }

        return ResponseEntity.ok(response);
    }
}


