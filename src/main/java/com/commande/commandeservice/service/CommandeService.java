package com.commande.commandeservice.service;

import com.commande.commandeservice.CommandeRepository;
import com.commande.commandeservice.model.Commande;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CommandeService {

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${service.produit.url}")
    private String produitServiceUrl;

    @Value("${service.notification.url}")
    private String notificationServiceUrl;

    public Commande creerCommande(Commande commande) {
        // 1. Vérifier si le produit existe en appelant le service produit
        // Note: Dans un vrai projet, on créerait des DTOs (Data Transfer Objects)
        // pour ne pas dépendre directement de la structure de l'autre service.
        try {
            restTemplate.getForObject(produitServiceUrl + "/produits/" + commande.getProduitId(), Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Produit non trouvé: " + commande.getProduitId());
        }

        // 2. Si le produit existe, on enregistre la commande
        commande.setDateCommande(LocalDate.now());
        Commande savedCommande = commandeRepository.save(commande);

        // 3. Envoyer une notification (appel asynchrone dans l'idéal)
        String message = "Nouvelle commande " + savedCommande.getId() + " enregistrée.";
        restTemplate.postForObject(notificationServiceUrl + "/notifications", Map.of("message", message), String.class);

        return savedCommande;
    }

    public List<Commande> listerCommandes() {
        return commandeRepository.findAll();
    }
}
