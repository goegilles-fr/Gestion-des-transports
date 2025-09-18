package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.dto.VehiculeDTO;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import fr.diginamic.Gestion_des_transports.services.VehiculePersonnelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicules-personnels")
public class VehiculePersonnelController {

    private final VehiculePersonnelService service;
    private final UtilisateurService utilisateurService;

    public VehiculePersonnelController(VehiculePersonnelService service, UtilisateurService utilisateurService) {
        this.service = service;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public ResponseEntity<List<VehiculeDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<VehiculeDTO> create(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody VehiculeDTO dto) {
        Utilisateur user = getUtilisateur(userDetails);

        VehiculeDTO created = service.create(user.getId(), dto);

        return ResponseEntity.ok(created);
    }

    @PutMapping
    public ResponseEntity<VehiculeDTO> update(@AuthenticationPrincipal UserDetails userDetails,
                                                       @Valid @RequestBody VehiculeDTO dto) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.update(user.getId(), dto));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur user = getUtilisateur(userDetails);

        service.deleteByUtilisateurId(user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/utilisateur")
    public ResponseEntity<List<VehiculeDTO>> getByUtilisateur(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.findByUtilisateurId(user.getId()));
    }

    private Utilisateur getUtilisateur(UserDetails userDetails) {
        String email = userDetails.getUsername();

        return Optional.ofNullable(utilisateurService.obtenirUtilisateurParEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
