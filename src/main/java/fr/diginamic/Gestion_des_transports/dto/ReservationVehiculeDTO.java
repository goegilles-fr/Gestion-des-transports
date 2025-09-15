package fr.diginamic.Gestion_des_transports.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ReservationVehiculeDTO(
        Long id,

        @NotNull
        Long utilisateurId,

        @NotNull
        Long vehiculeId,

        @NotNull
        @Future
        LocalDateTime dateDebut,

        @NotNull
        @Future
        LocalDateTime dateFin
) {}
