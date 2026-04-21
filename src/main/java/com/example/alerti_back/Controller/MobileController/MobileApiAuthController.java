package com.example.alerti_back.Controller.MobileController;

import com.example.alerti_back.Model.User;
import com.example.alerti_back.Secutity.MobileSecurity.MobileApiJwtService;
import com.example.alerti_back.Service.MobileService.MobileApiAuthService;
import com.example.alerti_back.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class MobileApiAuthController {
    private final MobileApiAuthService mobileApiAuthService;
    private final MobileApiJwtService mobileApiJwtService;
    private final UserService userService;

    public MobileApiAuthController(MobileApiAuthService mobileApiAuthService, MobileApiJwtService mobileApiJwtService, UserService userService) {
        this.mobileApiAuthService = mobileApiAuthService;
        this.mobileApiJwtService = mobileApiJwtService;
        this.userService = userService;
    }

    @PostMapping("/mobile/register")
    public Map<String, String> register(@RequestBody User user) {
        boolean created = mobileApiAuthService.register(user);
        if (created) {
            return Map.of("message", "Utilisateur créé avec succès");
        } else {
            return Map.of("message", "Échec de la création de l'utilisateur");
        }
    }

    @PostMapping("/mobile/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginData) {
        String token = mobileApiAuthService.login(loginData.get("num_tel"), loginData.get("password"));
        return Map.of("token", token);
    }

    @PostMapping("/mobile/password/reset/request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestBody Map<String, String> payload) {
        try {
            mobileApiAuthService.requestPasswordReset(payload.get("num_tel"));
            return ResponseEntity.ok(Map.of("message", "Numéro vérifié"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/mobile/password/reset/confirm")
    public ResponseEntity<Map<String, String>> confirmPasswordReset(@RequestBody Map<String, String> payload) {
        try {
            mobileApiAuthService.confirmPasswordReset(
                    payload.get("num_tel"),
                    payload.get("new_password")
            );
            return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/mobile/me")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String numTel = mobileApiJwtService.extractUsername(token);
            User user = userService.findByNumTel(numTel)
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Masquer le mot de passe avant de renvoyer
            user.setPassword(null);

            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).build();
    }


}
