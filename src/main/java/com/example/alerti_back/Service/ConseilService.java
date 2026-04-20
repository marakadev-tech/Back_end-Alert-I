package com.example.alerti_back.Service;
import com.example.alerti_back.Model.Conseil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ConseilService {

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void ajouterConseil(Conseil conseil) {

        String conseilUrl = supabaseUrl + "/rest/v1/conseil";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Prefer", "return=representation");

        Map<String, Object> conseilBody = new HashMap<>();
        conseilBody.put("type", conseil.getType());
        conseilBody.put("description", conseil.getDescription());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(conseilBody, headers);

        ResponseEntity<Conseil[]> response = restTemplate.exchange(
                conseilUrl,
                HttpMethod.POST,
                requestEntity,
                Conseil[].class
        );

        // Facultatif : traiter la réponse
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Conseil created = response.getBody()[0];
            System.out.println("Conseil créé : " + created.getId());
        } else {
            System.err.println("Échec de la création du conseil : " + response.getStatusCode());
        }

    }

    public List<Conseil> getConseilsParType(String type) {
        String url = supabaseUrl + "/rest/v1/conseil?type=eq." + type;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Conseil[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Conseil[].class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return List.of(response.getBody());
        } else {
            throw new RuntimeException("Erreur lors de la récupération des conseils : " + response.getStatusCode());
        }
    }



}
