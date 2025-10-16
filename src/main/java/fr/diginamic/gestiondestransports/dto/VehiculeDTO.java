package fr.diginamic.gestiondestransports.dto;

import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import fr.diginamic.gestiondestransports.enums.Motorisation;
import fr.diginamic.gestiondestransports.enums.Categorie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO unique pour véhicules personnels et d'entreprise.
 *
 * Règles d'usage :
 * - type = PERSONNEL  => renseigner utilisateurId ; laisser statut/motorisation/co2ParKm/photoUrl/categorie à null si non pertinents.
 * - type = ENTREPRISE => renseigner statut (EN_SERVICE, EN_REPARATION, HORS_SERVICE), et éventuellement
 *                        motorisation/co2ParKm/photoUrl/categorie ; utilisateurId = null.
 */
public record VehiculeDTO(
        Long id,

        @NotBlank(message = "immatriculation est obligatoire")
        String immatriculation,
        @NotBlank(message = "marque est obligatoire")
        String marque,
        @NotBlank(message = "modele est obligatoire")
        String modele,
        @NotNull(message = "nbPlaces est obligatoire")
        @Min(value = 1, message = "nbPlaces doit etre >= 1")
        Integer nbPlaces,

        Motorisation motorisation,
        @PositiveOrZero(message = "co2ParKm est obligatoire")
        Integer co2ParKm,
        String photo,
        Categorie categorie,

        // --- Spécifique ENTREPRISE (optionnel) ---
        StatutVehicule statut,

        // --- Spécifique PERSONNEL (optionnel) ---
        Long utilisateurId
) {}
