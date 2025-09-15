package fr.diginamic.Gestion_des_transports.mapper;

import fr.diginamic.Gestion_des_transports.dto.UtilisateurDto;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper MapStruct pour convertir entre Utilisateur et UtilisateurDto
 */
@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AdresseMapper.class})
public interface UtilisateurMapper {

    /**
     * Convertit une entité Utilisateur en UtilisateurDto
     * @param utilisateur l'entité à convertir
     * @return le DTO correspondant
     */
    // ENTITE -> DTO
    @Mapping(target = "adresse", source = "adresse")
    //@Mapping(target = "password", ignore = true) // Ignorer le mot de passe pour la sécurité
    UtilisateurDto versDto(Utilisateur utilisateur);

    /**
     * Convertit une liste d'entités Utilisateur en liste d'UtilisateurDto
     * @param utilisateurs la liste d'entités à convertir
     * @return la liste de DTOs correspondante
     */
    // ENTITE -> DTO (liste)
    List<UtilisateurDto> versDtoList(List<Utilisateur> utilisateurs);

    /**
     * Convertit un UtilisateurDto en entité Utilisateur
     * @param utilisateurDto le DTO à convertir
     * @return l'entité correspondante
     * Note: Les collections et le mot de passe sont ignorés
     */
    // DTO -> ENTITE
    @Mapping(target = "adresse", source = "adresse")
    @Mapping(target = "password", ignore = true) // Le mot de passe n'est pas mappé depuis le DTO
    @Mapping(target = "vehiculesPersonnels", ignore = true) // Géré dans le service
    @Mapping(target = "annoncesResponsables", ignore = true) // Géré dans le service
    Utilisateur versEntite(UtilisateurDto utilisateurDto);

    /**
     * Convertit une liste d'UtilisateurDto en liste d'entités Utilisateur
     * @param utilisateursDto la liste de DTOs à convertir
     * @return la liste d'entités correspondante
     */
    // DTO -> ENTITE (liste)
    List<Utilisateur> versEntiteList(List<UtilisateurDto> utilisateursDto);

    /**
     * Met à jour une entité Utilisateur existante avec les données d'un DTO
     * @param utilisateurDto le DTO contenant les nouvelles données
     * @param utilisateurExistant l'entité à mettre à jour
     * Note: Le mot de passe et les collections ne sont pas mis à jour
     */
    // DTO -> ENTITE (mise à jour)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) // Ne pas modifier l'ID
    @Mapping(target = "password", ignore = true) // Ne pas modifier le mot de passe
    @Mapping(target = "vehiculesPersonnels", ignore = true) // Géré dans le service
    @Mapping(target = "annoncesResponsables", ignore = true) // Géré dans le service
    void mettreAJourEntite(UtilisateurDto utilisateurDto, @MappingTarget Utilisateur utilisateurExistant);
}