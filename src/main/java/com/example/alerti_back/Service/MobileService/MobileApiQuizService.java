package com.example.alerti_back.Service.MobileService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


@Service
public class MobileApiQuizService {
    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> getAllQuizWithQuestions() {
        // Endpoint avec jointures imbriquées
        String url = supabaseUrl + "/rest/v1/quiz?select=*,questions(*,reponses(*))";

        org.springframework.http.HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Prefer", "return=representation");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                }
        );

        return response.getBody();
    }
    public List<Map<String, Object>> getQuestionsWithReponsesByQuizId(Long quizId) {
        // On va interroger directement la table questions
        String url = supabaseUrl + "/rest/v1/questions"
                + "?select=*,reponses(*)"
                + "&quiz_id=eq." + quizId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        return response.getBody();
    }




}