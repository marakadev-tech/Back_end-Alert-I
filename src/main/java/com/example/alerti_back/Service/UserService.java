package com.example.alerti_back.Service;

import com.example.alerti_back.Model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class UserService {
    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

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
            return passwordsMatch(user.getPassword(), password);
        }
        return false;
    }
    public boolean mobileAuthenticate(String num_tel, String password) {
        Optional<User> optionalUser = findByNumTel(num_tel);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return passwordsMatch(user.getPassword(), password);
        }
        return false;
    }

    /**
     * Compatibility matching for legacy users:
     * - exact match (same format)
     * - stored plain text vs provided SHA-256 hash
     * - stored SHA-256 hash vs provided plain text
     */
    private boolean passwordsMatch(String storedPassword, String providedPassword) {
        if (storedPassword == null || providedPassword == null) {
            return false;
        }

        String stored = storedPassword.trim();
        String provided = providedPassword.trim();

        // 1) Direct match
        if (stored.equals(provided)) {
            return true;
        }

        // 2) BCrypt compatibility (legacy or other clients)
        // If stored value looks like BCrypt, try matching both raw and SHA-256 raw.
        if (isBcryptHash(stored)) {
            if (bCryptPasswordEncoder.matches(provided, stored)) {
                return true;
            }
            String providedAsSha256 = sha256Hex(provided);
            return bCryptPasswordEncoder.matches(providedAsSha256, stored);
        }

        // 3) SHA-256 / plain compatibility
        String storedAsSha256 = sha256Hex(stored);
        if (storedAsSha256.equalsIgnoreCase(provided)) {
            return true;
        }

        String providedAsSha256 = sha256Hex(provided);
        return providedAsSha256.equalsIgnoreCase(stored);
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("\\D", "");
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 non supporté", e);
        }
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
