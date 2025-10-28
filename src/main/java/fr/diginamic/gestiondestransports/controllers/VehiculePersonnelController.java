package fr.diginamic.gestiondestransports.controllers;

import fr.diginamic.gestiondestransports.dto.VehiculeDTO;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.services.UtilisateurService;
import fr.diginamic.gestiondestransports.services.VehiculePersonnelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
/**
 * Contrôleur REST pour la gestion des véhicules personnels des collaborateurs.
 * Permet à chaque utilisateur de gérer son véhicule personnel (limité à un seul véhicule par utilisateur).
 * Les véhicules personnels peuvent être utilisés pour créer des annonces de covoiturage.
 * Seul le propriétaire peut consulter, modifier ou supprimer son propre véhicule.
 * Conforme aux règles métier du cahier des charges concernant les véhicules personnels.
 */
@RestController
@RequestMapping("/api/vehicules-personnels")
@Tag(name = "Véhicules personnels", description = "Gestion des véhicules des employés")
public class VehiculePersonnelController {

    private final VehiculePersonnelService service;
    private final UtilisateurService utilisateurService;

    public VehiculePersonnelController(VehiculePersonnelService service, UtilisateurService utilisateurService) {
        this.service = service;
        this.utilisateurService = utilisateurService;
    }
    /**
     * Récupère la liste complète de tous les véhicules personnels.
     * Accessible à tous les utilisateurs authentifiés.
     * Utile pour les administrateurs pour consulter l'ensemble du parc de véhicules personnels.
     *
     * @return ResponseEntity contenant la liste de tous les véhicules personnels (200 OK)
     */
    @GetMapping
    @Operation(summary = "Récupérer tous les véhicules personnels")
    public ResponseEntity<List<VehiculeDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }
    /**
     * Récupère un véhicule personnel spécifique par son identifiant.
     * Accessible à tous les utilisateurs authentifiés.
     * Affiche les informations du véhicule (immatriculation, marque, modèle, nombre de places, CO2/km).
     *
     * @param id l'identifiant unique du véhicule personnel
     * @return ResponseEntity contenant le véhicule demandé (200 OK) ou erreur (404 NOT FOUND)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un véhicule personnel par son ID")
    public ResponseEntity<VehiculeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    /**
     * Crée un nouveau véhicule personnel pour l'utilisateur authentifié.
     * Chaque utilisateur est limité à un seul véhicule personnel.
     * Si l'utilisateur possède déjà un véhicule, la création échoue.
     * Le véhicule devient automatiquement disponible pour créer des annonces de covoiturage.
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @param dto les données du véhicule (immatriculation, marque, modèle, nbPlaces, co2ParKm)
     * @return ResponseEntity contenant le véhicule créé avec son ID (200 OK)
     * @throws IllegalStateException si l'utilisateur possède déjà un véhicule personnel
     * @throws IllegalArgumentException si les données sont invalides ou si l'immatriculation existe déjà
     */
    @PostMapping
    @Operation(summary = "Créer un nouveau véhicule personnel pour l'utilisateur connecté (limité à 1 véhicule par utilisateur)")

    public ResponseEntity<VehiculeDTO> create(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody VehiculeDTO dto) {
        Utilisateur user = getUtilisateur(userDetails);

        VehiculeDTO created = service.create(user.getId(), dto);

        return ResponseEntity.ok(created);
    }
    /**
     * Modifie le véhicule personnel de l'utilisateur authentifié.
     * Seul le propriétaire peut modifier son propre véhicule.
     * Mise à jour partielle supportée (seuls les champs fournis sont modifiés).
     * Si le véhicule est utilisé dans des annonces de covoiturage actives, les passagers sont notifiés.
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @param dto les nouvelles données du véhicule
     * @return ResponseEntity contenant le véhicule modifié (200 OK) ou erreur (404 NOT FOUND si aucun véhicule)
     */
    @PutMapping
    @Operation(summary = "Modifier le véhicule personnel de l'utilisateur connecté (update partiel possible)")
    public ResponseEntity<VehiculeDTO> update(@AuthenticationPrincipal UserDetails userDetails,
                                                       @Valid @RequestBody VehiculeDTO dto) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.update(user.getId(), dto));
    }
    /**
     * Supprime le véhicule personnel de l'utilisateur authentifié.
     * Seul le propriétaire peut supprimer son propre véhicule.
     * La suppression est impossible si le véhicule est utilisé dans des annonces de covoiturage actives ou futures.
     * Si des covoiturages sont impactés, un message d'erreur liste les annonces concernées.
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @return ResponseEntity vide (204 NO CONTENT) ou erreur (404 NOT FOUND, 400 BAD REQUEST si utilisé dans des covoiturages)
     * @throws IllegalStateException si le véhicule est utilisé dans des annonces de covoiturage
     */
    @DeleteMapping
    @Operation(summary = "Supprimer le véhicule personnel de l'utilisateur connecté")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur user = getUtilisateur(userDetails);

        service.deleteByUtilisateurId(user.getId());
        return ResponseEntity.noContent().build();
    }
    /**
     * Récupère le véhicule personnel de l'utilisateur authentifié.
     * Chaque utilisateur ne peut avoir qu'un seul véhicule personnel, donc la liste contient au maximum un élément.
     * Retourne une liste vide si l'utilisateur n'a pas enregistré de véhicule.
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @return ResponseEntity contenant une liste avec le véhicule de l'utilisateur ou liste vide (200 OK)
     */
    @GetMapping("/utilisateur")
    @Operation(summary = "Récupérer le véhicule personnel de l'utilisateur connecté")
    public ResponseEntity<List<VehiculeDTO>> getByUtilisateur(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.findByUtilisateurId(user.getId()));
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
