package fr.diginamic.gestiondestransports.controllers;

import fr.diginamic.gestiondestransports.dto.AnnonceCovoiturageAvecPlacesDto;
import fr.diginamic.gestiondestransports.dto.AnnonceCovoiturageDto;
import fr.diginamic.gestiondestransports.dto.ParticipantsCovoiturageDto;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.services.AnnonceCovoiturageService;
import fr.diginamic.gestiondestransports.services.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des annonces de covoiturage
 */
@RestController
@RequestMapping("/api/covoit")
@Tag(name = "Annonces de covoiturage", description = "Gestion des annonces de covoiturage et des réservations")
public class AnnonceCovoiturageController {

    private final AnnonceCovoiturageService annonceCovoiturageService;
    private final UtilisateurService utilisateurService;

    @Autowired
    public AnnonceCovoiturageController(
            AnnonceCovoiturageService annonceCovoiturageService,
            UtilisateurService utilisateurService) {
        this.annonceCovoiturageService = annonceCovoiturageService;
        this.utilisateurService = utilisateurService;
    }

    /**
     * Crée une nouvelle annonce de covoiturage pour l'utilisateur authentifié.
     * Le véhicule de service est optionnel (vehiculeServiceId peut être null).
     * L'utilisateur devient automatiquement le responsable/conducteur de cette annonce.
     *
     * @param annonceDto les données de l'annonce à créer (adresse départ, adresse arrivée, date/heure départ, durée trajet, distance, véhicule)
     * @param authentication l'authentification JWT contenant l'email de l'utilisateur connecté
     * @return ResponseEntity contenant l'annonce créée avec son ID (201 CREATED) ou un message d'erreur (400 BAD REQUEST / 500 INTERNAL SERVER ERROR)
     * @throws IllegalArgumentException si les données de l'annonce sont invalides
     */
    @PostMapping("/create")
    @Operation(summary = "Créer une annonce. vehiculeServiceId peut être nul")
    public ResponseEntity<?> creerAnnonce(
            @Valid @RequestBody AnnonceCovoiturageDto annonceDto,
            Authentication authentication) {

        try {
            Long idUtilisateurConnecte = utilisateurService.obtenirUtilisateurParEmail(authentication.getName()).getId();
            AnnonceCovoiturageDto annonceCree = annonceCovoiturageService.creerAnnonce(annonceDto, idUtilisateurConnecte);
            return ResponseEntity.status(HttpStatus.CREATED).body(annonceCree);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne: " + e.getMessage());
        }
    }

