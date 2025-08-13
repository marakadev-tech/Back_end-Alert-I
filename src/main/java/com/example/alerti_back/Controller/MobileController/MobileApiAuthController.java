package com.example.alerti_back.Controller.MobileController;

import com.example.alerti_back.Model.User;
import com.example.alerti_back.Service.MobileService.MobileApiAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class MobileApiAuthController {
    private final MobileApiAuthService mobileApiAuthService;

    public MobileApiAuthController(MobileApiAuthService mobileApiAuthService) {
        this.mobileApiAuthService = mobileApiAuthService;
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
        String token = String.valueOf(mobileApiAuthService.login(loginData.get("num_tel"), loginData.get("password")));
        return Map.of("token", token);
    }
}
