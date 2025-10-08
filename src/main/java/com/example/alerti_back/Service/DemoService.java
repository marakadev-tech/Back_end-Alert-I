package com.example.alerti_back.Service;

import com.example.alerti_back.Model.AlertLevel;
import com.example.alerti_back.Model.Sensors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de démonstration pour la présentation
 * Déclenche des notifications automatiquement
 */
@Service
public class DemoService {

    @Autowired
    private SupabaseService supabaseService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private NotificationService notificationService;

    private int demoCounter = 0;
    private boolean demoMode = false;

    /**
     * Activer le mode démonstration
     */
    public void enableDemoMode() {
        demoMode = true;
        demoCounter = 0;
        System.out.println("🎤 MODE DÉMONSTRATION ACTIVÉ - Notifications automatiques toutes les 30 secondes");
    }

    /**
     * Désactiver le mode démonstration
     */
    public void disableDemoMode() {
        demoMode = false;
        System.out.println("🎤 MODE DÉMONSTRATION DÉSACTIVÉ");
    }

    /**
     * Démonstration automatique - Toutes les 30 secondes
     * Parfait pour une présentation
     */
    @Scheduled(fixedRate = 30000) // 30 secondes
    public void demoAutomaticNotifications() {
        if (!demoMode) {
            return;
        }

        demoCounter++;
        System.out.println("🎤 DÉMONSTRATION #" + demoCounter + " - " + LocalDateTime.now());

        try {
            // Récupérer un capteur pour la démonstration
            List<Sensors> activeSensors = supabaseService.getAllActiveSensors();
            
            if (activeSensors.isEmpty()) {
                System.out.println("⚠️ Aucun capteur actif pour la démonstration");
                return;
            }

            // Prendre le premier capteur actif
            Sensors demoSensor = activeSensors.get(0);
            
            // Simuler différents niveaux d'alerte pour la démonstration
            AlertLevel demoAlertLevel = getDemoAlertLevel(demoCounter);
            
            // Forcer des valeurs de démonstration
            demoSensor.setPluviometrieJour(getDemoPluviometrie(demoCounter));
            demoSensor.setSeuilPluviometrie(25.0); // Seuil fixe pour la démo
            
            System.out.println("🎯 Capteur: " + demoSensor.getId());
            System.out.println("🌧️ Pluviométrie: " + demoSensor.getPluviometrieJour() + " mm/jour");
            System.out.println("📊 Seuil: " + demoSensor.getSeuilPluviometrie() + " mm/jour");
            System.out.println("🚨 Niveau d'alerte: " + demoAlertLevel.getLabel());
            
            // Message personnalisé pour la démonstration
            String demoMessage = getDemoMessage(demoAlertLevel, demoSensor.getPluviometrieJour());
            System.out.println("📱 Message de démonstration: " + demoMessage);
            
            // Envoyer la notification
            boolean sent = notificationService.sendAlert(demoSensor, demoAlertLevel, new ArrayList<>());
            
            if (sent) {
                System.out.println("✅ Notification de démonstration envoyée avec succès !");
            } else {
                System.out.println("❌ Échec de l'envoi de la notification de démonstration");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la démonstration: " + e.getMessage());
        }
    }

    /**
     * Démonstration avec capteur spécifique - Toutes les 45 secondes
     */
    @Scheduled(fixedRate = 45000) // 45 secondes
    public void demoWithSpecificSensor() {
        if (!demoMode) {
            return;
        }

        System.out.println("🎤 DÉMONSTRATION CAPTEUR SPÉCIFIQUE - " + LocalDateTime.now());

        try {
            // Utiliser ESP32_2 pour la démonstration
            var sensorOpt = supabaseService.findById("ESP32_2");
            if (sensorOpt.isEmpty()) {
                System.out.println("⚠️ Capteur ESP32_2 non trouvé pour la démonstration");
                return;
            }

            Sensors sensor = sensorOpt.get();
            
            // Simuler des données météo réelles
            double pluviometrie = 15.0 + (Math.random() * 20.0); // 15-35 mm/jour
            sensor.setPluviometrieJour(pluviometrie);
            sensor.setSeuilPluviometrie(25.0);
            
            System.out.println("🎯 Capteur ESP32_2 - Pluviométrie: " + pluviometrie + " mm/jour");
            
            // Vérifier et envoyer l'alerte
            alertService.checkAndNotifyWithRealWeather(sensor, new ArrayList<>());
            
        } catch (Exception e) {
            System.err.println("❌ Erreur démonstration capteur spécifique: " + e.getMessage());
        }
    }

    /**
     * Obtenir le niveau d'alerte pour la démonstration
     */
    private AlertLevel getDemoAlertLevel(int counter) {
        switch (counter % 3) {
            case 0:
                return AlertLevel.NORMAL;
            case 1:
                return AlertLevel.ATTENTION;
            case 2:
                return AlertLevel.DANGER;
            default:
                return AlertLevel.NORMAL;
        }
    }

    /**
     * Obtenir la pluviométrie pour la démonstration
     */
    private double getDemoPluviometrie(int counter) {
        switch (counter % 3) {
            case 0:
                return 10.0; // NORMAL
            case 1:
                return 30.0; // ATTENTION
            case 2:
                return 50.0; // DANGER
            default:
                return 10.0;
        }
    }

    /**
     * Obtenir le message personnalisé pour la démonstration
     */
    private String getDemoMessage(AlertLevel alertLevel, double pluviometrie) {
        switch (alertLevel) {
            case NORMAL:
                return String.format("🌤️ Démonstration - Pluviométrie normale: %.1f mm/jour", pluviometrie);
            case ATTENTION:
                return String.format("⚠️ Démonstration - Attention: %.1f mm/jour - Risque d'inondation", pluviometrie);
            case DANGER:
                return String.format("🚨 Démonstration - DANGER: %.1f mm/jour - Évacuation recommandée!", pluviometrie);
            default:
                return String.format("📊 Démonstration - Pluviométrie: %.1f mm/jour", pluviometrie);
        }
    }

    /**
     * Statut du mode démonstration
     */
    public boolean isDemoModeActive() {
        return demoMode;
    }

    /**
     * Compteur de démonstrations
     */
    public int getDemoCounter() {
        return demoCounter;
    }
}
