package com.example.alerti_back.Service.MobileService;

import com.example.alerti_back.Model.User;
import com.example.alerti_back.Secutity.MobileSecurity.MobileApiJwtService;
import com.example.alerti_back.Service.UserService;
import org.springframework.stereotype.Service;

@Service
public class MobileApiAuthService {
    private final UserService userService;
    private final MobileApiJwtService mobileApiJwtService;

    public MobileApiAuthService(UserService userService, MobileApiJwtService mobileApiJwtService) {
        this.userService = userService;
        this.mobileApiJwtService = mobileApiJwtService;
    }

    public String login(String num_tel, String password) {
        if (userService.mobileAuthenticate(num_tel, password)) {
            User user = userService.findByNumTel(num_tel).get();
            return mobileApiJwtService.generateToken(user.getNum_tel());
        }
        throw new RuntimeException("Identifiants invalides");
    }

    public boolean register(User user) {
        if (userService.findByNumTel(String.valueOf(user.getNum_tel())).isPresent()) {
            throw new RuntimeException("Cet numero existe déjà");
        }
        return userService.saveUser(user);
    }

    /**
     * Vérifie juste qu'un compte existe pour ce numéro.
     */
    public void requestPasswordReset(String numTel) {
        String normalizedPhone = normalizePhone(numTel);
        if (normalizedPhone.isEmpty()) {
            throw new RuntimeException("Numéro de téléphone invalide");
        }
        if (userService.findByNumTel(normalizedPhone).isEmpty()) {
            throw new RuntimeException("Aucun compte associé à ce numéro");
        }
    }

    /**
     * Met à jour le mot de passe sans vérification OTP (temporaire).
     */
    public void confirmPasswordReset(String numTel, String newPassword) {
        String normalizedPhone = normalizePhone(numTel);
        if (normalizedPhone.isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("Données de réinitialisation invalides");
        }

        if (userService.findByNumTel(normalizedPhone).isEmpty()) {
            throw new RuntimeException("Aucun compte associé à ce numéro");
        }

        boolean updated = userService.updatePasswordByNumTel(normalizedPhone, newPassword.trim());
        if (!updated) {
            throw new RuntimeException("Impossible de mettre à jour le mot de passe");
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("\\D", "");
    }
}