    /**
     * Modifie une annonce de covoiturage existante.
     * La modification n'est autorisée que si aucune réservation n'a été effectuée sur cette annonce.
     * Seul le responsable de l'annonce peut la modifier.
     *
     * @param id l'identifiant unique de l'annonce à modifier
     * @param annonceDto les nouvelles données de l'annonce
     * @param authentication l'authentification JWT contenant l'email de l'utilisateur connecté
     * @return ResponseEntity contenant l'annonce modifiée (200 OK) ou vide (400 BAD REQUEST / 500 INTERNAL SERVER ERROR)
     * @throws IllegalArgumentException si l'annonce n'existe pas, si l'utilisateur n'est pas le responsable, ou si des réservations existent
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "modifier l'annonce. cela ne fonctionne que si personne ne l'a réservée")
    public ResponseEntity<AnnonceCovoiturageDto> modifierAnnonce(
            @PathVariable Long id,
            @Valid @RequestBody AnnonceCovoiturageDto annonceDto,
            Authentication authentication) {

        try {

            // Récupérer l'ID de l'utilisateur connecté depuis le JWT
            Long idUtilisateurConnecte = utilisateurService.obtenirUtilisateurParEmail(authentication.getName()).getId();

            // Modifier l'annonce
            AnnonceCovoiturageDto annonceModifiee = annonceCovoiturageService.modifierAnnonce(id, annonceDto, idUtilisateurConnecte);

            return ResponseEntity.ok(annonceModifiee);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime une annonce de covoiturage.
     * Si des utilisateurs ont réservé cette annonce, un email d'avertissement leur est envoyé automatiquement.
     * Seul le responsable de l'annonce peut la supprimer.
     *
     * @param id l'identifiant unique de l'annonce à supprimer
     * @param authentication l'authentification JWT contenant l'email de l'utilisateur connecté
     * @return ResponseEntity vide (204 NO CONTENT) en cas de succès ou erreur (400 BAD REQUEST / 500 INTERNAL SERVER ERROR)
     * @throws IllegalArgumentException si l'annonce n'existe pas ou si l'utilisateur n'est pas le responsable
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "supprimer l'annonce et envoyer un e-mail aux utilisateurs qui l'ont réservée")
    public ResponseEntity<Void> supprimerAnnonce(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            // Récupérer l'ID de l'utilisateur connecté depuis le JWT
            Long idUtilisateurConnecte = utilisateurService.obtenirUtilisateurParEmail(authentication.getName()).getId();

            // Supprimer l'annonce
            annonceCovoiturageService.supprimerAnnonce(id, idUtilisateurConnecte);

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les détails complets d'une annonce de covoiturage par son identifiant.
     * Affiche le nombre total de places du véhicule et le nombre de places déjà occupées.
     *
     * @param id l'identifiant unique de l'annonce à consulter
     * @return ResponseEntity contenant l'annonce avec les informations de places (200 OK) ou vide (404 NOT FOUND / 500 INTERNAL SERVER ERROR)
     * @throws IllegalArgumentException si l'annonce n'existe pas
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Consultez les informations sur les annonces par identifiant. L'affichage indique également le nombre total de places et leur occupation."
    )
    public ResponseEntity<AnnonceCovoiturageAvecPlacesDto> obtenirAnnonce(@PathVariable Long id) {
        try {
            AnnonceCovoiturageDto annonce = annonceCovoiturageService.obtenirAnnonceParId(id);
            Integer placesTotales = annonceCovoiturageService.obtenirNombrePlacesTotales(id);
            Integer placesOccupees = annonceCovoiturageService.obtenirNombrePlacesOccupees(id);

            AnnonceCovoiturageAvecPlacesDto response = AnnonceCovoiturageAvecPlacesDto.of(annonce, placesTotales, placesOccupees);


            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Réserve une place dans un covoiturage pour l'utilisateur authentifié.
     * L'utilisateur ne peut réserver que si :
     * - Il reste des places disponibles
     * - Il n'a pas déjà réservé cette annonce
     * - Il n'est pas le conducteur de cette annonce
     *
     * @param id l'identifiant unique de l'annonce de covoiturage
     * @param authentication l'authentification JWT contenant l'email de l'utilisateur connecté
     * @return ResponseEntity avec message de confirmation (200 OK) ou message d'erreur (400 BAD REQUEST / 500 INTERNAL SERVER ERROR)
     * @throws IllegalArgumentException si les conditions de réservation ne sont pas remplies
     */
    @PostMapping("/reserve/{id}")
    @Operation(
            summary = "Réserver une place en covoiturage. L'utilisateur ne peut réserver une place que s'il dispose d'une place libre, s'il ne l'a pas déjà réservée, s'il n'est pas conducteur")
    public ResponseEntity<String> reserverPlace(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            // Récupérer l'ID de l'utilisateur connecté depuis le JWT
            Long idUtilisateurConnecte = utilisateurService.obtenirUtilisateurParEmail(authentication.getName()).getId();

            // Réserver la place
            annonceCovoiturageService.reserverPlace(id, idUtilisateurConnecte);

            return ResponseEntity.ok("Place réservée avec succès");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la réservation");
        }
    }




    /**
     * Annule la réservation d'une place dans un covoiturage.
     * L'utilisateur doit avoir une réservation active pour cette annonce.
     * Libère une place dans le covoiturage.
     *
     * @param id l'identifiant unique de l'annonce de covoiturage
     * @param authentication l'authentification JWT contenant l'email de l'utilisateur connecté
     * @return ResponseEntity avec message de confirmation (200 OK) ou message d'erreur (400 BAD REQUEST / 500 INTERNAL SERVER ERROR)
     * @throws IllegalArgumentException si l'utilisateur n'a pas de réservation pour cette annonce
     */
    @DeleteMapping("/reserve/{id}")
    @Operation(
            summary = "Annuler une réservation de covoiturage.L'utilisateur doit avoir une réservation existante pour cette annonce.")
    public ResponseEntity<String> annulerReservation(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            // Récupérer l'ID de l'utilisateur connecté depuis le JWT
            Long idUtilisateurConnecte = utilisateurService.obtenirUtilisateurParEmail(authentication.getName()).getId();

            // Annuler la réservation
            annonceCovoiturageService.annulerReservation(id, idUtilisateurConnecte);

            return ResponseEntity.ok("Réservation annulée avec succès");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'annulation");
        }
    }


    /**
     * Récupère la liste complète de toutes les annonces de covoiturage disponibles.
     * Pour chaque annonce, affiche le nombre total de places et le nombre de places occupées.
     *
     * @return ResponseEntity contenant la liste de toutes les annonces avec leurs informations de places (200 OK) ou vide (500 INTERNAL SERVER ERROR)
     */
    @GetMapping("/")
    @Operation(
            summary = "Récupérer toutes les annonces de covoiturage. L'affichage indique également le nombre total de places et leur occupation.")


    public ResponseEntity<List<AnnonceCovoiturageAvecPlacesDto>> obtenirToutesLesAnnonces() {
        try {
            List<AnnonceCovoiturageAvecPlacesDto> annonces = annonceCovoiturageService.obtenirToutesLesAnnonces();
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Récupère toutes les réservations de covoiturage de l'utilisateur connecté en tant que passager.
     * N'inclut pas les annonces où l'utilisateur est conducteur.
     * Pour chaque réservation, affiche le nombre total de places et le nombre de places occupées.
     *
     * @param authentication l'authentification JWT contenant l'email de l'utilisateur connecté
     * @return ResponseEntity contenant la liste des réservations (200 OK) ou message d'erreur (400 BAD REQUEST / 500 INTERNAL SERVER ERROR)
     * @throws IllegalArgumentException si l'utilisateur n'existe pas
     */
    @GetMapping("/mes-reservations")
    @Operation(
            summary = "Récupérer toutes les réservations de covoiturage de l'utilisateur connecté en tant que passager. L'affichage indique également le nombre total de places et leur occupation.")
    public ResponseEntity<?> obtenirToutesLesReservationsUtilisateur(Authentication authentication) {
        try {
            // Récupérer l'ID de l'utilisateur connecté depuis le JWT
            String emailUtilisateurConnecte = authentication.getName();
            Utilisateur utilisateur = utilisateurService.obtenirUtilisateurParEmail(emailUtilisateurConnecte);

            // Récupérer les réservations de l'utilisateur
            List<AnnonceCovoiturageAvecPlacesDto> reservations = annonceCovoiturageService.obtenirReservationsUtilisateur(utilisateur.getId());

            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    /**
     * Récupère toutes les annonces de covoiturage organisées par l'utilisateur connecté (en tant que conducteur)
     * @param authentication l'authentification JWT de l'utilisateur
     * @return liste des annonces où l'utilisateur est organisateur
     */
    @GetMapping("/mes-annonces")
    @Operation(
            summary = "Récupérer toutes les réservations de covoiturage de l'utilisateur connecté en tant que conducteur. L'affichage indique également le nombre total de places et leur occupation.")
    public ResponseEntity<?> obtenirToutesLesAnnoncesUtilisateur(Authentication authentication) {
        try {
            // Récupérer l'ID de l'utilisateur connecté depuis le JWT
            String emailUtilisateurConnecte = authentication.getName();
            Utilisateur utilisateur = utilisateurService.obtenirUtilisateurParEmail(emailUtilisateurConnecte);

            // Récupérer les annonces organisées par l'utilisateur
            List<AnnonceCovoiturageAvecPlacesDto> annonces = annonceCovoiturageService.obtenirAnnoncesOrganiseesParUtilisateur(utilisateur.getId());

            return ResponseEntity.ok(annonces);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les participants d'une annonce de covoiturage.
     * Retourne le conducteur (responsable) et la liste complète de tous les passagers ayant réservé.
     *
     * @param id l'identifiant unique de l'annonce
     * @return ResponseEntity contenant les participants (conducteur et passagers) (200 OK) ou message d'erreur (404 NOT FOUND / 500 INTERNAL SERVER ERROR)
     * @throws IllegalArgumentException si l'annonce n'existe pas
     */
    @GetMapping("/{id}/participants")
    @Operation(
            summary = "Récupérer les participants d'un covoiturage",
            description = "Retourne le conducteur et la liste des passagers pour une annonce donnée"
    )
    public ResponseEntity<?> obtenirParticipants(@PathVariable Long id) {
        try {
            ParticipantsCovoiturageDto participants = annonceCovoiturageService.obtenirParticipants(id);
            return ResponseEntity.ok(participants);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne: " + e.getMessage());
        }
    }

}