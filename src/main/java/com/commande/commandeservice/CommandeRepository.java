package com.commande.commandeservice;

import com.commande.commandeservice.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeRepository extends JpaRepository<Commande, Long> {}
