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
import javax.net.ssl.SSLSocketFactory;
import java.util.*;

@Service
public class MqttListenerService {

    @Value("${mqtt.broker}")
    private String mqttBroker;

    @Value("${mqtt.topic}")
    private String mqttTopic;

    @Value("${mqtt.username}")
    private String mqttUsername;

    @Value("${mqtt.password}")
    private String mqttPassword;

    @Autowired
    private SupabaseService supabaseService;
    // Injecte SensorService
    @Autowired
    private SensorService sensorService;

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




        } catch (Exception e) {
            System.err.println("❌ Erreur traitement MQTT : " + e.getMessage());
            e.printStackTrace();
        }
    }


    private Double getDoubleOrNull(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asDouble() : null;
    }

    private String getTextOrNull(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }
}
