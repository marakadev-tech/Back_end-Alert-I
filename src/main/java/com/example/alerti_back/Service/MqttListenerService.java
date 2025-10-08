package com.example.alerti_back.Service;

import com.example.alerti_back.Model.HistoryEntry;
import com.example.alerti_back.Model.Sensors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.Map;

@Service
public class MqttListenerService {

    @Value("${mqtt.broker:}")
    private String mqttBroker;

    @Value("${mqtt.topic:}")
    private String mqttTopic;

    @Value("${mqtt.username:}")
    private String mqttUsername;

    @Value("${mqtt.password:}")
    private String mqttPassword;
    private MqttClient client;

    @Autowired
    private SupabaseService supabaseService;
    
    // Injecte AlertService pour gérer les alertes
    @Autowired
    private AlertService alertService;

    // Injecte WeatherService pour les données météo réelles
    @Autowired
    private WeatherService weatherService;

    @PostConstruct
    public void start() {
        try {
            // Generate a unique client ID
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(mqttBroker, clientId);

            // Configure connection options - no authentication needed
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(30);
            options.setKeepAliveInterval(60);

            // Connect to the broker
            client.connect(options);

            // Subscribe to the topic
            client.subscribe(mqttTopic, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    handleMessage(new String(message.getPayload()));
                }
            });

            System.out.println("✅ Connecté et abonné à Mosquitto (sans sécurité)");
            System.out.println("Broker: " + mqttBroker);
            System.out.println("Topic: " + mqttTopic);
            System.out.println("Client ID: " + clientId);

        } catch (MqttException e) {
            System.err.println("❌ Erreur de connexion MQTT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleMessage(String json) {
        System.out.println("📩 Message MQTT reçu : " + json);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);

            String deviceId = node.get("device_id").asText();

            // Récupérer capteur existant s'il existe
            Sensors sensor = supabaseService.findById(deviceId).orElse(new Sensors());
            sensor.setId(deviceId);
            sensor.setStatut("active");

            // Timestamp (date/heure/minute de la dernière requête)
            double timestampSeconds = node.get("timestamp").asDouble();
            Date timestampDate = new Date((long)(timestampSeconds * 1000));

            sensor.setTimestamp(timestampDate);
            sensor.setUpdatedAt(new Date()); // mise à jour au moment de l'enregistrement

            // Dernières données du capteur
            sensor.setDernierDonneeCaptemperature(getDoubleOrNull(node, "Temperature"));
            sensor.setDernierDonneeCapniveauEau(getDoubleOrNull(node, "niveau_eau"));
            sensor.setDernierDonneevitesseDuVent(getDoubleOrNull(node, "vitesse_du_vent"));

            // Localisation si présente
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


            // Enregistrement
            supabaseService.saveSensorData(sensor);

            // 🌤️ Récupérer les données météo réelles avec les coordonnées GPS du capteur
            try {
                if (sensor.getLatitude() != null && sensor.getLongitude() != null) {
                    System.out.println("🌤️ Récupération des données météo pour " + deviceId + 
                                     " (lat: " + sensor.getLatitude() + ", lon: " + sensor.getLongitude() + ")");
                    
                    Map<String, Object> weatherData = weatherService.getWeatherForSensor(sensor);
                    
                    if ((Boolean) weatherData.get("success")) {
                        // Mettre à jour le capteur avec les données météo réelles
                        Double pluviometrie24h = (Double) weatherData.get("pluviometrie_24h");
                        if (pluviometrie24h != null) {
                            sensor.setPluviometrieJour(pluviometrie24h);
                            System.out.println("📊 Pluviométrie 24h mise à jour: " + pluviometrie24h + " mm");
                        }
                    } else {
                        System.err.println("⚠️ Impossible de récupérer les données météo pour " + deviceId);
                    }
                } else {
                    System.out.println("⚠️ Capteur " + deviceId + " sans coordonnées GPS - utilisation des données locales");
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur récupération météo pour " + deviceId + ": " + e.getMessage());
            }

            // 🚨 Vérification des alertes avec données météo réelles
            alertService.checkAndNotifyWithRealWeather(sensor, new ArrayList<>());

        } catch (Exception e) {
            System.err.println("❌ Erreur traitement MQTT : " + e.getMessage());
            e.printStackTrace();
        }
    }


    private Double getDoubleOrNull(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asDouble() : null;
    }
    public void publishMessage(String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(1);
            client.publish(mqttTopic, mqttMessage);
            System.out.println("📤 Message publié: " + message);
        } catch (MqttException e) {
            System.err.println("❌ Erreur lors de l'envoi du message: " + e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                client.close();
                System.out.println("🔌 Déconnecté de Mosquitto");
            }
        } catch (MqttException e) {
            System.err.println("❌ Erreur lors de la déconnexion: " + e.getMessage());
        }
    }
}
