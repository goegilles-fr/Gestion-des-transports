package fr.diginamic.Gestion_des_transports.dto;

import fr.diginamic.Gestion_des_transports.enums.StatutVehicule;
import fr.diginamic.Gestion_des_transports.enums.Motorisation;
import fr.diginamic.Gestion_des_transports.enums.Categorie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO unique pour véhicules personnels et d'entreprise.
 *
 * Règles d'usage :
 * - type = PERSONNEL  => renseigner utilisateurId ; laisser statut/motorisation/co2ParKm/photoUrl/categorie à null si non pertinents.
 * - type = ENTREPRISE => renseigner statut (EN_SERVICE, EN_REPARATION, HORS_SERVICE), et éventuellement
 *                        motorisation/co2ParKm/photoUrl/categorie ; utilisateurId = null.
 * - reservations : résumé (optionnel) des réservations pour un véhicule d'entreprise.
 */
public record VehiculeDTO(
        Long id,

        @NotNull
        VehiculeType type,                      // PERSONNEL ou ENTREPRISE

        @NotBlank
        String immatriculation,

        @NotBlank
        String marque,

        @NotBlank
        String modele,

        @Positive
        Integer nombrePlaces,

        Motorisation motorisation,
        double co2ParKm,
        String photoUrl,
        Categorie categorie,// chauffeur inclus

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
            Integer nombrePlaces,
            StatutVehicule statut,
            Motorisation motorisation,
            double co2ParKm,
            String photoUrl,
            Categorie categorie
    ) {
        return new VehiculeDTO(
                id,
                VehiculeType.ENTREPRISE,
                immatriculation,
                marque,
                modele,
                nombrePlaces,
                motorisation,
                co2ParKm,
                photoUrl,
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
            Integer nombrePlaces,
            Motorisation motorisation,
            double co2ParKm,
            String photoUrl,
            Categorie categorie,
            Long utilisateurId
    ) {
        return new VehiculeDTO(
                id,
                VehiculeType.PERSONNEL,
                immatriculation,
                marque,
                modele,
                nombrePlaces,
                motorisation,
                co2ParKm,
                photoUrl,
                categorie,
                null,   // statut
                utilisateurId
        );
    }

    // Types de véhicules couverts par ce DTO
    public enum VehiculeType { PERSONNEL, ENTREPRISE }
}
