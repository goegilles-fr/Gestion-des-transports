package fr.diginamic.Gestion_des_transports.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la modification du profil utilisateur
 * Contient uniquement les champs que l'utilisateur peut modifier lui-même
 */
public record ModifierProfilDto(
        @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
        String nom,

        @Size(max = 255, message = "Le prénom ne peut pas dépasser 255 caractères")
        String prenom,

        @Valid
        AdresseDto adresse,

        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        @Size(max = 255, message = "Le mot de passe ne peut pas dépasser 255 caractères")
        String motDePasse
) {
    /**
     * Fabrique pour créer une modification complète du profil
     */
    public static ModifierProfilDto complet(
            String nom,
            String prenom,
            AdresseDto adresse,
            String motDePasse
    ) {
        return new ModifierProfilDto(nom, prenom, adresse, motDePasse);
    }

    /**
     * Fabrique pour modifier seulement les informations personnelles
     */
    public static ModifierProfilDto informationsPersonnelles(
            String nom,
            String prenom
    ) {
        return new ModifierProfilDto(nom, prenom, null, null);
    }

    /**
     * Fabrique pour modifier seulement l'adresse
     */
    public static ModifierProfilDto adresseSeule(AdresseDto adresse) {
        return new ModifierProfilDto(null, null, adresse, null);
    }

    /**
     * Fabrique pour modifier seulement le mot de passe
     */
    public static ModifierProfilDto motDePasseSeul(String motDePasse) {
        return new ModifierProfilDto(null, null, null, motDePasse);
    }

    /**
     * Vérifie si au moins un champ est fourni pour la mise à jour
     */
    public boolean aDesChampsPourMiseAJour() {
        return nom != null || prenom != null || adresse != null || motDePasse != null;
    }
}