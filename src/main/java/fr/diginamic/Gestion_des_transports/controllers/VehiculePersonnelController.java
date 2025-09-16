package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.dto.VehiculeDTO;
import fr.diginamic.Gestion_des_transports.services.VehiculePersonnelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/vehicules-personnels")
public class VehiculePersonnelController {

    private final VehiculePersonnelService service;

    public VehiculePersonnelController(VehiculePersonnelService service) {
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
        URI location = uriBuilder.path("/vehicules-personnels/{id}")
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

    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<VehiculeDTO>> getByUtilisateur(@PathVariable Long utilisateurId) {
        return ResponseEntity.ok(service.findByUtilisateurId(utilisateurId));
    }
}
