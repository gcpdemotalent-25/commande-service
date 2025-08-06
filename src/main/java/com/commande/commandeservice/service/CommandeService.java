package com.commande.commandeservice.service;

import com.commande.commandeservice.CommandeRepository;
import com.commande.commandeservice.model.Commande;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
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

    // C'est ici que la magie opère : Spring injecte un template pour interagir avec Pub/Sub
    @Autowired
    private PubSubTemplate pubSubTemplate;

    // Injecter le nom du topic depuis application.properties
    @Value("${app.pubsub.topic-name}")
    private String topicName;

    @Value("${service.produit.url}")
    private String produitServiceUrl;
    // Helper pour convertir des objets en JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Commande creerCommande(Commande commande) throws Exception { // Ajout de "throws Exception"
        // 1. Vérifier si le produit existe (logique synchrone inchangée)
        try {
            restTemplate.getForObject(produitServiceUrl + "/produits/" + commande.getProduitId(), Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Produit non trouvé: " + commande.getProduitId());
        }

        // 2. Enregistrer la commande
        commande.setDateCommande(LocalDate.now());
        Commande savedCommande = commandeRepository.save(commande);

        // 3. Envoyer une notification DECOUPLEE via Pub/Sub
        System.out.println("Publication d'un message sur le topic: " + topicName);

        // Créer un payload simple (un message) pour la notification
        Map<String, String> notificationPayload = Map.of(
                "commandeId", savedCommande.getId().toString(),
                "produitId", savedCommande.getProduitId().toString(),
                "message", "Nouvelle commande " + savedCommande.getId() + " enregistrée."
        );

        // Convertir le payload en JSON et le publier
        this.pubSubTemplate.publish(this.topicName, objectMapper.writeValueAsString(notificationPayload));

        System.out.println("Message publié avec succès !");

        return savedCommande;
    }

    public List<Commande> listerCommandes() {
        return commandeRepository.findAll();
    }

}
