package com.example.alerti_back.Service;

import com.example.alerti_back.Model.User;
import com.example.alerti_back.Secutity.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthService(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public String login(String email, String password) {
        if (userService.authenticate(email, password)) {
            User user = userService.findByEmail(email).get();
            return jwtService.generateToken(user.getEmail());
        }
        throw new RuntimeException("Identifiants invalides");
    }

    public boolean register(User user) {
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }
        return userService.saveUser(user);
    }
}
