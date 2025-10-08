package com.example.alerti_back.Service;

import com.example.alerti_back.Model.HistoryEntry;
import com.example.alerti_back.Model.Sensors;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FirestoreService {
    
    private boolean firebaseInitialized = false;
    
    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                File serviceAccountFile = new File("src/main/resources/firebase/serviceAccountKey.json");
                
                // Vérifier si le fichier existe (optionnel en production)
                if (!serviceAccountFile.exists()) {
                    System.out.println("⚠️ Firebase serviceAccountKey.json non trouvé - Firestore désactivé");
                    firebaseInitialized = false;
                    return;
                }
                
                FileInputStream serviceAccount = new FileInputStream(serviceAccountFile);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                firebaseInitialized = true;
                System.out.println("✅ Firebase initialisé !");
            } else {
                firebaseInitialized = true;
                System.out.println("⚠️ Firebase déjà initialisé.");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur initialisation Firebase (non bloquant): " + e.getMessage());
            firebaseInitialized = false;
        }
    }
    public Sensors getSensorWithHistory(String sensorId) throws Exception {
        if (!firebaseInitialized) {
            System.err.println("⚠️ Firebase non initialisé - getSensorWithHistory ignoré");
            return null;
        }
        
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("sensors").document(sensorId);
        DocumentSnapshot snapshot = docRef.get().get();

        if (!snapshot.exists()) return null;

        Sensors sensor = new Sensors();

        sensor.setId(snapshot.getId());

        sensor.setUpdatedAt(snapshot.getDate("updated_at"));

        // 🔁 timestamp Firestore ISO Date
        Timestamp ts = snapshot.getTimestamp("timestamp");
        if (ts != null) {
            sensor.setTimestamp(ts.toDate());
        }

        // 📥 Lire la sous-collection history
        ApiFuture<QuerySnapshot> historyFuture = docRef.collection("history").get();
        List<QueryDocumentSnapshot> historyDocs = historyFuture.get().getDocuments();

        List<HistoryEntry> historyList = new ArrayList<>();
        for (QueryDocumentSnapshot doc : historyDocs) {
            HistoryEntry entry = new HistoryEntry();

            entry.setTemperature(doc.getDouble("temperature"));
            entry.setHumidity(doc.getDouble("humidity"));
            entry.setNiveauEau(doc.getDouble("niveau_eau"));
            entry.setVitesseDuVent(doc.getDouble("Vitesse_du_vent"));

            Double tsUnix = doc.getDouble("timestamp");
            if (tsUnix != null) {
                entry.setTimestamp(new Date(tsUnix.longValue() * 1000)); // Converti en millisecondes
            }

            historyList.add(entry);
        }

        sensor.setHistory(historyList);
        return sensor;
    }
}
