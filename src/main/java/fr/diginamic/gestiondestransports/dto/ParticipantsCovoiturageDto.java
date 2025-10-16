package fr.diginamic.gestiondestransports.dto;

import java.util.List;

/**
 * DTO pour représenter les participants d'un covoiturage
 * Contient le conducteur et la liste des passagers
 */
public record ParticipantsCovoiturageDto(
        PersonneDto conducteur,
        List<PersonneDto> passagers
) {
    /**
     * Représentation simplifiée d'une personne
     * Contient uniquement le nom et le prénom
     */
    public record PersonneDto(
            String nom,
            String prenom
    ) {
        /**
         * Fabrique pour créer une PersonneDto
         */
        public static PersonneDto of(String nom, String prenom) {
            return new PersonneDto(nom, prenom);
        }

        /**
         * Retourne le nom complet de la personne
         */
        public String nomComplet() {
            return prenom + " " + nom;
        }
    }

    /**
     * Fabrique pour créer un ParticipantsCovoiturageDto
     */
    public static ParticipantsCovoiturageDto of(
            PersonneDto conducteur,
            List<PersonneDto> passagers
    ) {
        return new ParticipantsCovoiturageDto(conducteur, passagers);
    }
}