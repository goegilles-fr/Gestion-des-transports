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

    @GetMapping
    @Operation(summary = "Récupérer tous les véhicules personnels")
    public ResponseEntity<List<VehiculeDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un véhicule personnel par son ID")
    public ResponseEntity<VehiculeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "réer un nouveau véhicule personnel pour l'utilisateur connecté (limité à 1 véhicule par utilisateur)")

    public ResponseEntity<VehiculeDTO> create(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody VehiculeDTO dto) {
        Utilisateur user = getUtilisateur(userDetails);

        VehiculeDTO created = service.create(user.getId(), dto);

        return ResponseEntity.ok(created);
    }

    @PutMapping
    @Operation(summary = "Modifier le véhicule personnel de l'utilisateur connecté (update partiel possible)")
    public ResponseEntity<VehiculeDTO> update(@AuthenticationPrincipal UserDetails userDetails,
                                                       @Valid @RequestBody VehiculeDTO dto) {
        Utilisateur user = getUtilisateur(userDetails);

        return ResponseEntity.ok(service.update(user.getId(), dto));
    }

    @DeleteMapping
    @Operation(summary = "Supprimer le véhicule personnel de l'utilisateur connecté")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur user = getUtilisateur(userDetails);

        service.deleteByUtilisateurId(user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/utilisateur")
    @Operation(summary = "Récupérer le véhicule personnel de l'utilisateur connecté")
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
