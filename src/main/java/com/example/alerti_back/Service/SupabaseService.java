package com.example.alerti_back.Service;

import com.example.alerti_back.Model.HistoryEntry;
import com.example.alerti_back.Model.Sensors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Service
public class SupabaseService {

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private final SimpleDateFormat isoFormat;

    public SupabaseService() {
        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void saveSensorData(Sensors sensor) {
        // Initialiser les valeurs par défaut pour éviter les erreurs null
        initializeSensorDefaults(sensor);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", sensor.getId());
        payload.put("statut", sensor.getStatut());
        payload.put("localite", sensor.getLocalite());
        payload.put("latitude", sensor.getLatitude());
        payload.put("longitude", sensor.getLongitude());
        payload.put("dernierDonneeCaptemperature", sensor.getDernierDonneeCaptemperature());
        payload.put("dernierDonneeCapniveauEau", sensor.getDernierDonneeCapniveauEau());
        payload.put("dernierDonneevitesseDuVent", sensor.getDernierDonneevitesseDuVent());
        payload.put("seuilniveauEau", sensor.getSeuilniveauEau());
        
        // Champs pluviométrie
        payload.put("pluviometrieJour", sensor.getPluviometrieJour());
        payload.put("seuilPluviometrie", sensor.getSeuilPluviometrie());
        
        // Dates obligatoires
        if (sensor.getTimestamp() != null) {
            payload.put("timestamp", isoFormat.format(sensor.getTimestamp()));
        } else {
            payload.put("timestamp", isoFormat.format(new Date()));
        }
        
        if (sensor.getUpdatedAt() != null) {
            payload.put("updated_at", isoFormat.format(sensor.getUpdatedAt()));
        } else {
            payload.put("updated_at", isoFormat.format(new Date()));
        }

        // Envoi vers la table sensors (en mode UPSERT)
        postToSupabase("/rest/v1/sensors", payload);

        // Envoi de chaque entrée historique (si elle existe)
        if (sensor.getHistory() != null && !sensor.getHistory().isEmpty()) {
            for (HistoryEntry entry : sensor.getHistory()) {
                Map<String, Object> history = new HashMap<>();
                history.put("id", UUID.randomUUID().toString());
                history.put("sensor_id", sensor.getId());
                history.put("timestamp", isoFormat.format(entry.getTimestamp()));
                history.put("temperature", entry.getTemperature());
                history.put("humidity", entry.getHumidity());
                history.put("niveau_eau", entry.getNiveauEau());
                history.put("vitesse_du_vent", entry.getVitesseDuVent());

                postToSupabase("/rest/v1/history", history);
            }
        }
    }

    private void postToSupabase(String endpoint, Map<String, Object> data) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Prefer", "resolution=merge-duplicates"); // ✅ For UPSERT behavior

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);

        try {
            restTemplate.exchange(supabaseUrl + endpoint, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            System.err.println("❌ Exception lors de l'envoi à Supabase : " + e.getMessage());
        }
    }

    public Optional<Sensors> findById(String id) {
        String endpoint = supabaseUrl + "/rest/v1/sensors?id=eq." + id;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Sensors[]> response = restTemplate.exchange(
                    endpoint, HttpMethod.GET, entity, Sensors[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                return Optional.of(response.getBody()[0]);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur findById Supabase : " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Récupère tous les capteurs actifs
     */
    public List<Sensors> getAllActiveSensors() {
        try {
            String url = supabaseUrl + "/rest/v1/sensors?statut=eq.active&select=*";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Sensors[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Sensors[].class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération capteurs actifs: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupère tous les capteurs avec coordonnées GPS
     */
    public List<Sensors> getSensorsWithCoordinates() {
        try {
            String url = supabaseUrl + "/rest/v1/sensors?and=(statut.eq.active,latitude.not.is.null,longitude.not.is.null)&select=*";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Sensors[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Sensors[].class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération capteurs avec GPS: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Nettoie les anciennes données d'historique
     */
    public int cleanupOldHistoryData(int daysToKeep) {
        try {
            // Supprimer les entrées d'historique de plus de X jours
            String url = supabaseUrl + "/rest/v1/sensors?select=id,history";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Sensors[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Sensors[].class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                int deletedCount = 0;
                long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60L * 60L * 1000L);
                
                for (Sensors sensor : response.getBody()) {
                    if (sensor.getHistory() != null && !sensor.getHistory().isEmpty()) {
                        List<HistoryEntry> filteredHistory = sensor.getHistory().stream()
                            .filter(entry -> entry.getTimestamp() != null && entry.getTimestamp().getTime() > cutoffTime)
                            .collect(java.util.stream.Collectors.toList());
                        
                        if (filteredHistory.size() != sensor.getHistory().size()) {
                            sensor.setHistory(filteredHistory);
                            saveSensorData(sensor);
                            deletedCount += (sensor.getHistory().size() - filteredHistory.size());
                        }
                    }
                }
                
                return deletedCount;
            }
            
            return 0;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur nettoyage historique: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Initialise un capteur avec des valeurs par défaut pour éviter les erreurs null
     */
    public void initializeSensorDefaults(Sensors sensor) {
        if (sensor == null) return;
        
        // Initialiser les dates si null
        if (sensor.getTimestamp() == null) {
            sensor.setTimestamp(new Date());
        }
        if (sensor.getUpdatedAt() == null) {
            sensor.setUpdatedAt(new Date());
        }
        
        // Initialiser l'historique si null
        if (sensor.getHistory() == null) {
            sensor.setHistory(new ArrayList<>());
        }
        
        // Initialiser le statut si null
        if (sensor.getStatut() == null) {
            sensor.setStatut("active");
        }
    }

    /**
     * Vérifie la santé de la connexion à la base de données
     */
    public boolean isHealthy() {
        try {
            String url = supabaseUrl + "/rest/v1/sensors?select=count&limit=1";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            System.err.println("❌ Base de données non accessible: " + e.getMessage());
            return false;
        }
    }

}
