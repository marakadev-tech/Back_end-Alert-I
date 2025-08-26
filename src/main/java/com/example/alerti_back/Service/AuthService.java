package com.example.alerti_back.Service;

import com.example.alerti_back.Model.User;
import com.example.alerti_back.Secutity.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public String login(String email, String password) {
        logger.info("Tentative de login pour l'email : {}", email);

        if (userService.authenticate(email, password)) {
            User user = userService.findByEmail(email).get();
            logger.info("Login réussi pour : {}", email);

            String token = jwtService.generateToken(user.getEmail());
            logger.debug("Token JWT généré : {}", token); // ⚠️ En prod, évite d'afficher le token
            return token;
        }

        logger.warn("Échec d'authentification pour : {}", email);
        throw new RuntimeException("Identifiants invalides");
    }

    public boolean register(User user) {
        logger.info("Tentative d'enregistrement pour : {}", user.getEmail());

        if (userService.findByEmail(user.getEmail()).isPresent()) {
            logger.warn("Enregistrement échoué : email déjà utilisé : {}", user.getEmail());
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        boolean result = userService.saveUser(user);
        logger.info("Résultat de la création pour {} : {}", user.getEmail(), result);
        return result;
    }


}
