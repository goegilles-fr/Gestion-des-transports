package fr.diginamic.gestiondestransports.controllers;

import fr.diginamic.gestiondestransports.dto.VehiculeDTO;
import fr.diginamic.gestiondestransports.services.VehiculeEntrepriseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
/**
 * Contrôleur REST pour la gestion des véhicules d'entreprise (véhicules de service).
 * Permet aux administrateurs de gérer le parc automobile de la société.
 * Les collaborateurs peuvent consulter les véhicules disponibles pour effectuer des réservations.
 * Gère le cycle de vie des véhicules (en service, en réparation, hors service).
 * Les modifications de statut impactent automatiquement les réservations existantes.
 * Conforme aux règles métier du cahier des charges concernant la gestion des véhicules de service.
 */
@RestController
@RequestMapping("/api/vehicules-entreprise")
@Tag(name = "Véhicules d'entreprise", description = "Gestion des véhicules de la société")
public class VehiculeEntrepriseController {

    private final VehiculeEntrepriseService service;

    public VehiculeEntrepriseController(VehiculeEntrepriseService service) {
        this.service = service;
    }
    /**
     * Récupère la liste complète de tous les véhicules d'entreprise.
     * Accessible à tous les utilisateurs authentifiés.
     * Affiche tous les véhicules quel que soit leur statut (en service, en réparation, hors service).
     *
     * @return ResponseEntity contenant la liste de tous les véhicules d'entreprise (200 OK)
     */
    @GetMapping
    @Operation(summary = "Récupérer tous les véhicules d'entreprise")
    public ResponseEntity<List<VehiculeDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Récupère les véhicules d'entreprise disponibles pour une période donnée.
     * Un véhicule est disponible si :
     * - Son statut est "en service"
     * - Il n'a aucune réservation sur la période demandée
     * Utile pour afficher les véhicules réservables dans le carrousel lors de la création d'une réservation.
     *
     * @param dateDebut la date et heure de début de la période recherchée (format ISO 8601)
     * @param dateFin la date et heure de fin de la période recherchée (format ISO 8601)
     * @return ResponseEntity contenant la liste des véhicules disponibles avec leurs caractéristiques (200 OK)
     */
    @GetMapping("/dispo")
    @Operation(summary = "Obtenir les véhicules d'entreprise disponibles pour une période donnée")
    public ResponseEntity<List<VehiculeDTO>> getVehiculesEntrepriseDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateDebut,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateFin) {

        List<VehiculeDTO> vehiculesDisponibles = service.findByAvailability(dateDebut, dateFin);

        return ResponseEntity.ok(vehiculesDisponibles);
    }

    /**
     * Récupère un véhicule d'entreprise spécifique par son identifiant.
     * Accessible à tous les utilisateurs authentifiés.
     * Affiche toutes les informations du véhicule (immatriculation, marque, modèle, catégorie, motorisation, etc.).
     *
     * @param id l'identifiant unique du véhicule
     * @return ResponseEntity contenant le véhicule demandé (200 OK) ou erreur (404 NOT FOUND)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un véhicule d'entreprise par son ID")
    public ResponseEntity<VehiculeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    /**
     * Crée un nouveau véhicule d'entreprise dans le parc automobile.
     * Accessible uniquement aux administrateurs.
     * Le statut initial est obligatoirement "en service".
     * La photo n'est pas stockée localement, il s'agit d'une URL accessible en ligne.
     *
     * @param dto les données du véhicule (immatriculation, marque, modèle, catégorie, photo URL, nbPlaces, motorisation, co2ParKm, statut)
     * @param uriBuilder constructeur d'URI pour générer la location de la ressource créée
     * @return ResponseEntity contenant le véhicule créé avec son ID (201 CREATED)
     * @throws IllegalArgumentException si les données sont invalides ou si l'immatriculation existe déjà
     */
    @PostMapping
    @Operation(summary = "Créer un nouveau véhicule d'entreprise (ADMIN uniquement)")
    public ResponseEntity<VehiculeDTO> create(@Valid @RequestBody VehiculeDTO dto,
                                              UriComponentsBuilder uriBuilder) {
        VehiculeDTO created = service.create(dto);
        URI location = uriBuilder.path("/vehicules-entreprise/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }
    /**
     * Modifie un véhicule d'entreprise existant.
     * Accessible uniquement aux administrateurs.
     * La modification du statut (en réparation, hors service) annule automatiquement les réservations en cours.
     * Des emails d'avertissement sont envoyés aux utilisateurs concernés et aux passagers des covoiturages impactés.
     * Mise à jour partielle supportée (seuls les champs fournis sont modifiés).
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @param id l'identifiant unique du véhicule à modifier
     * @param dto les nouvelles données du véhicule
     * @return ResponseEntity contenant le véhicule modifié (200 OK) ou erreur (404 NOT FOUND, 400 BAD REQUEST)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Modifier un véhicule d'entreprise existant (ADMIN uniquement, update partiel possible)")
    public ResponseEntity<VehiculeDTO> update(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                              @Valid @RequestBody VehiculeDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
    /**
     * Supprime un véhicule d'entreprise du parc automobile.
     * Accessible uniquement aux administrateurs.
     * La suppression annule toutes les réservations associées.
     * Des emails d'avertissement sont envoyés aux utilisateurs ayant des réservations et aux passagers des covoiturages.
     *
     * @param id l'identifiant unique du véhicule à supprimer
     * @return ResponseEntity vide (204 NO CONTENT) ou erreur (404 NOT FOUND)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un véhicule d'entreprise (ADMIN uniquement)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère les véhicules d'entreprise filtrés par statut.
     * Statuts possibles : EN_SERVICE, EN_REPARATION, HORS_SERVICE.
     * Utile pour les administrateurs pour consulter les véhicules nécessitant une intervention.
     *
     * @param statut le statut recherché (EN_SERVICE, EN_REPARATION, HORS_SERVICE)
     * @return ResponseEntity contenant la liste des véhicules ayant le statut spécifié (200 OK)
     */
    @GetMapping("/statut/{statut}")
    @Operation(summary = "Récupérer les véhicules d'entreprise par statut")
    public ResponseEntity<List<VehiculeDTO>> getByStatut(@PathVariable String statut) {
        return ResponseEntity.ok(service.findByStatut(statut));
    }
}
