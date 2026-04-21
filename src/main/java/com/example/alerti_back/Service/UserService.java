package com.example.alerti_back.Service;

import com.example.alerti_back.Model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class UserService {
    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final RestTemplate patchRestTemplate =
            new RestTemplate(new HttpComponentsClientHttpRequestFactory());

    /**
     * Sauvegarde un utilisateur dans Supabase (table 'users')
     */
    public boolean saveUser(User user) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("email", user.getEmail());
        payload.put("password", user.getPassword()); // ⚠️ à chiffrer si nécessaire
        payload.put("nom", user.getNom());
        payload.put("prenom", user.getPrenom());
        payload.put("role", user.getRole());
        payload.put("localite", user.getLocalite());
        payload.put("num_tel", user.getNum_tel());

        return postToSupabase("/rest/v1/users", payload);
    }

    /**
     * Récupère un utilisateur par email
     */
    public Optional<User> findByEmail(String email) {
        String endpoint = supabaseUrl + "/rest/v1/users?email=eq." + email;

        HttpHeaders headers = getHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<User[]> response = restTemplate.exchange(
                    endpoint, HttpMethod.GET, entity, User[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                return Optional.of(response.getBody()[0]);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur findByEmail Supabase : " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Récupère un utilisateur par son numéro de téléphone
     */

    public Optional<User> findByNumTel(String numTel) {
        String normalizedNumTel = normalizePhone(numTel);
        String endpoint = supabaseUrl + "/rest/v1/users?num_tel=eq." + normalizedNumTel;

        HttpHeaders headers = getHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<User[]> response = restTemplate.exchange(
                    endpoint, HttpMethod.GET, entity, User[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                return Optional.of(response.getBody()[0]);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur findByNumTel Supabase : " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Vérifie les identifiants
     */
    public boolean authenticate(String email, String password) {
        Optional<User> optionalUser = findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return passwordEquals(user.getPassword(), password);
        }
        return false;
    }
    public boolean mobileAuthenticate(String num_tel, String password) {
        Optional<User> optionalUser = findByNumTel(num_tel);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return passwordEquals(user.getPassword(), password);
        }
        return false;
    }

    /**
     * Met à jour le mot de passe d'un utilisateur via son numéro de téléphone.
     */
    public boolean updatePasswordByNumTel(String numTel, String newPassword) {
        String normalizedNumTel = normalizePhone(numTel);
        String endpoint = supabaseUrl + "/rest/v1/users?num_tel=eq." + normalizedNumTel;

        HttpHeaders headers = getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Prefer", "return=representation");

        Map<String, Object> payload = new HashMap<>();
        payload.put("password", newPassword);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<User[]> response = patchRestTemplate.exchange(
                    endpoint, HttpMethod.PATCH, request, User[].class);
            return response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().length > 0;
        } catch (Exception e) {
            System.err.println("❌ Erreur updatePasswordByNumTel Supabase : " + e.getMessage());
            return false;
        }
    }

    /** Strict comparison only (same string after trim). */
    private boolean passwordEquals(String storedPassword, String providedPassword) {
        if (storedPassword == null || providedPassword == null) {
            return false;
        }
        return storedPassword.trim().equals(providedPassword.trim());
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("\\D", "");
    }

    /**
     * Envoi POST vers Supabase
     */
    private boolean postToSupabase(String endpoint, Map<String, Object> data) {
        HttpHeaders headers = getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);

        try {
            restTemplate.exchange(supabaseUrl + endpoint, HttpMethod.POST, request, String.class);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Exception lors de l'envoi à Supabase : " + e.getMessage());
            return false;
        }
    }

    /**
     * Crée les headers nécessaires
     */
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}
