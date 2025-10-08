package com.example.alerti_back.Service;

import com.example.alerti_back.Model.Sensors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour récupérer les données météorologiques depuis OpenWeatherMap
 */
@Service
public class WeatherService {

    @Value("${openweathermap.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "https://api.openweathermap.org/data/3.0/onecall";

    /**
     * Récupère les données météo actuelles et prévisions pour une localité
     * Utilise One Call API 3.0 avec coordonnées approximatives
     * ⚠️ DEPRECATED: Utilisez getWeatherForSensor() avec les coordonnées GPS exactes
     */
    public Map<String, Object> getCurrentWeather(String localite) {
        try {
            // Coordonnées approximatives pour les localités du Mali (fallback)
            Map<String, Double> coordinates = getCoordinatesForLocalite(localite);
            
            if (coordinates == null) {
                return createErrorResponse("Localité non trouvée: " + localite);
            }

            return getWeatherByCoordinates(coordinates.get("lat"), coordinates.get("lon"), localite);

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération météo pour " + localite + ": " + e.getMessage());
            return createErrorResponse("Erreur: " + e.getMessage());
        }
    }

    /**
     * Récupère les données météo actuelles et prévisions pour des coordonnées GPS exactes
     * Utilise One Call API 3.0
     */
    public Map<String, Object> getWeatherByCoordinates(Double latitude, Double longitude, String localite) {
        try {
            if (latitude == null || longitude == null) {
                return createErrorResponse("Coordonnées GPS manquantes");
            }

            // One Call API 3.0 - inclut current, minutely, hourly, daily
            String url = baseUrl +
                    "?lat=" + latitude +
                    "&lon=" + longitude +
                    "&appid=" + apiKey +
                    "&units=metric" +
                    "&exclude=alerts"; // Exclure les alertes pour simplifier

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = processOneCallData(response.getBody());
                result.put("localite", localite);
                result.put("latitude", latitude);
                result.put("longitude", longitude);
                return result;
            } else {
                return createErrorResponse("Erreur API OpenWeatherMap: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération météo pour coordonnées " + latitude + "," + longitude + ": " + e.getMessage());
            return createErrorResponse("Erreur: " + e.getMessage());
        }
    }

    /**
     * Récupère les données météo pour un capteur spécifique
     * Utilise les coordonnées GPS exactes du capteur
     */
    public Map<String, Object> getWeatherForSensor(Sensors sensor) {
        try {
            if (sensor == null) {
                return createErrorResponse("Capteur non fourni");
            }

            if (sensor.getLatitude() == null || sensor.getLongitude() == null) {
                return createErrorResponse("Coordonnées GPS manquantes pour le capteur " + sensor.getId());
            }

            return getWeatherByCoordinates(
                sensor.getLatitude(), 
                sensor.getLongitude(), 
                sensor.getLocalite()
            );

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération météo pour capteur " + sensor.getId() + ": " + e.getMessage());
            return createErrorResponse("Erreur: " + e.getMessage());
        }
    }

    /**
     * Récupère les prévisions pluviométriques pour un capteur spécifique
     * Utilise les coordonnées GPS exactes du capteur
     */
    public Map<String, Object> getRainForecastForSensor(Sensors sensor, int days) {
        try {
            if (sensor == null) {
                return createErrorResponse("Capteur non fourni");
            }

            if (sensor.getLatitude() == null || sensor.getLongitude() == null) {
                return createErrorResponse("Coordonnées GPS manquantes pour le capteur " + sensor.getId());
            }

            // Récupérer les données météo avec les coordonnées exactes
            Map<String, Object> weatherData = getWeatherByCoordinates(
                sensor.getLatitude(), 
                sensor.getLongitude(), 
                sensor.getLocalite()
            );
            
            if (!(Boolean) weatherData.get("success")) {
                return weatherData;
            }
            
            // Extraire les données de prévision
            Map<String, Object> forecast = new HashMap<>();
            forecast.put("success", true);
            forecast.put("sensor_id", sensor.getId());
            forecast.put("localite", sensor.getLocalite());
            forecast.put("days_requested", days);
            
            // Données déjà calculées dans processOneCallData
            forecast.put("pluviometrie_24h", weatherData.get("pluviometrie_24h"));
            forecast.put("pluviometrie_7days", weatherData.get("pluviometrie_7days"));
            forecast.put("rain_hours_24h", weatherData.get("rain_hours_24h"));
            forecast.put("rain_days_7days", weatherData.get("rain_days_7days"));
            
            // Données actuelles
            forecast.put("current_rain_1h", weatherData.get("pluviometrie_1h"));
            forecast.put("temperature", weatherData.get("temperature"));
            forecast.put("humidity", weatherData.get("humidity"));
            forecast.put("description", weatherData.get("description"));
            
            return forecast;

        } catch (Exception e) {
            System.err.println("❌ Erreur prévisions pluie pour capteur " + sensor.getId() + ": " + e.getMessage());
            return createErrorResponse("Erreur: " + e.getMessage());
        }
    }

    /**
     * Récupère les prévisions pluviométriques pour les prochains jours
     * Utilise One Call API 3.0 (données déjà incluses dans getCurrentWeather)
     * ⚠️ DEPRECATED: Utilisez getRainForecastForSensor() avec les coordonnées GPS exactes
     */
    public Map<String, Object> getRainForecast(String localite, int days) {
        try {
            // One Call API 3.0 fournit déjà les prévisions, on réutilise getCurrentWeather
            Map<String, Object> weatherData = getCurrentWeather(localite);
            
            if (!(Boolean) weatherData.get("success")) {
                return weatherData;
            }
            
            // Extraire les données de prévision
            Map<String, Object> forecast = new HashMap<>();
            forecast.put("success", true);
            forecast.put("localite", localite);
            forecast.put("days_requested", days);
            
            // Données déjà calculées dans processOneCallData
            forecast.put("pluviometrie_24h", weatherData.get("pluviometrie_24h"));
            forecast.put("pluviometrie_7days", weatherData.get("pluviometrie_7days"));
            forecast.put("rain_hours_24h", weatherData.get("rain_hours_24h"));
            forecast.put("rain_days_7days", weatherData.get("rain_days_7days"));
            
            // Données actuelles
            forecast.put("current_rain_1h", weatherData.get("pluviometrie_1h"));
            forecast.put("temperature", weatherData.get("temperature"));
            forecast.put("humidity", weatherData.get("humidity"));
            forecast.put("description", weatherData.get("description"));
            
            return forecast;

        } catch (Exception e) {
            System.err.println("❌ Erreur prévisions pluie pour " + localite + ": " + e.getMessage());
            return createErrorResponse("Erreur: " + e.getMessage());
        }
    }

    /**
     * Traite les données One Call API 3.0
     */
    private Map<String, Object> processOneCallData(Map<String, Object> oneCallData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("🔍 Traitement des données One Call API...");
            System.out.println("📋 Clés disponibles: " + oneCallData.keySet());
            
            // Données actuelles
            Map<String, Object> current = (Map<String, Object>) oneCallData.get("current");
            java.util.List<Map<String, Object>> daily = (java.util.List<Map<String, Object>>) oneCallData.get("daily");
            java.util.List<Map<String, Object>> hourly = (java.util.List<Map<String, Object>>) oneCallData.get("hourly");
            
            result.put("success", true);
            
            System.out.println("📊 Données trouvées - Current: " + (current != null) + 
                             ", Daily: " + (daily != null ? daily.size() : 0) + 
                             ", Hourly: " + (hourly != null ? hourly.size() : 0));
            
            // Données actuelles
            if (current != null) {
                result.put("temperature", current.get("temp"));
                result.put("humidity", current.get("humidity"));
                result.put("pressure", current.get("pressure"));
                result.put("uvi", current.get("uvi")); // Index UV
                result.put("visibility", current.get("visibility"));
                
                // Pluviométrie actuelle (mm/h)
                Object rainObj = current.get("rain");
                if (rainObj instanceof Map) {
                    Map<String, Object> rain = (Map<String, Object>) rainObj;
                    if (rain.get("1h") != null) {
                        result.put("pluviometrie_1h", rain.get("1h"));
                    } else {
                        result.put("pluviometrie_1h", 0.0);
                    }
                } else {
                    result.put("pluviometrie_1h", 0.0);
                }
                
                // Description météo actuelle
                if (current.get("weather") != null) {
                    java.util.List<Map<String, Object>> weather = (java.util.List<Map<String, Object>>) current.get("weather");
                    if (!weather.isEmpty()) {
                        result.put("description", weather.get(0).get("description"));
                        result.put("main", weather.get(0).get("main"));
                        result.put("icon", weather.get(0).get("icon"));
                    }
                }
            }
            
            // Pluviométrie des dernières 24h (somme des données hourly)
            if (hourly != null && !hourly.isEmpty()) {
                double totalRain24h = 0.0;
                int rainHours = 0;
                
                // Prendre les 24 premières heures
                for (int i = 0; i < Math.min(24, hourly.size()); i++) {
                    Map<String, Object> hour = hourly.get(i);
                    Object rainObj = hour.get("rain");
                    if (rainObj instanceof Map) {
                        Map<String, Object> rain = (Map<String, Object>) rainObj;
                        if (rain.get("1h") != null) {
                            totalRain24h += ((Number) rain.get("1h")).doubleValue();
                            rainHours++;
                        }
                    }
                }
                
                result.put("pluviometrie_24h", totalRain24h);
                result.put("rain_hours_24h", rainHours);
            }
            
            // Prévisions quotidiennes (pluie)
            if (daily != null && !daily.isEmpty()) {
                double totalRain7days = 0.0;
                int rainDays = 0;
                
                for (Map<String, Object> day : daily) {
                    Object rainObj = day.get("rain");
                    if (rainObj instanceof Map) {
                        Map<String, Object> rain = (Map<String, Object>) rainObj;
                        if (rain.get("1h") != null) {
                            totalRain7days += ((Number) rain.get("1h")).doubleValue();
                            rainDays++;
                        }
                    }
                }
                
                result.put("pluviometrie_7days", totalRain7days);
                result.put("rain_days_7days", rainDays);
            }
            
            result.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            System.err.println("❌ Erreur traitement données One Call: " + e.getMessage());
            return createErrorResponse("Erreur traitement données: " + e.getMessage());
        }
        
        return result;
    }


