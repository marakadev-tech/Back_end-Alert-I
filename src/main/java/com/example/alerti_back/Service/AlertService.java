package com.example.alerti_back.Service;

import com.example.alerti_back.Model.AlertLevel;
import com.example.alerti_back.Model.Sensors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service pour gérer les alertes basées sur les niveaux d'eau des capteurs
 */
@Service
public class AlertService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private WeatherService weatherService;

    // Pourcentages du seuil pour chaque niveau d'alerte
    @Value("${alert.level.moderate.percentage:0.70}")
    private double moderateThresholdPercentage;

    @Value("${alert.level.high.percentage:0.85}")
    private double highThresholdPercentage;

    @Value("${alert.level.critical.percentage:1.0}")
    private double criticalThresholdPercentage;

    // Cache pour éviter d'envoyer des alertes en double pour le même niveau
    private final Map<String, AlertLevel> lastAlertLevelBySensor = new HashMap<>();

    /**
     * Détermine le niveau d'alerte en fonction de la pluviométrie actuelle et du seuil
     *
     * @param pluviometrieActuelle La pluviométrie actuelle mesurée (mm/jour)
     * @param seuilPluviometrie Le seuil pluviométrique configuré (mm/jour)
     * @return Le niveau d'alerte correspondant
     */
    public AlertLevel determineAlertLevel(Double pluviometrieActuelle, Double seuilPluviometrie) {
        if (pluviometrieActuelle == null || seuilPluviometrie == null || seuilPluviometrie == 0) {
            return AlertLevel.NORMAL;
        }

        double ratio = pluviometrieActuelle / seuilPluviometrie;

        if (ratio >= criticalThresholdPercentage) {
            return AlertLevel.DANGER;
        } else if (ratio >= highThresholdPercentage) {
            return AlertLevel.ATTENTION;
        } else if (ratio >= moderateThresholdPercentage) {
            return AlertLevel.ATTENTION;
        } else {
            return AlertLevel.NORMAL;
        }
    }

    /**
     * Vérifie le niveau d'alerte et envoie une notification si nécessaire
     *
     * @param sensor Le capteur à vérifier
     * @param recipients Liste des destinataires des notifications
     */
    public void checkAndNotify(Sensors sensor, List<String> recipients) {
        if (sensor == null || sensor.getPluviometrieJour() == null) {
            return;
        }

        AlertLevel currentLevel = determineAlertLevel(
                sensor.getPluviometrieJour(),
                sensor.getSeuilPluviometrie()
        );

        // Récupérer le dernier niveau d'alerte envoyé pour ce capteur
        AlertLevel lastLevel = lastAlertLevelBySensor.get(sensor.getId());

        // Envoyer une notification seulement si le niveau d'alerte a changé
        // ou s'il s'agit d'un niveau critique (toujours notifier)
        boolean shouldNotify = (lastLevel == null || !lastLevel.equals(currentLevel)) 
                               || currentLevel == AlertLevel.DANGER;

        if (shouldNotify && currentLevel != AlertLevel.NORMAL) {
            System.out.println("🚨 Alerte détectée pour le capteur " + sensor.getId() 
                + ": " + currentLevel.getLabel() 
                + " (Pluviométrie: " + sensor.getPluviometrieJour() 
                + " mm/jour, Seuil: " + sensor.getSeuilPluviometrie() + " mm/jour)");

            boolean sent = notificationService.sendAlert(sensor, currentLevel, recipients);
            
            if (sent) {
                // Mettre à jour le cache uniquement si la notification a été envoyée
                lastAlertLevelBySensor.put(sensor.getId(), currentLevel);
            }
        } else if (currentLevel == AlertLevel.NORMAL && lastLevel != null && lastLevel != AlertLevel.NORMAL) {
            // Le niveau est revenu à la normale, on peut notifier le retour à la normale
            System.out.println("✅ Retour à la normale pour le capteur " + sensor.getId());
            lastAlertLevelBySensor.put(sensor.getId(), AlertLevel.NORMAL);
        }
    }

    /**
     * Obtient le niveau d'alerte actuel pour un capteur
     */
    public AlertLevel getCurrentAlertLevel(Sensors sensor) {
        if (sensor == null) {
            return AlertLevel.NORMAL;
        }
        return determineAlertLevel(
                sensor.getPluviometrieJour(),
                sensor.getSeuilPluviometrie()
        );
    }

    /**
     * Vérifie les alertes en utilisant les données météo réelles d'OpenWeatherMap
     * Utilise les coordonnées GPS exactes du capteur
     */
    public void checkAndNotifyWithRealWeather(Sensors sensor, List<String> recipients) {
        if (sensor == null) {
            return;
        }

        try {
            // Récupérer les données météo réelles avec les coordonnées du capteur
            Map<String, Object> weatherData = weatherService.getWeatherForSensor(sensor);
            
            if (!(Boolean) weatherData.get("success")) {
                System.err.println("❌ Impossible de récupérer les données météo pour " + sensor.getId());
                return;
            }

            // Utiliser la pluviométrie 24h d'OpenWeatherMap
            Double pluviometrieReelle = (Double) weatherData.get("pluviometrie_24h");
            if (pluviometrieReelle == null || pluviometrieReelle == 0.0) {
                pluviometrieReelle = (Double) weatherData.get("pluviometrie_1h");
            }

            if (pluviometrieReelle == null) {
                pluviometrieReelle = 0.0;
            }

            // Mettre à jour le capteur avec les données météo réelles
            sensor.setPluviometrieJour(pluviometrieReelle);

            // Déterminer le niveau d'alerte
            AlertLevel currentLevel = determineAlertLevel(
                    pluviometrieReelle,
                    sensor.getSeuilPluviometrie()
            );

            AlertLevel lastLevel = lastAlertLevelBySensor.get(sensor.getId());

            boolean shouldNotify = (lastLevel == null || !lastLevel.equals(currentLevel))
                                   || currentLevel == AlertLevel.DANGER;

            if (shouldNotify && currentLevel != AlertLevel.NORMAL) {
                System.out.println("🌤️ Alerte météo détectée pour le capteur " + sensor.getId()
                    + ": " + currentLevel.getLabel()
                    + " (Pluviométrie réelle: " + pluviometrieReelle
                    + " mm/24h, Seuil: " + sensor.getSeuilPluviometrie() + " mm/jour)");

                boolean sent = notificationService.sendAlert(sensor, currentLevel, recipients);

                if (sent) {
                    lastAlertLevelBySensor.put(sensor.getId(), currentLevel);
                }
            } else if (currentLevel == AlertLevel.NORMAL && lastLevel != null && lastLevel != AlertLevel.NORMAL) {
                System.out.println("✅ Retour à la normale (météo) pour le capteur " + sensor.getId());
                lastAlertLevelBySensor.put(sensor.getId(), AlertLevel.NORMAL);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification météo pour " + sensor.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Réinitialise le cache d'alertes pour un capteur spécifique
     */
    public void resetAlertCache(String sensorId) {
        lastAlertLevelBySensor.remove(sensorId);
    }

    /**
     * Réinitialise tout le cache d'alertes
     */
    public void resetAllAlertCache() {
        lastAlertLevelBySensor.clear();
    }

    /**
     * Obtient les statistiques des alertes
     */
    public Map<String, Object> getAlertStats() {
        Map<String, Object> stats = new HashMap<>();
        
        Map<String, Integer> levelCounts = new HashMap<>();
        for (AlertLevel level : lastAlertLevelBySensor.values()) {
            levelCounts.put(level.name(), levelCounts.getOrDefault(level.name(), 0) + 1);
        }
        
        stats.put("totalSensorsMonitored", lastAlertLevelBySensor.size());
        stats.put("levelCounts", levelCounts);
        stats.put("moderateThreshold", moderateThresholdPercentage * 100 + "%");
        stats.put("highThreshold", highThresholdPercentage * 100 + "%");
        stats.put("criticalThreshold", criticalThresholdPercentage * 100 + "%");
        
        return stats;
    }
}


