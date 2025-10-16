package fr.diginamic.gestiondestransports.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * DTO pour l'entité AnnonceCovoiturage utilisant un Record Java
 *
 */
public record AnnonceCovoiturageDto(
        Long id,

        @NotNull(message = "L'heure de départ est obligatoire")
        @Future(message = "L'heure de départ doit être dans le futur")
        LocalDateTime heureDepart,

        @NotNull(message = "La durée du trajet est obligatoire")
        @Min(value = 1, message = "La durée du trajet doit être d'au moins 1 minute")
        @Max(value = 1440, message = "La durée du trajet ne peut pas dépasser 24 heures")
        Integer dureeTrajet,

        @NotNull(message = "La distance est obligatoire")
        @Min(value = 1, message = "La distance doit être d'au moins 1 km")
        @Max(value = 2000, message = "La distance ne peut pas dépasser 2000 km")
        Integer distance,

        @NotNull(message = "L'adresse de départ est obligatoire")
        AdresseDto adresseDepart,

        @NotNull(message = "L'adresse d'arrivée est obligatoire")
        AdresseDto adresseArrivee,

        // ID du véhicule de service (optionnel - si null, utilisera véhicule personnel)
        Long vehiculeServiceId
) {

    /**
     * Fabrique pour créer une annonce complète
     */
    public static AnnonceCovoiturageDto of(
            Long id,
            LocalDateTime heureDepart,
            Integer dureeTrajet,
            Integer distance,
            AdresseDto adresseDepart,
            AdresseDto adresseArrivee,
            Long vehiculeServiceId
    ) {
        return new AnnonceCovoiturageDto(
                id,
                heureDepart,
                dureeTrajet,
                distance,
                adresseDepart,
                adresseArrivee,
                vehiculeServiceId
        );
    }

    /**
     * Fabrique pour créer une nouvelle annonce (sans ID)
     */
    public static AnnonceCovoiturageDto nouvelle(
            LocalDateTime heureDepart,
            Integer dureeTrajet,
            Integer distance,
            AdresseDto adresseDepart,
            AdresseDto adresseArrivee,
            Long vehiculeServiceId
    ) {
        return new AnnonceCovoiturageDto(
                null,
                heureDepart,
                dureeTrajet,
                distance,
                adresseDepart,
                adresseArrivee,
                vehiculeServiceId
        );
    }

    /**
     * Retourne une description formatée de l'annonce
     */
    public String descriptionTrajet() {
        return String.format("De %s à %s le %s - %d km en %d minutes",
                adresseDepart.adresseComplete(),
                adresseArrivee.adresseComplete(),
                heureDepart.toString(),
                distance,
                dureeTrajet
        );
    }
}