    /**
     * Coordonnées approximatives pour les localités du Mali
     */
    private Map<String, Double> getCoordinatesForLocalite(String localite) {
        Map<String, Double> coordinates = new HashMap<>();
        
        switch (localite.toLowerCase()) {
            case "bamako":
                coordinates.put("lat", 12.65);
                coordinates.put("lon", -8.0);
                break;
            case "sebenikoro":
                coordinates.put("lat", 12.65);
                coordinates.put("lon", -8.0);
                break;
            case "dakar":
                coordinates.put("lat", 14.6928);
                coordinates.put("lon", -17.4467);
                break;
            case "thiès":
                coordinates.put("lat", 14.8);
                coordinates.put("lon", -16.9);
                break;
            case "saint-louis":
                coordinates.put("lat", 16.0333);
                coordinates.put("lon", -16.5);
                break;
            default:
                // Coordonnées par défaut (Bamako)
                coordinates.put("lat", 12.65);
                coordinates.put("lon", -8.0);
                break;
        }
        
        return coordinates;
    }

    /**
     * Crée une réponse d'erreur
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return error;
    }

    /**
     * Vérifie la santé de l'API OpenWeatherMap
     */
    public boolean isHealthy() {
        try {
            // Test simple avec une localité connue
            Map<String, Object> testData = getCurrentWeather("bamako");
            return (Boolean) testData.get("success");
        } catch (Exception e) {
            System.err.println("❌ API OpenWeatherMap non accessible: " + e.getMessage());
            return false;
        }
    }
}
