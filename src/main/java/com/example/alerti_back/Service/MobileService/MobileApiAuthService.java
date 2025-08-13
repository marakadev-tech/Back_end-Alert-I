package com.example.alerti_back.Service.MobileService;

import com.example.alerti_back.Model.User;
import com.example.alerti_back.Secutity.JwtService;
import com.example.alerti_back.Secutity.MobileSecurity.MobileApiJwtService;
import com.example.alerti_back.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MobileApiAuthService {
    private final UserService userService;
    private final MobileApiJwtService mobileApiJwtService;

    public MobileApiAuthService(UserService userService, MobileApiJwtService mobileApiJwtService) {
        this.userService = userService;
        this.mobileApiJwtService = mobileApiJwtService;
    }

    public Map<String, Object> login(String num_tel, String password) {
        if (userService.mobileAuthenticate(num_tel, password)) {
            User user = userService.findByNumTel(num_tel).get();
            String token = mobileApiJwtService.generateToken(user.getNum_tel());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            return response;
        }
        throw new RuntimeException("Identifiants invalides");
    }

    public boolean register(User user) {
        if (userService.findByNumTel(String.valueOf(user.getNum_tel())).isPresent()) {
            throw new RuntimeException("Cet numero existe déjà");
        }
        return userService.saveUser(user);
    }
}
