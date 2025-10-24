package fr.diginamic.gestiondestransports.mapper;

import fr.diginamic.gestiondestransports.dto.UtilisateurDto;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper MapStruct pour convertir entre Utilisateur et UtilisateurDto.
 * Permet la conversion bidirectionnelle entre l'entité et son DTO.
 * Utilise AdresseMapper pour convertir les adresses imbriquées.
 * Le mot de passe est géré avec précaution pour des raisons de sécurité.
 */
@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AdresseMapper.class})
public interface UtilisateurMapper {

    /**
     * Convertit une entité Utilisateur en UtilisateurDto.
     * Mappe automatiquement l'adresse associée.
     *
     * @param utilisateur l'entité à convertir
     * @return le DTO correspondant
     */
    @Mapping(target = "adresse", source = "adresse")
    //@Mapping(target = "password", ignore = true) // Ignorer le mot de passe pour la sécurité
    UtilisateurDto versDto(Utilisateur utilisateur);

    /**
     * Convertit une liste d'entités Utilisateur en liste d'UtilisateurDto.
     *
     * @param utilisateurs la liste d'entités à convertir
     * @return la liste de DTOs correspondante
     */
    List<UtilisateurDto> versDtoList(List<Utilisateur> utilisateurs);

    /**
     * Convertit un UtilisateurDto en entité Utilisateur.
     * Le mot de passe et les collections (véhicules, annonces) doivent être gérés dans la couche service.
     *
     * @param utilisateurDto le DTO à convertir
     * @return l'entité correspondante
     */
    @Mapping(target = "adresse", source = "adresse")
    @Mapping(target = "password", ignore = true) // Le mot de passe n'est pas mappé depuis le DTO
    @Mapping(target = "vehiculesPersonnels", ignore = true) // Géré dans le service
    @Mapping(target = "annoncesResponsables", ignore = true) // Géré dans le service
    Utilisateur versEntite(UtilisateurDto utilisateurDto);

    /**
     * Convertit une liste d'UtilisateurDto en liste d'entités Utilisateur.
     *
     * @param utilisateursDto la liste de DTOs à convertir
     * @return la liste d'entités correspondante
     */
    List<Utilisateur> versEntiteList(List<UtilisateurDto> utilisateursDto);

    /**
     * Met à jour une entité Utilisateur existante avec les données d'un DTO.
     * Les propriétés null du DTO ne sont pas copiées. L'ID, le mot de passe et les collections ne sont jamais modifiés.
     *
     * @param utilisateurDto le DTO contenant les nouvelles données
     * @param utilisateurExistant l'entité à mettre à jour
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) // Ne pas modifier l'ID
    @Mapping(target = "password", ignore = true) // Ne pas modifier le mot de passe
    @Mapping(target = "vehiculesPersonnels", ignore = true) // Géré dans le service
    @Mapping(target = "annoncesResponsables", ignore = true) // Géré dans le service
    void mettreAJourEntite(UtilisateurDto utilisateurDto, @MappingTarget Utilisateur utilisateurExistant);
}