package com.commande.commandeservice.controller;

import com.commande.commandeservice.model.Commande;
import com.commande.commandeservice.service.CommandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commandes")
@CrossOrigin(origins = "http://localhost:3000")
public class CommandeController {

    @Autowired
    private CommandeService commandeService;

    @PostMapping
    public ResponseEntity<Commande> createCommande(@RequestBody Commande commande) {
        try {
            Commande nouvelleCommande = commandeService.creerCommande(commande);
            return ResponseEntity.ok(nouvelleCommande);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public List<Commande> getAllCommandes() {
        return commandeService.listerCommandes();
    }
}
