package com.example.alerti_back.Service;

import com.example.alerti_back.Model.HistoryEntry;
import com.example.alerti_back.Model.RainForecast;
import com.example.alerti_back.Model.Sensors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;



@Service
public class SensorService {

    @Autowired
    private WeatherSchedulerService weatherSchedulerService;

    // Injecte l'URL Supabase depuis application.properties
    @Value("${supabase.url}")
    private String supabaseUrl;

    // Injecte la clé d'API Supabase
    @Value("${supabase.key}")
    private String supabaseKey;
    // Utilisé pour faire des requêtes HTTP
    private final RestTemplate restTemplate = new RestTemplate();




    // Récupère tous les capteurs depuis la table Supabase "sensors"
    public List<Sensors> getAllSensors() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = supabaseUrl + "/rest/v1/sensors?select=*";

        ResponseEntity<Sensors[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Sensors[].class
        );

        return Arrays.asList(response.getBody());
    }

    // Récupère l'historique d'un capteur donné via son ID
    public List<HistoryEntry> getHistoryBySensorId(String sensorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = supabaseUrl + "/rest/v1/history?sensor_id=eq." + sensorId + "&select=*";

        ResponseEntity<HistoryEntry[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                HistoryEntry[].class
        );

        return Arrays.asList(response.getBody());
    }
    public List<HistoryEntry> getHistoryBySensorIdAndDate(String sensorId, LocalDate date) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Supabase format de filtre par date : timestamp=gte.date_start&timestamp=lt.date_end
        String start = date.atStartOfDay().toString(); // ex: 2024-07-30T00:00:00
        String end = date.plusDays(1).atStartOfDay().toString(); // ex: 2024-07-31T00:00:00

        String url = supabaseUrl + "/rest/v1/history"
                + "?sensor_id=eq." + sensorId
                + "&timestamp=gte." + start
                + "&timestamp=lt." + end
                + "&select=*";

        ResponseEntity<HistoryEntry[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                HistoryEntry[].class
        );

        return Arrays.asList(response.getBody());
    }



    @Autowired
    private FirestoreService firestoreService;

    public Sensors getSensorWithHistory(String sensorId) throws Exception {
        return firestoreService.getSensorWithHistory(sensorId);
    }

    public Sensors AddDispositive(Sensors sensorPartial) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Prefer", "return=representation");
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        var payload = new java.util.HashMap<String, Object>();

        if (sensorPartial.getId() != null) payload.put("id", sensorPartial.getId());
        if (sensorPartial.getStatut() != null) payload.put("statut", sensorPartial.getStatut());
        if (sensorPartial.getLocalite() != null) payload.put("localite", sensorPartial.getLocalite());
        if (sensorPartial.getLatitude() != null) payload.put("latitude", sensorPartial.getLatitude());
        if (sensorPartial.getLongitude() != null) payload.put("longitude", sensorPartial.getLongitude());
        if (sensorPartial.getSeuilniveauEau() != null) payload.put("seuilniveauEau", sensorPartial.getSeuilniveauEau());

        // ✅ Sécurise le timestamp et updated_at
        Date now = new Date();
        Date timestamp = (sensorPartial.getTimestamp() != null) ? sensorPartial.getTimestamp() : now;

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        payload.put("timestamp", isoFormat.format(timestamp));
        payload.put("updated_at", isoFormat.format(now));

        // Envoi à Supabase
        HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Sensors[]> response = restTemplate.exchange(
                supabaseUrl + "/rest/v1/sensors",
                HttpMethod.POST,
                request,
                Sensors[].class
        );

        return response.getBody() != null && response.getBody().length > 0 ? response.getBody()[0] : null;
    }

    //Parametrage du capteur

    public Sensors updateSensorThreshold(String sensorId, Double nouveauSeuil) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Prefer", "return=representation");
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        // Payload contenant uniquement le champ à modifier
        Map<String, Object> payload = new HashMap<>();
        payload.put("seuilniveauEau", nouveauSeuil);
        payload.put("updated_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .format(new Date())); // mise à jour automatique

        // Requête PATCH vers Supabase
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        String url = supabaseUrl + "/rest/v1/sensors?id=eq." + sensorId;

        ResponseEntity<Sensors[]> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                request,
                Sensors[].class
        );

        return response.getBody() != null && response.getBody().length > 0 ? response.getBody()[0] : null;
    }

    // Ajoutez ces méthodes à votre SensorService existant



    /**
     * Récupère tous les capteurs avec leurs prévisions météo actuelles
     */
    public List<Map<String, Object>> getAllSensorsWithForecast() {
        List<Sensors> sensors = getAllSensors();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Sensors sensor : sensors) {
            Map<String, Object> sensorData = new HashMap<>();

            // Informations du capteur
            sensorData.put("id", sensor.getId());
            sensorData.put("statut", sensor.getStatut());
            sensorData.put("localite", sensor.getLocalite());
            sensorData.put("latitude", sensor.getLatitude());
            sensorData.put("longitude", sensor.getLongitude());
            sensorData.put("dernierDonneeCaptemperature", sensor.getDernierDonneeCaptemperature());
            sensorData.put("dernierDonneeCapniveauEau", sensor.getDernierDonneeCapniveauEau());
            sensorData.put("dernierDonneevitesseDuVent", sensor.getDernierDonneevitesseDuVent());
            sensorData.put("seuilniveauEau", sensor.getSeuilniveauEau());
            sensorData.put("timestamp", sensor.getTimestamp());
            sensorData.put("updatedAt", sensor.getUpdatedAt());

            // Récupérer les prévisions météo
            RainForecast forecast = weatherSchedulerService.getCurrentForecast(sensor.getId());
            if (forecast != null) {
                Map<String, Object> forecastData = new HashMap<>();
                forecastData.put("pluiePrevue", forecast.getPluiePrevue());
                forecastData.put("forecastFrom", forecast.getForecastFrom());
                forecastData.put("forecastTo", forecast.getForecastTo());
                forecastData.put("createdAt", forecast.getCreatedAt());
                sensorData.put("forecast", forecastData);
            } else {
                sensorData.put("forecast", null);
            }

            // Calculer l'état d'alerte basé sur le niveau d'eau et le seuil
            String alertStatus = calculateAlertStatus(sensor);
            sensorData.put("alertStatus", alertStatus);

            result.add(sensorData);
        }

        return result;
    }

    /**
     * Récupère un capteur spécifique avec toutes ses informations détaillées
     */
    public Map<String, Object> getSensorDetails(String sensorId) {
        List<Sensors> sensors = getAllSensors();
        Sensors sensor = sensors.stream()
                .filter(s -> s.getId().equals(sensorId))
                .findFirst()
                .orElse(null);

        if (sensor == null) {
            return null;
        }

        Map<String, Object> sensorData = new HashMap<>();

        // Informations du capteur
        sensorData.put("id", sensor.getId());
        sensorData.put("statut", sensor.getStatut());
        sensorData.put("localite", sensor.getLocalite());
        sensorData.put("latitude", sensor.getLatitude());
        sensorData.put("longitude", sensor.getLongitude());
        sensorData.put("dernierDonneeCaptemperature", sensor.getDernierDonneeCaptemperature());
        sensorData.put("dernierDonneeCapniveauEau", sensor.getDernierDonneeCapniveauEau());
        sensorData.put("dernierDonneevitesseDuVent", sensor.getDernierDonneevitesseDuVent());
        sensorData.put("seuilniveauEau", sensor.getSeuilniveauEau());
        sensorData.put("timestamp", sensor.getTimestamp());
        sensorData.put("updatedAt", sensor.getUpdatedAt());

        // Récupérer les prévisions météo
        weatherSchedulerService.updateForecastIfNeeded(sensorId);
        RainForecast forecast = weatherSchedulerService.getCurrentForecast(sensorId);
        if (forecast != null) {
            Map<String, Object> forecastData = new HashMap<>();
            forecastData.put("pluiePrevue", forecast.getPluiePrevue());
            forecastData.put("forecastFrom", forecast.getForecastFrom());
            forecastData.put("forecastTo", forecast.getForecastTo());
            forecastData.put("createdAt", forecast.getCreatedAt());
            sensorData.put("forecast", forecastData);
        } else {
            sensorData.put("forecast", null);
        }

        // Récupérer l'historique récent (dernières 24h)
        LocalDate today = LocalDate.now();
        List<HistoryEntry> recentHistory = getHistoryBySensorIdAndDate(sensorId, today);
        sensorData.put("recentHistory", recentHistory);

        // Calculer l'état d'alerte
        String alertStatus = calculateAlertStatus(sensor);
        sensorData.put("alertStatus", alertStatus);

        // Calculer des statistiques additionnelles
        Map<String, Object> stats = calculateSensorStats(sensor, recentHistory);
        sensorData.put("statistics", stats);

        return sensorData;
    }

    /**
     * Calcule l'état d'alerte d'un capteur
     */
    private String calculateAlertStatus(Sensors sensor) {
        if (sensor.getDernierDonneeCapniveauEau() == null || sensor.getSeuilniveauEau() == null) {
            return "INCONNU";
        }

        double niveauEau = sensor.getDernierDonneeCapniveauEau();
        double seuil = sensor.getSeuilniveauEau();

        if (niveauEau >= seuil * 0.9) {
            return "CRITIQUE";
        } else if (niveauEau >= seuil * 0.7) {
            return "ALERTE";
        } else if (niveauEau >= seuil * 0.5) {
            return "SURVEILLANCE";
        } else {
            return "NORMAL";
        }
    }

    /**
     * Calcule des statistiques pour un capteur
     */
    private Map<String, Object> calculateSensorStats(Sensors sensor, List<HistoryEntry> history) {
        Map<String, Object> stats = new HashMap<>();

        if (history != null && !history.isEmpty()) {
            double avgTemp = history.stream()
                    .filter(h -> h.getTemperature() != null)
                    .mapToDouble(HistoryEntry::getTemperature)
                    .average()
                    .orElse(0.0);

            double avgWaterLevel = history.stream()
                    .filter(h -> h.getNiveauEau() != null)
                    .mapToDouble(HistoryEntry::getNiveauEau)
                    .average()
                    .orElse(0.0);

            double avgWindSpeed = history.stream()
                    .filter(h -> h.getVitesseDuVent() != null)
                    .mapToDouble(HistoryEntry::getVitesseDuVent)
                    .average()
                    .orElse(0.0);

            stats.put("avgTemperature24h", Math.round(avgTemp * 100.0) / 100.0);
            stats.put("avgWaterLevel24h", Math.round(avgWaterLevel * 100.0) / 100.0);
            stats.put("avgWindSpeed24h", Math.round(avgWindSpeed * 100.0) / 100.0);
            stats.put("totalMeasurements24h", history.size());
        } else {
            stats.put("avgTemperature24h", null);
            stats.put("avgWaterLevel24h", null);
            stats.put("avgWindSpeed24h", null);
            stats.put("totalMeasurements24h", 0);
        }

        return stats;
    }

}
