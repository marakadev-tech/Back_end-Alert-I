package com.example.alerti_back.Controller;


import com.example.alerti_back.Model.HistoryEntry;
import com.example.alerti_back.Model.Sensors;
import com.example.alerti_back.Service.SensorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorsController {

    private final SensorService sensorService;

    // Injection du service
    public SensorsController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @PostMapping("adddispo")
    public  ResponseEntity<?>AddDispo(@RequestBody Sensors sensors){
        sensorService.AddDispositive(sensors);
        return ResponseEntity.ok("Dispositif ajouté avec succès");
    }

    // Endpoint pour récupérer tous les capteurs
    @GetMapping
    public ResponseEntity<List<Sensors>> getAllSensors() {
        return ResponseEntity.ok(sensorService.getAllSensors());
    }

    // Endpoint pour récupérer l’historique d’un capteur donné
    @GetMapping("/{id}/history")
    public ResponseEntity<List<HistoryEntry>> getSensorHistory(@PathVariable String id) {
        return ResponseEntity.ok(sensorService.getHistoryBySensorId(id));
    }

    @GetMapping("/{id}/history-by-date")
    public List<HistoryEntry> getHistoryByDate(
            @PathVariable("id") String sensorId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return sensorService.getHistoryBySensorIdAndDate(sensorId, date);
    }
}
