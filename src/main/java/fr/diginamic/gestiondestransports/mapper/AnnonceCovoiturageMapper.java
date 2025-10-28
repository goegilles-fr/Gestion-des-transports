package fr.diginamic.gestiondestransports.mapper;

import fr.diginamic.gestiondestransports.dto.AnnonceCovoiturageDto;
import fr.diginamic.gestiondestransports.entites.AnnonceCovoiturage;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper MapStruct pour convertir entre AnnonceCovoiturage et AnnonceCovoiturageDto.
 * Permet la conversion bidirectionnelle entre l'entité et son DTO.
 * Utilise AdresseMapper pour convertir les adresses imbriquées.
 */
@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AdresseMapper.class})
public interface AnnonceCovoiturageMapper {

    /**
     * Convertit une entité AnnonceCovoiturage en AnnonceCovoiturageDto.
     * Mappe automatiquement les adresses de départ et d'arrivée, et récupère l'ID du véhicule de service.
     *
     * @param annonceCovoiturage l'entité à convertir
     * @return le DTO correspondant
     */
    @Mapping(target = "adresseDepart", source = "adresseDepart")
    @Mapping(target = "adresseArrivee", source = "adresseArrivee")
    @Mapping(target = "vehiculeServiceId", source = "vehiculeService.id")
    AnnonceCovoiturageDto versDto(AnnonceCovoiturage annonceCovoiturage);

    /**
     * Convertit une liste d'entités AnnonceCovoiturage en liste d'AnnonceCovoiturageDto.
     *
     * @param annoncesCovoiturage la liste d'entités à convertir
     * @return la liste de DTOs correspondante
     */
    List<AnnonceCovoiturageDto> versDtoList(List<AnnonceCovoiturage> annoncesCovoiturage);

    /**
     * Convertit un AnnonceCovoiturageDto en entité AnnonceCovoiturage.
     * Les relations (véhicule, responsable, passagers) doivent être gérées dans la couche service.
     *
     * @param annonceDto le DTO à convertir
     * @return l'entité correspondante
     */
    @Mapping(target = "adresseDepart", source = "adresseDepart")
    @Mapping(target = "adresseArrivee", source = "adresseArrivee")
    @Mapping(target = "vehiculeService", ignore = true) // Géré dans le service
    @Mapping(target = "responsable", ignore = true) // Géré dans le service
    @Mapping(target = "passagers", ignore = true) // Géré dans le service
    AnnonceCovoiturage versEntite(AnnonceCovoiturageDto annonceDto);

    /**
     * Convertit une liste d'AnnonceCovoiturageDto en liste d'entités AnnonceCovoiturage.
     *
     * @param annoncesDto la liste de DTOs à convertir
     * @return la liste d'entités correspondante
     */
    List<AnnonceCovoiturage> versEntiteList(List<AnnonceCovoiturageDto> annoncesDto);

    /**
     * Met à jour une entité AnnonceCovoiturage existante avec les données d'un DTO.
     * Les propriétés null du DTO ne sont pas copiées. L'ID et le responsable ne sont jamais modifiés.
     * Les relations (véhicule, passagers) doivent être gérées dans la couche service.
     *
     * @param annonceDto le DTO contenant les nouvelles données
     * @param annonceExistante l'entité à mettre à jour
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) // Ne pas modifier l'ID
    @Mapping(target = "responsable", ignore = true) // Ne pas modifier le responsable
    @Mapping(target = "vehiculeService", ignore = true) // Géré dans le service
    @Mapping(target = "passagers", ignore = true) // Géré dans le service
    @Mapping(target = "adresseDepart", source = "adresseDepart")
    @Mapping(target = "adresseArrivee", source = "adresseArrivee")
    void mettreAJourEntite(AnnonceCovoiturageDto annonceDto, @MappingTarget AnnonceCovoiturage annonceExistante);
}