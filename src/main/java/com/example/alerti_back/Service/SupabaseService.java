package com.example.alerti_back.Service;

import com.example.alerti_back.Model.HistoryEntry;
import com.example.alerti_back.Model.Sensors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SupabaseService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private final SimpleDateFormat isoFormat;

    public SupabaseService() {
        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void saveSensorData(Sensors sensor) {
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
        payload.put("timestamp", isoFormat.format(sensor.getTimestamp()));
        payload.put("updated_at", isoFormat.format(sensor.getUpdatedAt()));

        // Envoi vers la table sensors (en mode UPSERT)
        postToSupabase("/rest/v1/sensors", payload);

        // Envoi de chaque entrée historique
        for (HistoryEntry entry : sensor.getHistory()) {
            Map<String, Object> history = new HashMap<>();

            history.put("sensor_id", sensor.getId());
            history.put("timestamp", isoFormat.format(entry.getTimestamp()));
            history.put("temperature", entry.getTemperature());
            history.put("humidity", entry.getHumidity());
            history.put("niveau_eau", entry.getNiveauEau());
            history.put("vitesse_du_vent", entry.getVitesseDuVent());

            postToSupabase("/rest/v1/history", history);
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

    // Ajoutez cette méthode dans votre SupabaseService existant

    /**
     * Récupère tous les capteurs depuis Supabase
     */
    public List<Sensors> getAllSensors() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = supabaseUrl + "/rest/v1/sensors?select=*";

            ResponseEntity<List<Sensors>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Sensors>>() {}
            );

            List<Sensors> sensors = response.getBody();
            if (sensors != null) {
                System.out.println("📡 Récupération de " + sensors.size() + " capteur(s) depuis Supabase");
                return sensors;
            } else {
                System.out.println("⚠️ Aucun capteur trouvé dans Supabase");
                return new ArrayList<>();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des capteurs : " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


}
