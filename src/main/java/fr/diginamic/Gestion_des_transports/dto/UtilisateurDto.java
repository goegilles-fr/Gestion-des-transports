package fr.diginamic.Gestion_des_transports.dto;

import fr.diginamic.Gestion_des_transports.enums.RoleEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour l'entité Utilisateur utilisant un Record Java
 * Utilisé pour éviter les boucles infinies et contrôler les données exposées
 * Le mot de passe n'est pas inclus pour des raisons de sécurité
 */
public record UtilisateurDto(
        Long id,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 255, message = "Le prénom ne peut pas dépasser 255 caractères")
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être valide")
        @Size(max = 255, message = "L'email ne peut pas dépasser 255 caractères")
        String email,

        @Valid
        AdresseDto adresse,

        @NotNull(message = "Le rôle est obligatoire")
        RoleEnum role,

        Boolean estBanni,
        Boolean estVerifie,
        Boolean estSupprime
) {

    /**
     * Fabrique pour créer un utilisateur complet
     */
    public static UtilisateurDto of(
            Long id,
            String nom,
            String prenom,
            String email,
            AdresseDto adresse,
            RoleEnum role,
            Boolean estBanni,
            Boolean estVerifie,
            Boolean estSupprime
    ) {
        return new UtilisateurDto(
                id, nom, prenom, email, adresse, role,
                estBanni, estVerifie, estSupprime
        );
    }

    /**
     * Fabrique pour créer un nouvel utilisateur (sans ID, avec valeurs par défaut)
     */
    public static UtilisateurDto nouveau(
            String nom,
            String prenom,
            String email,
            AdresseDto adresse,
            RoleEnum role
    ) {
        return new UtilisateurDto(
                null, nom, prenom, email, adresse, role,
                false, // estBanni = false par défaut
                false, // estVerifie = false par défaut
                false  // estSupprime = false par défaut
        );
    }

    /**
     * Fabrique pour créer un utilisateur collaborateur par défaut
     */
    public static UtilisateurDto collaborateur(
            String nom,
            String prenom,
            String email,
            AdresseDto adresse
    ) {
        return nouveau(nom, prenom, email, adresse, RoleEnum.ROLE_USER);
    }

    /**
     * Fabrique pour créer un utilisateur administrateur
     */
    public static UtilisateurDto administrateur(
            String nom,
            String prenom,
            String email,
            AdresseDto adresse
    ) {
        return nouveau(nom, prenom, email, adresse, RoleEnum.ROLE_ADMIN);
    }

    /**
     * Retourne le nom complet de l'utilisateur
     */
    public String nomComplet() {
        return prenom + " " + nom;
    }

    /**
     * Vérifie si l'utilisateur est actif (non banni et non supprimé)
     */
    public boolean estActif() {
        return !Boolean.TRUE.equals(estBanni) && !Boolean.TRUE.equals(estSupprime);
    }

    /**
     * Vérifie si l'utilisateur peut se connecter (actif et vérifié)
     */
    public boolean peutSeReserver() {
        return estActif() && Boolean.TRUE.equals(estVerifie);
    }
}