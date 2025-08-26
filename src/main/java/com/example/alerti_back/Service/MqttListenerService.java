package com.example.alerti_back.Service;
/*
import com.example.alerti_back.Model.HistoryEntry;
import com.example.alerti_back.Model.RainForecast;
import com.example.alerti_back.Model.Sensors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLSocketFactory;
import java.util.*;

@Service*/
public class MqttListenerService {}

    /*@Value("${mqtt.broker}")
    private String mqttBroker;

    @Value("${mqtt.topic}")
    private String mqttTopic;

    @Value("${mqtt.username}")
    private String mqttUsername;

    @Value("${mqtt.password}")
    private String mqttPassword;

    @Autowired
    private SupabaseService supabaseService;

    @Autowired
    private RainForecastService rainForecastService;

    @Autowired
    private SensorService sensorService;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private AlertService alertService;

    @PostConstruct
    public void start() {
        try {
            MqttClient client = new MqttClient(mqttBroker, MqttClient.generateClientId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(mqttUsername);
            options.setPassword(mqttPassword.toCharArray());
            options.setSocketFactory(SSLSocketFactory.getDefault());

            client.connect(options);
            client.subscribe(mqttTopic, (topic, message) -> handleMessage(new String(message.getPayload())));
            System.out.println("✅ Connecté et abonné à MQTT");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleMessage(String json) {
        System.out.println("📩 Message MQTT reçu : " + json);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);

            String deviceId = node.get("device_id").asText();

            // Récupérer ou créer capteur
            Sensors sensor = supabaseService.findById(deviceId).orElse(new Sensors());
            sensor.setId(deviceId);
            sensor.setStatut("active");

            // 🕒 Timestamps : vérifier si le timestamp est correct
            double timestampSeconds = node.get("timestamp").asDouble();
            Date timestampDate;

            if (timestampSeconds < 1_000_000_000) {
                System.err.println("⚠️ Capteur " + deviceId + " a envoyé un timestamp invalide (" + timestampSeconds + "). Utilisation de la date actuelle.");
                timestampDate = new Date(); // maintenant
            } else {
                timestampDate = new Date((long) (timestampSeconds * 1000));
            }

            sensor.setTimestamp(timestampDate);
            sensor.setUpdatedAt(new Date());

            // Données capteur
            sensor.setDernierDonneeCaptemperature(getDoubleOrNull(node, "Temperature"));
            sensor.setDernierDonneeCapniveauEau(getDoubleOrNull(node, "niveau_eau"));
            sensor.setDernierDonneevitesseDuVent(getDoubleOrNull(node, "vitesse_du_vent"));

            // Localisation
            sensor.setLatitude(getDoubleOrNull(node, "latitude"));
            sensor.setLongitude(getDoubleOrNull(node, "longitude"));

            // Historique
            HistoryEntry entry = new HistoryEntry();
            entry.setTimestamp(timestampDate);
            entry.setTemperature(sensor.getDernierDonneeCaptemperature());
            entry.setNiveauEau(sensor.getDernierDonneeCapniveauEau());
            entry.setVitesseDuVent(sensor.getDernierDonneevitesseDuVent());

            List<HistoryEntry> history = sensor.getHistory();
            if (history == null) {
                history = new ArrayList<>();
            }
            history.add(entry);
            sensor.setHistory(history);

            // ⚠️ Prévision & alerte
            Double seuil = sensor.getSeuilniveauEau();
            Double niveauActuel = sensor.getDernierDonneeCapniveauEau();
            Double lat = sensor.getLatitude();
            Double lon = sensor.getLongitude();

            if (niveauActuel != null && lat != null && lon != null) {
                try {
                    // Pluie prévue sur 3 heures
                    double pluiePrevue = weatherService.getRainForecast(lat, lon);
                    double totalAnticipe = niveauActuel + pluiePrevue;

                    System.out.println("🌧️ Pluie prévue (mm)     : " + pluiePrevue);
                    System.out.println("🌊 Niveau actuel (mm)    : " + niveauActuel);
                    System.out.println("🚧 Seuil du capteur (mm) : " + (seuil != null ? seuil : "non défini"));
                    System.out.println("🧮 Total anticipé (mm)    : " + totalAnticipe);


                    // ✅ Sauvegarder la prévision dans rain_forecast
                    RainForecast forecast = new RainForecast();
                    forecast.setSensorId(deviceId);
                    forecast.setPluiePrevue(pluiePrevue);
                    forecast.setForecastFrom(timestampDate);

                    Date forecastTo = new Date(timestampDate.getTime() + (3 * 60 * 60 * 1000)); // +3 heures
                    forecast.setForecastTo(forecastTo);
                    forecast.setCreatedAt(new Date());

                    rainForecastService.saveRainForecast(forecast);

                    // 🚨 Envoyer alerte si dépassement
                    if (seuil != null && totalAnticipe >= seuil) {
                        System.out.println("🚨 INONDATION ANTICIPÉE pour le capteur " + deviceId);
                        alertService.sendAlert(deviceId,
                                "⚠️ Risque d’inondation imminent ! Niveau estimé : " +
                                        totalAnticipe + " mm (seuil : " + seuil + " mm)");
                    } else if (seuil == null) {
                        System.out.println("ℹ️ Aucun seuil défini pour ce capteur. Aucune alerte envoyée.");
                    }

                } catch (Exception e) {
                    System.err.println("❌ Erreur appel API météo : " + e.getMessage());
                }
            }

            // ✅ Sauvegarde finale du capteur
            supabaseService.saveSensorData(sensor);

        } catch (Exception e) {
            System.err.println("❌ Erreur traitement MQTT : " + e.getMessage());
            e.printStackTrace();
        }
    }



   /* private void handleMessage(String json) {
        System.out.println("📩 Message MQTT reçu : " + json);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);

            String deviceId = node.get("device_id").asText();

            // Récupérer ou créer capteur
            Sensors sensor = supabaseService.findById(deviceId).orElse(new Sensors());
            sensor.setId(deviceId);
            sensor.setStatut("active");

            // Timestamps
            double timestampSeconds = node.get("timestamp").asDouble();
            Date timestampDate = new Date((long) (timestampSeconds * 1000));
            sensor.setTimestamp(timestampDate);
            sensor.setUpdatedAt(new Date());

            // Données capteur
            sensor.setDernierDonneeCaptemperature(getDoubleOrNull(node, "Temperature"));
            sensor.setDernierDonneeCapniveauEau(getDoubleOrNull(node, "niveau_eau"));
            sensor.setDernierDonneevitesseDuVent(getDoubleOrNull(node, "vitesse_du_vent"));

            // Localisation
            sensor.setLatitude(getDoubleOrNull(node, "latitude"));
            sensor.setLongitude(getDoubleOrNull(node, "longitude"));

            // Historique
            HistoryEntry entry = new HistoryEntry();
            entry.setTimestamp(timestampDate);
            entry.setTemperature(sensor.getDernierDonneeCaptemperature());
            entry.setNiveauEau(sensor.getDernierDonneeCapniveauEau());
            entry.setVitesseDuVent(sensor.getDernierDonneevitesseDuVent());

            List<HistoryEntry> history = sensor.getHistory();
            if (history == null) {
                history = new ArrayList<>();
            }
            history.add(entry);
            sensor.setHistory(history);

            // ⚠️ Traitement prédiction d'inondation
            Double seuil = sensor.getSeuilniveauEau();
            Double niveauActuel = sensor.getDernierDonneeCapniveauEau();
            Double lat = sensor.getLatitude();
            Double lon = sensor.getLongitude();

            if (niveauActuel != null && lat != null && lon != null) {
                try {
                    double pluiePrevue = weatherService.getRainForecast(lat, lon);
                    double totalAnticipe = niveauActuel + pluiePrevue;

                    System.out.println("🌧️ Pluie prévue (mm)     : " + pluiePrevue);
                    System.out.println("🌊 Niveau actuel (mm)    : " + niveauActuel);
                    System.out.println("🚧 Seuil du capteur (mm) : " + (seuil != null ? seuil : "non défini"));
                    System.out.println("🧮 Total anticipé (mm)    : " + totalAnticipe);

                    if (seuil != null && totalAnticipe >= seuil) {
                        System.out.println("🚨 INONDATION ANTICIPÉE pour le capteur " + deviceId);
                        alertService.sendAlert(deviceId,
                                "⚠️ Risque d’inondation imminent ! Niveau estimé : " +
                                        totalAnticipe + " mm (seuil : " + seuil + " mm)");
                    } else if (seuil == null) {
                        System.out.println("ℹ️ Aucun seuil défini pour ce capteur. Aucune alerte envoyée.");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erreur appel API météo : " + e.getMessage());
                }
            }

            // Sauvegarde finale
            supabaseService.saveSensorData(sensor);

        } catch (Exception e) {
            System.err.println("❌ Erreur traitement MQTT : " + e.getMessage());
            e.printStackTrace();
        }
    }*/


  /*  private Double getDoubleOrNull(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asDouble() : null;
    }

    private String getTextOrNull(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }
}
*/