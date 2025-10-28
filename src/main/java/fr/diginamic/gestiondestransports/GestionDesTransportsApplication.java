package fr.diginamic.gestiondestransports;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * Classe principale de démarrage de l'application Spring Boot de gestion des transports.
 * Configure l'application de covoiturage d'entreprise et de réservation de véhicules de service.
 * Initialise le fuseau horaire par défaut à UTC pour garantir la cohérence des dates/heures.
 * Point d'entrée de l'application conforme au cahier des charges du système de gestion des transports.
 */
@SpringBootApplication

public class GestionDesTransportsApplication {


	public static void main(String[] args) {
		SpringApplication.run(GestionDesTransportsApplication.class, args);
	}
    /**
     * Méthode d'initialisation exécutée après la construction du bean.
     * Configure le fuseau horaire par défaut de l'application à UTC.
     * Garantit la cohérence des timestamps pour les réservations et annonces de covoiturage,
     * indépendamment du fuseau horaire du serveur.
     */
    @PostConstruct
    public void init(){

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}



