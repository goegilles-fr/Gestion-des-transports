package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.dto.ReservationVehiculeDTO;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.enums.RoleEnum;
import fr.diginamic.Gestion_des_transports.services.ReservationVehiculeService;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
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
public class ReservationVehiculeController {

    private final ReservationVehiculeService service;
    private final UtilisateurService utilisateurService;

    public ReservationVehiculeController(ReservationVehiculeService service, UtilisateurService utilisateurService) {
        this.service = service;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationVehiculeDTO>> getAll() {

        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationVehiculeDTO> getById(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.findById(user, id));
    }

    @PostMapping
    public ResponseEntity<ReservationVehiculeDTO> create(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ReservationVehiculeDTO dto,
                                                         UriComponentsBuilder uriBuilder) {
        Utilisateur user = getUtilisateur(userDetails);

        ReservationVehiculeDTO created = service.create(user, dto);
        URI location = uriBuilder.path("/api/reservations-vehicules/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        Utilisateur user = getUtilisateur(userDetails);

        service.delete(user, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/utilisateur")
    public ResponseEntity<List<ReservationVehiculeDTO>> getByUtilisateur(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.findByUtilisateurId(user));
    }



    /**
     * @param userDetails
     * @param dateDebut
     * @param dureeMinutes
     * @return
     * Example
     *
     * http://localhost:8080/api/reservations-vehicules/utilisateur/recherche?dateDebut=2025-09-23T10:00:00&dureeMinutes=60
     *
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






    @GetMapping("/vehicule/{vehiculeId}")
    public ResponseEntity<List<ReservationVehiculeDTO>> getByVehicule(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long vehiculeId) {

        return ResponseEntity.ok(service.findByVehiculeId(vehiculeId));
    }

    private Utilisateur getUtilisateur(UserDetails userDetails) {
        String email = userDetails.getUsername();

        return Optional.ofNullable(utilisateurService.obtenirUtilisateurParEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
