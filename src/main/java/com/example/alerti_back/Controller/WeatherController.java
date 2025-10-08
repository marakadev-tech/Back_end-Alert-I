package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.Sensors;
import com.example.alerti_back.Service.SupabaseService;
import com.example.alerti_back.Service.WeatherService;
import com.example.alerti_back.Service.NotificationService;
import com.example.alerti_back.Model.AlertLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur pour tester les données météorologiques
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private SupabaseService supabaseService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Récupère les données météo actuelles pour une localité
     * GET /api/weather/current/{localite}
     */
    @GetMapping("/current/{localite}")
    public ResponseEntity<?> getCurrentWeather(@PathVariable String localite) {
        try {
            Map<String, Object> weatherData = weatherService.getCurrentWeather(localite);
            
            if ((Boolean) weatherData.get("success")) {
                return ResponseEntity.ok(weatherData);
            } else {
                return ResponseEntity.badRequest().body(weatherData);
            }
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Récupère les prévisions pluviométriques pour un capteur spécifique
     * GET /api/weather/forecast-sensor/{sensorId}?days=3
     */
    @GetMapping("/forecast-sensor/{sensorId}")
    public ResponseEntity<?> getRainForecastForSensor(
            @PathVariable String sensorId,
            @RequestParam(defaultValue = "3") int days) {
        try {
            // Récupérer le capteur depuis Supabase
            Optional<Sensors> sensorOpt = supabaseService.findById(sensorId);
            if (sensorOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Sensors sensor = sensorOpt.get();
            Map<String, Object> forecastData = weatherService.getRainForecastForSensor(sensor, days);
            
            if ((Boolean) forecastData.get("success")) {
                return ResponseEntity.ok(forecastData);
            } else {
                return ResponseEntity.badRequest().body(forecastData);
            }
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Récupère les prévisions pluviométriques
     * GET /api/weather/forecast/{localite}?days=3
     * ⚠️ DEPRECATED: Utilisez /forecast-sensor/{sensorId} avec les coordonnées GPS exactes
     */
    @GetMapping("/forecast/{localite}")
    public ResponseEntity<?> getRainForecast(
            @PathVariable String localite,
            @RequestParam(defaultValue = "3") int days) {
        try {
            Map<String, Object> forecastData = weatherService.getRainForecast(localite, days);
            
            if ((Boolean) forecastData.get("success")) {
                return ResponseEntity.ok(forecastData);
            } else {
                return ResponseEntity.badRequest().body(forecastData);
            }
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Récupère les données météo pour un capteur spécifique
     * GET /api/weather/sensor/{sensorId}
     */
    @GetMapping("/sensor/{sensorId}")
    public ResponseEntity<?> getWeatherForSensor(@PathVariable String sensorId) {
        try {
            // Récupérer le capteur depuis Supabase
            Optional<Sensors> sensorOpt = supabaseService.findById(sensorId);
            if (sensorOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Sensors sensor = sensorOpt.get();
            Map<String, Object> weatherData = weatherService.getWeatherForSensor(sensor);
            
            if ((Boolean) weatherData.get("success")) {
                return ResponseEntity.ok(weatherData);
            } else {
                return ResponseEntity.badRequest().body(weatherData);
            }
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test complet : météo + calcul d'alerte pour un capteur
     * GET /api/weather/alert-test-sensor/{sensorId}?seuil=25.95
     */
    @GetMapping("/alert-test-sensor/{sensorId}")
    public ResponseEntity<?> testAlertWithSensorWeather(
            @PathVariable String sensorId,
            @RequestParam(defaultValue = "25.95") double seuil) {
        try {
            // Récupérer le capteur depuis Supabase
            Optional<Sensors> sensorOpt = supabaseService.findById(sensorId);
            if (sensorOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Sensors sensor = sensorOpt.get();
            
            // Récupérer les données météo avec les coordonnées exactes du capteur
            Map<String, Object> weatherData = weatherService.getWeatherForSensor(sensor);
            
            if (!(Boolean) weatherData.get("success")) {
                return ResponseEntity.badRequest().body(weatherData);
            }
            
            // Calculer le niveau d'alerte (utiliser pluviométrie 24h pour plus de précision)
            double pluviometrie = (Double) weatherData.get("pluviometrie_24h");
            if (pluviometrie == 0.0) {
                pluviometrie = (Double) weatherData.get("pluviometrie_1h");
            }
            double ratio = pluviometrie / seuil;
            
            String alertLevel;
            if (ratio >= 1.0) {
                alertLevel = "DANGER";
            } else if (ratio >= 0.85) {
                alertLevel = "DANGER";
            } else if (ratio >= 0.70) {
                alertLevel = "ATTENTION";
            } else {
                alertLevel = "NORMAL";
            }
            
            // Construire la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sensor_id", sensor.getId());
            response.put("localite", sensor.getLocalite());
            response.put("sensor_latitude", sensor.getLatitude());
            response.put("sensor_longitude", sensor.getLongitude());
            response.put("pluviometrie_24h", weatherData.get("pluviometrie_24h"));
            response.put("pluviometrie_1h", weatherData.get("pluviometrie_1h"));
            response.put("pluviometrie_7days", weatherData.get("pluviometrie_7days"));
            response.put("pluviometrie_utilisee", pluviometrie);
            response.put("seuil_configure", seuil);
            response.put("ratio", ratio);
            response.put("pourcentage", ratio * 100);
            response.put("alert_level", alertLevel);
            response.put("temperature", weatherData.get("temperature"));
            response.put("humidity", weatherData.get("humidity"));
            response.put("description", weatherData.get("description"));
            response.put("rain_hours_24h", weatherData.get("rain_hours_24h"));
            response.put("rain_days_7days", weatherData.get("rain_days_7days"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test complet : météo + calcul d'alerte
     * GET /api/weather/alert-test/{localite}?seuil=25.95
     */
    @GetMapping("/alert-test/{localite}")
    public ResponseEntity<?> testAlertWithWeather(
            @PathVariable String localite,
            @RequestParam(defaultValue = "25.95") double seuil) {
        try {
            // Récupérer les données météo
            Map<String, Object> weatherData = weatherService.getCurrentWeather(localite);
            
            if (!(Boolean) weatherData.get("success")) {
                return ResponseEntity.badRequest().body(weatherData);
            }
            
            // Calculer le niveau d'alerte (utiliser pluviométrie 24h pour plus de précision)
            double pluviometrie = (Double) weatherData.get("pluviometrie_24h");
            if (pluviometrie == 0.0) {
                pluviometrie = (Double) weatherData.get("pluviometrie_1h");
            }
            double ratio = pluviometrie / seuil;
            
            String alertLevel;
            if (ratio >= 1.0) {
                alertLevel = "DANGER";
            } else if (ratio >= 0.85) {
                alertLevel = "DANGER";
            } else if (ratio >= 0.70) {
                alertLevel = "ATTENTION";
            } else {
                alertLevel = "NORMAL";
            }
            
            // Construire la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("localite", localite);
            response.put("pluviometrie_24h", weatherData.get("pluviometrie_24h"));
            response.put("pluviometrie_1h", weatherData.get("pluviometrie_1h"));
            response.put("pluviometrie_7days", weatherData.get("pluviometrie_7days"));
            response.put("pluviometrie_utilisee", pluviometrie);
            response.put("seuil_configure", seuil);
            response.put("ratio", ratio);
            response.put("pourcentage", ratio * 100);
            response.put("alert_level", alertLevel);
            response.put("temperature", weatherData.get("temperature"));
            response.put("humidity", weatherData.get("humidity"));
            response.put("description", weatherData.get("description"));
            response.put("rain_hours_24h", weatherData.get("rain_hours_24h"));
            response.put("rain_days_7days", weatherData.get("rain_days_7days"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test simple pour débugger l'API OpenWeatherMap
     * GET /api/weather/debug/{localite}
     */
    @GetMapping("/debug/{localite}")
    public ResponseEntity<?> debugWeatherAPI(@PathVariable String localite) {
        try {
            Map<String, Object> weatherData = weatherService.getCurrentWeather(localite);
            return ResponseEntity.ok(weatherData);
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage(),
                "stackTrace", e.getStackTrace()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test notification à TOUS les utilisateurs
     * GET /api/weather/test-all-users/{sensorId}
     */
    @GetMapping("/test-all-users/{sensorId}")
    public ResponseEntity<?> testNotificationAllUsers(@PathVariable String sensorId) {
        try {
            // Récupérer le capteur depuis Supabase
            Optional<Sensors> sensorOpt = supabaseService.findById(sensorId);
            if (sensorOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Sensors sensor = sensorOpt.get();
            
            // Forcer un état NORMAL avec pluviométrie faible
            sensor.setPluviometrieJour(5.0); // 5mm/jour (très faible)
            sensor.setSeuilPluviometrie(25.95); // Seuil normal
            
            // Appeler directement NotificationService pour envoyer à TOUS
            System.out.println("🧪 TEST - Envoi notification à TOUS les utilisateurs pour capteur " + sensorId);
            
            // Utiliser le service injecté (avec configuration Spring)
            boolean sent = notificationService.sendAlert(sensor, 
                AlertLevel.NORMAL, 
                new ArrayList<>());
            
            // Construire la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sensor_id", sensor.getId());
            response.put("localite", sensor.getLocalite());
            response.put("notification_sent", sent);
            response.put("message", "Test notification à TOUS les utilisateurs déclenché pour " + sensorId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test simple de notification push
     * GET /api/weather/test-push-simple
     */
    @GetMapping("/test-push-simple")
    public ResponseEntity<?> testPushSimple() {
        try {
            System.out.println("🧪 TEST SIMPLE - Envoi notification push");
            System.out.println("🌐 Domaine ngrok: https://linnea-undefaulting-obdulia.ngrok-free.dev");
            
            // Créer un capteur fictif pour le test
            Sensors testSensor = new Sensors();
            testSensor.setId("TEST_SENSOR");
            testSensor.setLocalite("Test Localité");
            testSensor.setPluviometrieJour(10.0);
            testSensor.setSeuilPluviometrie(25.0);
            
            // Utiliser le service injecté (avec configuration Spring)
            boolean sent = notificationService.sendAlert(testSensor, 
                AlertLevel.NORMAL, 
                new ArrayList<>());
            
            // Construire la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notification_sent", sent);
            response.put("message", "Test simple de notification push");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            response.put("ngrok_domain", "https://linnea-undefaulting-obdulia.ngrok-free.dev");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test d'alerte avec données météo réelles pour un capteur
     * GET /api/weather/test-real-alert/{sensorId}
     */
    @GetMapping("/test-real-alert/{sensorId}")
    public ResponseEntity<?> testRealWeatherAlert(@PathVariable String sensorId) {
        try {
            // Récupérer le capteur depuis Supabase
            Optional<Sensors> sensorOpt = supabaseService.findById(sensorId);
            if (sensorOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Sensors sensor = sensorOpt.get();
            
            // Vérifier que le capteur a des coordonnées GPS
            if (sensor.getLatitude() == null || sensor.getLongitude() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Capteur sans coordonnées GPS"
                ));
            }
            
            // Récupérer les données météo réelles
            Map<String, Object> weatherData = weatherService.getWeatherForSensor(sensor);
            
            if (!(Boolean) weatherData.get("success")) {
                return ResponseEntity.badRequest().body(weatherData);
            }
            
            // Construire la réponse avec toutes les informations
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sensor_id", sensor.getId());
            response.put("localite", sensor.getLocalite());
            response.put("sensor_coordinates", Map.of(
                "latitude", sensor.getLatitude(),
                "longitude", sensor.getLongitude()
            ));
            response.put("weather_data", weatherData);
            response.put("message", "Données météo réelles récupérées avec succès pour le capteur " + sensorId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
