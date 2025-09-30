package fr.diginamic.Gestion_des_transports.mapper;

import fr.diginamic.Gestion_des_transports.dto.ModifierProfilDto;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import org.mapstruct.*;

/**
 * Mapper MapStruct pour la modification du profil utilisateur
 */
@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AdresseMapper.class})
public interface ModifierProfilMapper {

    /**
     * Met à jour une entité Utilisateur existante avec les données du DTO de modification
     * Seuls les champs non-null du DTO sont appliqués à l'entité
     *
     * @param modifierProfilDto le DTO contenant les nouvelles données (peut être partiel)
     * @param utilisateurExistant l'entité à mettre à jour
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