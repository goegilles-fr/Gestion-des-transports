package fr.diginamic.gestiondestransports.controllers;

import fr.diginamic.gestiondestransports.dto.ReservationVehiculeDTO;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.services.ReservationVehiculeService;
import fr.diginamic.gestiondestransports.services.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
/**
 * Contrôleur REST pour la gestion des réservations de véhicules de service.
 * Permet aux collaborateurs de réserver, consulter, modifier et annuler leurs réservations de véhicules d'entreprise.
 * Gère également les conflits avec les annonces de covoiturage utilisant ces véhicules.
 * Vérifie la disponibilité des véhicules et leur statut (en service, en réparation, hors service).
 * Conforme aux règles métier du cahier des charges concernant les réservations de véhicules de service.
 */
@RestController
@RequestMapping("/api/reservations-vehicules")
@Tag(name = "Vehicules Réservations", description = "Réservations de véhicules d'entreprise")
public class ReservationVehiculeController {

    private final ReservationVehiculeService service;
    private final UtilisateurService utilisateurService;

    public ReservationVehiculeController(ReservationVehiculeService service, UtilisateurService utilisateurService) {
        this.service = service;
        this.utilisateurService = utilisateurService;
    }

    /**
     * Récupère toutes les réservations de véhicules de service.
     * Accessible à tous les utilisateurs authentifiés.
     *
     * @return ResponseEntity contenant la liste complète de toutes les réservations de véhicules (200 OK)
     */
    @GetMapping
    @Operation(summary = "Obtenez toutes les réservations de voitures")
    public ResponseEntity<List<ReservationVehiculeDTO>> getAll() {

        return ResponseEntity.ok(service.findAll());
    }
    /**
     * Récupère une réservation de véhicule spécifique par son identifiant.
     * L'utilisateur ne peut consulter que ses propres réservations.
     * Les administrateurs peuvent consulter toutes les réservations.
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @param id l'identifiant unique de la réservation
     * @return ResponseEntity contenant la réservation demandée (200 OK) ou erreur (403 FORBIDDEN si non propriétaire, 404 NOT FOUND si inexistante)
     * @throws ResponseStatusException si l'utilisateur n'est pas autorisé à consulter cette réservation
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une réservation de voiture par identifiant. L'utilisateur ne peut obtenir que sa propre réservation, et non celle des autres.")
    public ResponseEntity<ReservationVehiculeDTO> getById(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.findById(user, id));
    }
    /**
     * Crée une nouvelle réservation de véhicule de service pour l'utilisateur authentifié.
     * Vérifie la disponibilité du véhicule sur la période demandée.
     * Seuls les véhicules avec statut "en service" peuvent être réservés.
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @param dto les données de la réservation (dateDebut, dateFin, vehiculeServiceId)
     * @param uriBuilder constructeur d'URI pour générer la location de la ressource créée
     * @return ResponseEntity contenant la réservation créée avec son ID (201 CREATED)
     * @throws IllegalArgumentException si le véhicule n'est pas disponible ou n'est pas en service
     */
    @PostMapping
    @Operation(summary = "Créer une nouvelle réservation de véhicule d'entreprise")
    public ResponseEntity<ReservationVehiculeDTO> create(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ReservationVehiculeDTO dto,
                                                         UriComponentsBuilder uriBuilder) {
        Utilisateur user = getUtilisateur(userDetails);

        ReservationVehiculeDTO created = service.create(user, dto);
        URI location = uriBuilder.path("/api/reservations-vehicules/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }
    /**
     * Modifie une réservation de véhicule existante.
     * Seul le propriétaire de la réservation peut la modifier.
     * Vérifie la disponibilité du véhicule si les dates sont modifiées.
     * Si le véhicule est rattaché à un covoiturage, un email est envoyé aux passagers.
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @param id l'identifiant unique de la réservation à modifier
     * @param dto les nouvelles données de la réservation
     * @return ResponseEntity contenant la réservation modifiée (200 OK) ou erreur (403 FORBIDDEN si non propriétaire, 404 NOT FOUND, 400 BAD REQUEST si conflit de disponibilité)
     * @throws IllegalArgumentException si le véhicule n'est pas disponible aux nouvelles dates
     */
    @PutMapping("/{id}")
    @Operation(summary = "Modifier une réservation existante (propriétaire uniquement, update partiel possible)")
    public ResponseEntity<ReservationVehiculeDTO> update(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                                         @Valid @RequestBody ReservationVehiculeDTO dto) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.update(user, id, dto));
    }

