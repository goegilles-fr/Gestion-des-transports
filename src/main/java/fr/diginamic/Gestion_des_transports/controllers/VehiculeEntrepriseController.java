package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.dto.VehiculeDTO;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.enums.RoleEnum;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import fr.diginamic.Gestion_des_transports.services.VehiculeEntrepriseService;
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
@RequestMapping("/api/vehicules-entreprise")
@Tag(name = "Véhicules d'entreprise", description = "Gestion des véhicules de la société")
public class VehiculeEntrepriseController {

    private final VehiculeEntrepriseService service;

    public VehiculeEntrepriseController(VehiculeEntrepriseService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les véhicules d'entreprise")
    public ResponseEntity<List<VehiculeDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * @param dateDebut
     * @param dateFin
     * @return
     * Example
     *      /api/vehicules-entreprise/dispo?dateDebut=2025-09-23T17:00:00&dateFin=2025-09-24T18:00:00
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


    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un véhicule d'entreprise par son ID")
    public ResponseEntity<VehiculeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau véhicule d'entreprise (ADMIN uniquement)")
    public ResponseEntity<VehiculeDTO> create(@Valid @RequestBody VehiculeDTO dto,
                                              UriComponentsBuilder uriBuilder) {
        VehiculeDTO created = service.create(dto);
        URI location = uriBuilder.path("/vehicules-entreprise/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un véhicule d'entreprise existant (ADMIN uniquement, update partiel possible)")
    public ResponseEntity<VehiculeDTO> update(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                              @Valid @RequestBody VehiculeDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un véhicule d'entreprise (ADMIN uniquement)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Ex: /vehicules-entreprise/statut/EN_SERVICE
    @GetMapping("/statut/{statut}")
    @Operation(summary = "Récupérer les véhicules d'entreprise par statut")
    public ResponseEntity<List<VehiculeDTO>> getByStatut(@PathVariable String statut) {
        return ResponseEntity.ok(service.findByStatut(statut));
    }
}
