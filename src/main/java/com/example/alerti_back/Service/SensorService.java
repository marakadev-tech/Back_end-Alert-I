package com.example.alerti_back.Service;

import com.example.alerti_back.Model.HistoryEntry;
import com.example.alerti_back.Model.Sensors;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
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
import java.util.stream.Collectors;

@Service
public class SensorService {

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

}
