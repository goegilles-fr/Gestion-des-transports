package fr.diginamic.gestiondestransports.services;

import fr.diginamic.gestiondestransports.dto.AnnonceCovoiturageAvecPlacesDto;
import fr.diginamic.gestiondestransports.dto.AnnonceCovoiturageDto;
import fr.diginamic.gestiondestransports.dto.ParticipantsCovoiturageDto;

import java.util.List;

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
