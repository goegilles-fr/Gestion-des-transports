package fr.diginamic.gestiondestransports.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ReservationVehiculeDTO(
        Long id,

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
