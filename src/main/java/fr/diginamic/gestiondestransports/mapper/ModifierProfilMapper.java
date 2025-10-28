package fr.diginamic.gestiondestransports.mapper;

import fr.diginamic.gestiondestransports.dto.ModifierProfilDto;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import org.mapstruct.*;

/**
 * Mapper MapStruct pour la modification du profil utilisateur.
 * Permet la mise à jour partielle d'un utilisateur existant sans modifier les champs sensibles.
 * Utilise AdresseMapper pour convertir les adresses imbriquées.
 */
@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AdresseMapper.class})
public interface ModifierProfilMapper {

    /**
     * Met à jour une entité Utilisateur existante avec les données du DTO de modification.
     * Seuls les champs non-null du DTO sont appliqués à l'entité.
     * Les champs sensibles (ID, email, rôle, statuts) ne peuvent pas être modifiés via ce mapper.
     *
     * @param modifierProfilDto le DTO contenant les nouvelles données (peut être partiel)
     * @param utilisateurExistant l'entité utilisateur à mettre à jour
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) // Ne jamais modifier l'ID
    @Mapping(target = "email", ignore = true) // Ne jamais modifier l'email
    @Mapping(target = "role", ignore = true) // Ne jamais modifier le rôle
    @Mapping(target = "estBanni", ignore = true) // Ne jamais modifier le statut banni
    @Mapping(target = "estVerifie", ignore = true) // Ne jamais modifier le statut vérifié
    @Mapping(target = "estSupprime", ignore = true) // Ne jamais modifier le statut supprimé
    @Mapping(target = "vehiculesPersonnels", ignore = true) // Géré séparément
    @Mapping(target = "annoncesResponsables", ignore = true) // Géré séparément
    @Mapping(target = "password", source = "motDePasse") // Mapper motDePasse vers password
    @Mapping(target = "adresse", source = "adresse")
    void mettreAJourProfil(ModifierProfilDto modifierProfilDto, @MappingTarget Utilisateur utilisateurExistant);
}