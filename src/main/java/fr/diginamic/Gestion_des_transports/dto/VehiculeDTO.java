package fr.diginamic.Gestion_des_transports.dto;

import fr.diginamic.Gestion_des_transports.enums.StatutVehicule;
import fr.diginamic.Gestion_des_transports.enums.Motorisation;
import fr.diginamic.Gestion_des_transports.enums.Categorie;
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

        @NotNull(message = "type est obligatoire (PERSONNEL ou ENTREPRISE")
        VehiculeType type,
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
) {
    // Fabrique pour un véhicule d'entreprise
    public static VehiculeDTO ofEntreprise(
            Long id,
            String immatriculation,
            String marque,
            String modele,
            Integer nbPlaces,
            StatutVehicule statut,
            Motorisation motorisation,
            Integer co2ParKm,
            String photo,
            Categorie categorie
    ) {
        return new VehiculeDTO(
                id,
                VehiculeType.ENTREPRISE,
                immatriculation,
                marque,
                modele,
                nbPlaces,
                motorisation,
                co2ParKm,
                photo,
                categorie,
                statut,
                null // utilisateurId
        );
    }

    // Fabrique pour un véhicule personnel
    public static VehiculeDTO ofPersonnel(
            Long id,
            String immatriculation,
            String marque,
            String modele,
            Integer nbPlaces,
            Motorisation motorisation,
            Integer co2ParKm,
            String photo,
            Categorie categorie,
            Long utilisateurId
    ) {
        return new VehiculeDTO(
                id,
                VehiculeType.PERSONNEL,
                immatriculation,
                marque,
                modele,
                nbPlaces,
                motorisation,
                co2ParKm,
                photo,
                categorie,
                null,   // statut
                utilisateurId
        );
    }

    // Types de véhicules couverts par ce DTO
    public enum VehiculeType { PERSONNEL, ENTREPRISE }
}
