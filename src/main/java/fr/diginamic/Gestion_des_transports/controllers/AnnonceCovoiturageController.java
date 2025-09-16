package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.dto.AnnonceCovoiturageAvecPlacesDto;
import fr.diginamic.Gestion_des_transports.dto.AnnonceCovoiturageDto;
import fr.diginamic.Gestion_des_transports.services.AnnonceCovoiturageService;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
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
@CrossOrigin(origins = "*")
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
     * Crée une nouvelle annonce de covoiturage
     * @param annonceDto les données de l'annonce
     * @param authentication l'authentification JWT de l'utilisateur
     * @return l'annonce créée avec son ID
     */
    @PostMapping("/create")
    @Operation(
            summary = "Créer une annonce. vehiculeServiceId peut être nul")
    public ResponseEntity<AnnonceCovoiturageDto> creerAnnonce(
            @Valid @RequestBody AnnonceCovoiturageDto annonceDto,
            Authentication authentication) {

        try {
            // Récupérer l'ID de l'utilisateur connecté depuis le JWT
            Long idUtilisateurConnecte = utilisateurService.obtenirUtilisateurParEmail(authentication.getName()).getId();

            // Créer l'annonce
            AnnonceCovoiturageDto annonceCree = annonceCovoiturageService.creerAnnonce(annonceDto, idUtilisateurConnecte);

            return ResponseEntity.status(HttpStatus.CREATED).body(annonceCree);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Modifie une annonce de covoiturage existante
     * @param id l'ID de l'annonce à modifier
     * @param annonceDto les nouvelles données de l'annonce
     * @param authentication l'authentification JWT de l'utilisateur
     * @return l'annonce modifiée
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
     * Supprime une annonce de covoiturage
     * @param id l'ID de l'annonce à supprimer
     * @param authentication l'authentification JWT de l'utilisateur
     * @return confirmation de suppression
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
     * Récupère une annonce de covoiturage par son ID
     * @param id l'ID de l'annonce
     * @return l'annonce trouvée
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

            AnnonceCovoiturageAvecPlacesDto response = AnnonceCovoiturageAvecPlacesDto.of(
                    annonce, placesTotales, placesOccupees
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Réserve une place dans un covoiturage
     * @param id l'ID de l'annonce de covoiturage
     * @param authentication l'authentification JWT de l'utilisateur
     * @return confirmation de réservation
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
     * Annule la réservation d'une place dans un covoiturage
     * @param id l'ID de l'annonce de covoiturage
     * @param authentication l'authentification JWT de l'utilisateur
     * @return confirmation d'annulation
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
     * Récupère toutes les annonces de covoiturage
     * @return liste de toutes les annonces
     */
    @GetMapping("/")
    @Operation(
            summary = "Récupérer toutes les annonces de covoiturage")


    public ResponseEntity<List<AnnonceCovoiturageAvecPlacesDto>> obtenirToutesLesAnnonces() {
        try {
            List<AnnonceCovoiturageAvecPlacesDto> annonces = annonceCovoiturageService.obtenirToutesLesAnnonces();
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}