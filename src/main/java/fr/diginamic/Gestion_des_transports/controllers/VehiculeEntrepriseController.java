package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.dto.VehiculeDTO;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.enums.RoleEnum;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import fr.diginamic.Gestion_des_transports.services.VehiculeEntrepriseService;
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
@RequestMapping("/api/vehicules-entreprise")
public class VehiculeEntrepriseController {

    private final VehiculeEntrepriseService service;
    private final UtilisateurService utilisateurService;

    public VehiculeEntrepriseController(VehiculeEntrepriseService service, UtilisateurService utilisateurService) {
        this.service = service;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public ResponseEntity<List<VehiculeDTO>> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        adminCheck(userDetails);
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculeDTO> getById(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        adminCheck(userDetails);
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<VehiculeDTO> create(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody VehiculeDTO dto,
                                                        UriComponentsBuilder uriBuilder) {
        adminCheck(userDetails);
        VehiculeDTO created = service.create(dto);
        URI location = uriBuilder.path("/vehicules-entreprise/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculeDTO> update(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                                        @Valid @RequestBody VehiculeDTO dto) {
        adminCheck(userDetails);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        adminCheck(userDetails);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Ex: /vehicules-entreprise/statut/EN_SERVICE
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<VehiculeDTO>> getByStatut(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String statut) {
        adminCheck(userDetails);
        return ResponseEntity.ok(service.findByStatut(statut));
    }

    private void adminCheck(UserDetails userDetails) {
        String email = userDetails.getUsername();
        Utilisateur user = Optional.ofNullable(utilisateurService.obtenirUtilisateurParEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        System.out.println(user.getRole());
        if (user == null || !user.getEstVerifie() || user.getEstBanni()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte non vérifié ou banni");
        }
        else if (user.getRole() != RoleEnum.ROLE_ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte non admin");
        }
    }
}
