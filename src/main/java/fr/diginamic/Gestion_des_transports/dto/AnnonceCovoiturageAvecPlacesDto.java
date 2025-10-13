package fr.diginamic.Gestion_des_transports.dto;

public record AnnonceCovoiturageAvecPlacesDto(
        AnnonceCovoiturageDto annonce,
        Integer placesTotales,
        Integer placesOccupees

) {
    // Fabrique pour créer avec calcul automatique des places disponibles
    public static AnnonceCovoiturageAvecPlacesDto of(
            AnnonceCovoiturageDto annonce,
            Integer placesTotales,
            Integer placesOccupees
    ) {
        return new AnnonceCovoiturageAvecPlacesDto(
                annonce,
                placesTotales,
                placesOccupees
        );
    }
}