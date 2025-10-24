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
            Long id,
            String nom,
            String prenom
    ) {
        /**
         * Fabrique pour créer une PersonneDto
         */
        public static PersonneDto of(Long id, String nom, String prenom) {
            return new PersonneDto(id, nom, prenom);
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