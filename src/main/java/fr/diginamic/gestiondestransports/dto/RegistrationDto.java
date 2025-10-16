package fr.diginamic.gestiondestransports.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour l'inscription d'un nouvel utilisateur
 * Contient le mot de passe (contrairement au DTO principal)
 */
public record RegistrationDto(
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

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, max = 255, message = "Le mot de passe doit contenir entre 6 et 255 caractères")
        String password,

        @Valid
        @NotNull(message = "L'adresse est obligatoire") // Add this too
        AdresseDto adresse
) {}