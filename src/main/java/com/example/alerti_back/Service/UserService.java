package com.example.alerti_back.Service;

import com.example.alerti_back.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class UserService {


    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sauvegarde un utilisateur dans Supabase (table 'users')
     */
    public boolean saveUser(User user) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("email", user.getEmail());
        payload.put("password", user.getPassword());  // encoder le mot de passe
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
        logger.debug("Requête GET vers Supabase : {}", endpoint);

        HttpHeaders headers = getHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<User[]> response = restTemplate.exchange(
                    endpoint, HttpMethod.GET, entity, User[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                logger.info("Utilisateur trouvé dans Supabase : {}", email);
                return Optional.of(response.getBody()[0]);
            } else {
                logger.warn("Aucun utilisateur trouvé pour : {}", email);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la requête Supabase pour {} : {}", email, e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Vérifie les identifiants
     */

    public boolean authenticate(String email, String password) {
        logger.debug("Vérification des identifiants pour : {}", email);

        Optional<User> optionalUser = findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            logger.debug("Utilisateur récupéré. Vérification du mot de passe...");

            boolean match = password.equals(user.getPassword());
            if (match) {
                logger.info("Mot de passe correct pour {}", email);
            } else {
                logger.warn("Mot de passe incorrect pour {}", email);
            }
            return match;
        }

        logger.warn("Utilisateur introuvable pour {}", email);
        return false;
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
