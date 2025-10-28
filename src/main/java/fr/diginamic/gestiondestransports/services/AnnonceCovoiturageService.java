package fr.diginamic.gestiondestransports.services;

import fr.diginamic.gestiondestransports.dto.AnnonceCovoiturageAvecPlacesDto;
import fr.diginamic.gestiondestransports.dto.AnnonceCovoiturageDto;
import fr.diginamic.gestiondestransports.dto.ParticipantsCovoiturageDto;

import java.util.List;
/**
 * Interface de service pour la gestion des annonces de covoiturage.
 * Définit les opérations métier principales du système de covoiturage :
 * - Création, modification, suppression d'annonces par les organisateurs
 * - Réservation et annulation de places par les passagers
 * - Consultation des annonces disponibles et des participants
 * - Gestion des places (totales et occupées)
 * Applique les règles métier du cahier des charges concernant les annonces de covoiturage.
 * Implémentée par AnnonceCovoiturageServiceImpl.
 */
public interface AnnonceCovoiturageService  {

    AnnonceCovoiturageDto creerAnnonce(AnnonceCovoiturageDto annonceDto, Long idUtilisateurResponsable);


    AnnonceCovoiturageDto modifierAnnonce(Long idAnnonce, AnnonceCovoiturageDto annonceDto, Long idUtilisateurResponsable);


    void supprimerAnnonce(Long idAnnonce, Long idUtilisateurResponsable);


    AnnonceCovoiturageDto obtenirAnnonceParId(Long idAnnonce);


    Integer obtenirNombrePlacesTotales(Long idAnnonce);


    Integer obtenirNombrePlacesOccupees(Long idAnnonce);


    void reserverPlace(Long idAnnonce, Long idUtilisateur);


    void annulerReservation(Long idAnnonce, Long idUtilisateur);


    List<AnnonceCovoiturageAvecPlacesDto> obtenirToutesLesAnnonces();


    List<AnnonceCovoiturageAvecPlacesDto> obtenirReservationsUtilisateur(Long idUtilisateur);

    ParticipantsCovoiturageDto obtenirParticipants(Long id);

    List<AnnonceCovoiturageAvecPlacesDto> obtenirAnnoncesOrganiseesParUtilisateur(Long id);
}
