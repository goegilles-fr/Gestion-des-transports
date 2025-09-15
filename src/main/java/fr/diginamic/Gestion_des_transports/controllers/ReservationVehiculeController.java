package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.dto.ReservationVehiculeDTO;
import fr.diginamic.Gestion_des_transports.service.ReservationVehiculeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservations-vehicules")
public class ReservationVehiculeController {

    private final ReservationVehiculeService service;

    public ReservationVehiculeController(ReservationVehiculeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ReservationVehiculeDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationVehiculeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ReservationVehiculeDTO> create(@Valid @RequestBody ReservationVehiculeDTO dto,
                                                         UriComponentsBuilder uriBuilder) {
        ReservationVehiculeDTO created = service.create(dto);
        URI location = uriBuilder.path("/reservations-vehicules/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationVehiculeDTO> update(@PathVariable Long id,
                                                         @Valid @RequestBody ReservationVehiculeDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<ReservationVehiculeDTO>> getByUtilisateur(@PathVariable Long utilisateurId) {
        return ResponseEntity.ok(service.findByUtilisateurId(utilisateurId));
    }

    @GetMapping("/vehicule/{vehiculeId}")
    public ResponseEntity<List<ReservationVehiculeDTO>> getByVehicule(@PathVariable Long vehiculeId) {
        return ResponseEntity.ok(service.findByVehiculeId(vehiculeId));
    }
}
