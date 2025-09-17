package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.dto.ReservationVehiculeDTO;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.enums.RoleEnum;
import fr.diginamic.Gestion_des_transports.services.ReservationVehiculeService;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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
    public ResponseEntity<List<ReservationVehiculeDTO>> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur user = getUtilisateur(userDetails);
        checkAdmin(user);

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
        URI location = uriBuilder.path("/reservations-vehicules/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationVehiculeDTO> update(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                                         @Valid @RequestBody ReservationVehiculeDTO dto) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.update(user, id, dto));
    }

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

    @GetMapping("/vehicule/{vehiculeId}")
    public ResponseEntity<List<ReservationVehiculeDTO>> getByVehicule(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long vehiculeId) {
        Utilisateur user = getUtilisateur(userDetails);
        checkAdmin(user);

        return ResponseEntity.ok(service.findByVehiculeId(vehiculeId));
    }

    private Utilisateur getUtilisateur(UserDetails userDetails) {
        String email = userDetails.getUsername();
        Utilisateur user = Optional.ofNullable(utilisateurService.obtenirUtilisateurParEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        checkUser(user);

        return user;
    }

    private void checkUser(Utilisateur user) {
        if (user == null || !user.getEstVerifie() || user.getEstBanni()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte non vérifié ou banni");
        }
    }

    private void checkAdmin(Utilisateur user) {
        if (user.getRole() != RoleEnum.ROLE_ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte non admin");
        }
    }
}
