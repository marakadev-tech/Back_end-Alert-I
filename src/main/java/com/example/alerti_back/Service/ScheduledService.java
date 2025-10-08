package com.example.alerti_back.Service;

import com.example.alerti_back.Model.Sensors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service pour les tâches planifiées automatiques
 */
@Service
public class ScheduledService {

    @Autowired
    private SupabaseService supabaseService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private WeatherService weatherService;

    /**
     * Vérification périodique de tous les capteurs actifs
     * Exécuté toutes les 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300000 ms
    public void checkAllSensorsPeriodically() {
        System.out.println("🕐 Vérification périodique des capteurs...");
        
        try {
            // Récupérer tous les capteurs actifs
            List<Sensors> activeSensors = supabaseService.getAllActiveSensors();
            
            if (activeSensors.isEmpty()) {
                System.out.println("ℹ️ Aucun capteur actif trouvé");
                return;
            }

            System.out.println("📊 " + activeSensors.size() + " capteurs actifs à vérifier");

            for (Sensors sensor : activeSensors) {
                try {
                    // Vérifier les alertes avec données météo réelles
                    alertService.checkAndNotifyWithRealWeather(sensor, List.of());
                    
                    System.out.println("✅ Capteur " + sensor.getId() + " vérifié");
                    
                } catch (Exception e) {
                    System.err.println("❌ Erreur vérification capteur " + sensor.getId() + ": " + e.getMessage());
                }
            }

            System.out.println("🎯 Vérification périodique terminée");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification périodique: " + e.getMessage());
        }
    }

    /**
     * Mise à jour du cache des données météo
     * Exécuté toutes les 15 minutes
     */
    @Scheduled(fixedRate = 900000) // 15 minutes = 900000 ms
    public void updateWeatherCache() {
        System.out.println("🌤️ Mise à jour du cache météo...");
        
        try {
            // Récupérer tous les capteurs avec coordonnées GPS
            List<Sensors> sensorsWithGPS = supabaseService.getSensorsWithCoordinates();
            
            if (sensorsWithGPS.isEmpty()) {
                System.out.println("ℹ️ Aucun capteur avec coordonnées GPS trouvé");
                return;
            }

            System.out.println("📍 " + sensorsWithGPS.size() + " capteurs avec GPS à mettre à jour");

            for (Sensors sensor : sensorsWithGPS) {
                try {
                    // Récupérer les données météo pour mettre à jour le cache
                    Map<String, Object> weatherData = weatherService.getWeatherForSensor(sensor);
                    
                    if ((Boolean) weatherData.get("success")) {
                        // Mettre à jour la pluviométrie du capteur
                        Double pluviometrie24h = (Double) weatherData.get("pluviometrie_24h");
                        if (pluviometrie24h != null) {
                            sensor.setPluviometrieJour(pluviometrie24h);
                            
                            // S'assurer que les dates sont initialisées
                            if (sensor.getTimestamp() == null) {
                                sensor.setTimestamp(new Date());
                            }
                            if (sensor.getUpdatedAt() == null) {
                                sensor.setUpdatedAt(new Date());
                            }
                            
                            // S'assurer que l'historique est initialisé
                            if (sensor.getHistory() == null) {
                                sensor.setHistory(new ArrayList<>());
                            }
                            
                            supabaseService.saveSensorData(sensor);
                        }
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Erreur mise à jour météo capteur " + sensor.getId() + ": " + e.getMessage());
                }
            }

            System.out.println("✅ Cache météo mis à jour");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du cache météo: " + e.getMessage());
        }
    }

    /**
     * Nettoyage des anciennes données d'historique
     * Exécuté tous les jours à 2h du matin
     */
    @Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h00
    public void cleanupOldHistoryData() {
        System.out.println("🧹 Nettoyage des anciennes données d'historique...");
        
        try {
            // Supprimer les données d'historique de plus de 30 jours
            int deletedCount = supabaseService.cleanupOldHistoryData(30);
            System.out.println("🗑️ " + deletedCount + " entrées d'historique supprimées");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du nettoyage: " + e.getMessage());
        }
    }

    /**
     * Vérification de la santé du système
     * Exécuté toutes les heures
     */
    @Scheduled(fixedRate = 3600000) // 1 heure = 3600000 ms
    public void systemHealthCheck() {
        System.out.println("🏥 Vérification de la santé du système...");
        
        try {
            // Vérifier la connexion à la base de données
            boolean dbHealthy = supabaseService.isHealthy();
            
            // Vérifier l'API OpenWeatherMap
            boolean weatherAPIHealthy = weatherService.isHealthy();
            
            // Vérifier les capteurs actifs
            List<Sensors> activeSensors = supabaseService.getAllActiveSensors();
            
            System.out.println("📊 État du système:");
            System.out.println("  - Base de données: " + (dbHealthy ? "✅ OK" : "❌ ERREUR"));
            System.out.println("  - API Météo: " + (weatherAPIHealthy ? "✅ OK" : "❌ ERREUR"));
            System.out.println("  - Capteurs actifs: " + activeSensors.size());

            // Alerte si problème critique
            if (!dbHealthy || !weatherAPIHealthy) {
                System.err.println("🚨 ALERTE: Problème critique détecté dans le système!");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification de santé: " + e.getMessage());
        }
    }
}
