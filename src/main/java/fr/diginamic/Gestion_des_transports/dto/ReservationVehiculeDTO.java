package fr.diginamic.Gestion_des_transports.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime dateDebut,

        @NotNull
        @Future
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime dateFin
) {}
