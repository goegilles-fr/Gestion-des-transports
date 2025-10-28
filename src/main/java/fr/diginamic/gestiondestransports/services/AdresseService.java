package fr.diginamic.gestiondestransports.services;

import fr.diginamic.gestiondestransports.entites.Adresse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * Interface de service pour la gestion des adresses.
 * Définit les opérations métier pour créer, consulter, modifier et supprimer des adresses.
 * Les adresses sont utilisées pour les points de départ et d'arrivée des annonces de covoiturage,
 * ainsi que pour les adresses personnelles des utilisateurs.
 * Implémentée par AdresseServiceImpl.
 */
public interface AdresseService {
    Adresse creerAdresse(Adresse adresse);

    Adresse creerAdresse(Integer numero, String libelle, String codePostal, String ville);

    @Transactional(readOnly = true)
    Adresse trouverParId(Long id);

    Adresse mettreAJourAdresse(Long id, Adresse adresseModifiee);

    @Transactional(readOnly = true)
    List<Adresse> obtenirToutesLesAdresses();

    boolean supprimerAdresse(Long id);
}
