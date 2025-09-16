package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.dto.VehiculeDTO;
import fr.diginamic.Gestion_des_transports.services.VehiculeEntrepriseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/vehicules-entreprise")
public class VehiculeEntrepriseController {

    private final VehiculeEntrepriseService service;

    public VehiculeEntrepriseController(VehiculeEntrepriseService service) {
        this.service = service;
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
    public ResponseEntity<VehiculeDTO> create(@Valid @RequestBody VehiculeDTO dto,
                                                        UriComponentsBuilder uriBuilder) {
        VehiculeDTO created = service.create(dto);
        URI location = uriBuilder.path("/vehicules-entreprise/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculeDTO> update(@PathVariable Long id,
                                                        @Valid @RequestBody VehiculeDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Ex: /vehicules-entreprise/statut/EN_SERVICE
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<VehiculeDTO>> getByStatut(@PathVariable String statut) {
        return ResponseEntity.ok(service.findByStatut(statut));
    }
}
