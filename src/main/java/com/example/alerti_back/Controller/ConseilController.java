package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.Conseil;
import com.example.alerti_back.Service.ConseilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conseils")
public class ConseilController {

    private final ConseilService conseilService;

    @Autowired
    public ConseilController(ConseilService conseilService) {
        this.conseilService = conseilService;
    }

    @PostMapping("/add_conseil")
    public ResponseEntity<String> ajouterConseil(@RequestBody Conseil conseil) {
        System.out.println("Requête reçue : " + conseil.getType());
        try {
            conseilService.ajouterConseil(conseil);
            return ResponseEntity.ok("Conseil ajouté avec succès.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Erreur lors de l'ajout du conseil : " + e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Conseil>> getConseilsParType(@PathVariable String type) {
        try {
            List<Conseil> conseils = conseilService.getConseilsParType(type);
            return ResponseEntity.ok(conseils);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }



}
