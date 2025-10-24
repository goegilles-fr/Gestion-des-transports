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

    @GetMapping
    @Operation(summary = "Obtenez toutes les réservations de voitures")
    public ResponseEntity<List<ReservationVehiculeDTO>> getAll() {

        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une réservation de voiture par identifiant. L'utilisateur ne peut obtenir que sa propre réservation, et non celle des autres.")
    public ResponseEntity<ReservationVehiculeDTO> getById(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.findById(user, id));
    }

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

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une réservation existante (propriétaire uniquement, update partiel possible)")
    public ResponseEntity<ReservationVehiculeDTO> update(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                                         @Valid @RequestBody ReservationVehiculeDTO dto) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.update(user, id, dto));
    }

    /**
     * @param userDetails
     * @param id
     * @return  Impossible de supprimer cette réservation. Le véhicule est utilisé dans 1 annonce(s) de covoiturage pendant cette période :\n- Covoiturage #12 : du 2025-10-25T10:30 au 2025-10-25T10:50\n
     *
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une réservation (impossible si le véhicule est utilisé dans un covoiturage)")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        Utilisateur user = getUtilisateur(userDetails);

        service.delete(user, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/utilisateur")
    @Operation(summary = "Récupérer toutes les réservations de l'utilisateur connecté")
    public ResponseEntity<List<ReservationVehiculeDTO>> getByUtilisateur(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.findByUtilisateurId(user));
    }




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






    @GetMapping("/vehicule/{vehiculeId}")
    @Operation(summary = "Récupérer toutes les réservations d'un véhicule spécifique")
    public ResponseEntity<List<ReservationVehiculeDTO>> getByVehicule(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long vehiculeId) {

        return ResponseEntity.ok(service.findByVehiculeId(vehiculeId));
    }

    private Utilisateur getUtilisateur(UserDetails userDetails) {
        String email = userDetails.getUsername();

        return Optional.ofNullable(utilisateurService.obtenirUtilisateurParEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
