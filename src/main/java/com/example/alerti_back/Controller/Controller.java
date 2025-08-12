package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.Sensors;
import com.example.alerti_back.Service.FirestoreService;
import com.example.alerti_back.Service.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("sensors")
@CrossOrigin
public class Controller {


    @Autowired
    private SensorService sensorService;
    @Autowired
    private FirestoreService firestoreService;
    @GetMapping("/{sensorId}")
    public ResponseEntity<?> getSensor(@PathVariable String sensorId) {
        try {
            Sensors sensor = sensorService.getSensorWithHistory(sensorId);
            if (sensor == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(sensor);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la récupération des données : " + e.getMessage());
        }
    }
}