    /**
     * Supprime une réservation de véhicule.
     * Seul le propriétaire de la réservation peut la supprimer.
     * La suppression est impossible si le véhicule est utilisé dans une annonce de covoiturage active.
     * Si le véhicule est rattaché à un covoiturage, un email d'avertissement est envoyé à tous les passagers.
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @param id l'identifiant unique de la réservation à supprimer
     * @return ResponseEntity vide (204 NO CONTENT) ou erreur avec message détaillé listant les covoiturages en conflit (400 BAD REQUEST)
     * @throws IllegalStateException si le véhicule est utilisé dans un ou plusieurs covoiturages avec message détaillant les annonces concernées
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une réservation (impossible si le véhicule est utilisé dans un covoiturage)")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        Utilisateur user = getUtilisateur(userDetails);

        service.delete(user, id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Récupère toutes les réservations de véhicules de l'utilisateur authentifié.
     * Inclut les réservations en cours, futures et passées (historique complet).
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @return ResponseEntity contenant la liste des réservations de l'utilisateur (200 OK)
     */
    @GetMapping("/utilisateur")
    @Operation(summary = "Récupérer toutes les réservations de l'utilisateur connecté")
    public ResponseEntity<List<ReservationVehiculeDTO>> getByUtilisateur(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.findByUtilisateurId(user));
    }



    /**
     * Recherche une réservation de véhicule de l'utilisateur pour une période spécifique.
     * Permet de vérifier si l'utilisateur a déjà une réservation active à une date donnée.
     * Utile pour la création d'annonces de covoiturage avec véhicule de service.
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @param dateDebut la date et heure de début de la période recherchée
     * @param dureeMinutes la durée en minutes de la période
     * @return ResponseEntity contenant la réservation trouvée (200 OK) ou vide (404 NOT FOUND)
     */
    @GetMapping("/utilisateur/recherche")
    @Operation(
            summary = "Obtenir la réservation de voiture de société pour l'utilisateur connecté pour la date et la durée spécifiées")
    public ResponseEntity<ReservationVehiculeDTO> getByUtilisateurAndPeriode(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam Integer dureeMinutes) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.findByUtilisateurAndPeriode(user, dateDebut, dureeMinutes));
    }





    /**
     * Récupère toutes les réservations associées à un véhicule spécifique.
     * Utile pour les administrateurs pour consulter l'historique d'utilisation d'un véhicule.
     * Affiche les réservations passées, en cours et futures.
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @param vehiculeId l'identifiant unique du véhicule de service
     * @return ResponseEntity contenant la liste des réservations du véhicule (200 OK)
     */
    @GetMapping("/vehicule/{vehiculeId}")
    @Operation(summary = "Récupérer toutes les réservations d'un véhicule spécifique")
    public ResponseEntity<List<ReservationVehiculeDTO>> getByVehicule(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long vehiculeId) {

        return ResponseEntity.ok(service.findByVehiculeId(vehiculeId));
    }
    /**
     * Méthode utilitaire privée pour récupérer l'entité Utilisateur à partir des détails d'authentification.
     * Extrait l'email du UserDetails et charge l'utilisateur complet depuis la base de données.
     *
     * @param userDetails les détails de l'utilisateur authentifié contenant l'email
     * @return l'entité Utilisateur complète
     * @throws ResponseStatusException (401 UNAUTHORIZED) si l'utilisateur n'existe pas en base
     */
    private Utilisateur getUtilisateur(UserDetails userDetails) {
        String email = userDetails.getUsername();

        return Optional.ofNullable(utilisateurService.obtenirUtilisateurParEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
