package com.example.alerti_back.Service;

import com.google.api.client.util.Value;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;




/**
 * SmsService en utilisant Twilio.
 * - Initialise Twilio au démarrage (PostConstruct)
 * - Fournit sendSms(to, body) qui retourne boolean (success/fail)
 *
 * Remarque : Twilio exige un numéro en format E.164 (ex: +221771234567).
 */

@Service
public class SmsService {
/**
    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.fromNumber}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        // Initialise la librairie Twilio
        Twilio.init(accountSid, authToken);
    }

    public boolean sendSms(String to, String body) {
        try {
            Message.creator(new PhoneNumber(to), new PhoneNumber(fromNumber), body).create();
            return true;
        } catch (Exception e) {
            // Log simple — remplace par logger dans ton projet
            System.err.println("❌ Erreur envoi SMS à " + to + " : " + e.getMessage());
            return false;
        }
    }*/
}
