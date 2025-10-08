package com.example.alerti_back.Controller;

import com.example.alerti_back.Service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour la démonstration en direct
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    @Autowired
    private DemoService demoService;

    /**
     * Activer le mode démonstration
     * GET /api/demo/enable
     */
    @GetMapping("/enable")
    public ResponseEntity<?> enableDemoMode() {
        try {
            demoService.enableDemoMode();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mode démonstration activé - Notifications automatiques toutes les 30 secondes");
            response.put("demo_mode", true);
            response.put("interval", "30 secondes");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Désactiver le mode démonstration
     * GET /api/demo/disable
     */
    @GetMapping("/disable")
    public ResponseEntity<?> disableDemoMode() {
        try {
            demoService.disableDemoMode();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mode démonstration désactivé");
            response.put("demo_mode", false);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Statut du mode démonstration
     * GET /api/demo/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getDemoStatus() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("demo_mode", demoService.isDemoModeActive());
            response.put("demo_counter", demoService.getDemoCounter());
            response.put("message", demoService.isDemoModeActive() ? 
                "Mode démonstration actif" : "Mode démonstration inactif");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Déclencher une notification de démonstration immédiate
     * GET /api/demo/trigger-now
     */
    @GetMapping("/trigger-now")
    public ResponseEntity<?> triggerDemoNow() {
        try {
            // Déclencher immédiatement une démonstration
            demoService.demoAutomaticNotifications();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Démonstration déclenchée immédiatement");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "Erreur: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
