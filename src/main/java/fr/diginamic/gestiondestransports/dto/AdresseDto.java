package fr.diginamic.gestiondestransports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour l'entité Adresse utilisant un Record Java
 * Utilisé pour éviter les boucles infinies et contrôler les données exposées
 */
public record AdresseDto(
        Long id,

        Integer numero,

        @NotBlank(message = "Le libellé de l'adresse est obligatoire")
        @Size(max = 255, message = "Le libellé ne peut pas dépasser 255 caractères")
        String libelle,

        @NotBlank(message = "Le code postal est obligatoire")
        @Size(max = 10, message = "Le code postal ne peut pas dépasser 10 caractères")
        String codePostal,

        @NotBlank(message = "La ville est obligatoire")
        @Size(max = 255, message = "La ville ne peut pas dépasser 255 caractères")
        String ville
) {

    /**
     * Fabrique pour créer une adresse complète
     */
    public static AdresseDto of(
            Long id,
            Integer numero,
            String libelle,
            String codePostal,
            String ville
    ) {
        return new AdresseDto(id, numero, libelle, codePostal, ville);
    }

    /**
     * Fabrique pour créer une nouvelle adresse (sans ID)
     */
    public static AdresseDto nouvelle(
            Integer numero,
            String libelle,
            String codePostal,
            String ville
    ) {
        return new AdresseDto(null, numero, libelle, codePostal, ville);
    }

    /**
     * Retourne l'adresse formatée pour l'affichage
     */
    public String adresseComplete() {
        StringBuilder sb = new StringBuilder();

        if (numero != null) {
            sb.append(numero).append(" ");
        }

        if (libelle != null && !libelle.trim().isEmpty()) {
            sb.append(libelle).append(", ");
        }

        if (codePostal != null && !codePostal.trim().isEmpty()) {
            sb.append(codePostal).append(" ");
        }

        if (ville != null && !ville.trim().isEmpty()) {
            sb.append(ville);
        }

        return sb.toString().trim();
    }
}