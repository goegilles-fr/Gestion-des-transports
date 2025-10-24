package fr.diginamic.gestiondestransports.mapper;

import fr.diginamic.gestiondestransports.dto.AdresseDto;
import fr.diginamic.gestiondestransports.entites.Adresse;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper MapStruct pour convertir entre Adresse et AdresseDto.
 * Permet la conversion bidirectionnelle entre l'entité et son DTO.
 */
@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdresseMapper {

    /**
     * Convertit une entité Adresse en AdresseDto.
     *
     * @param adresse l'entité à convertir
     * @return le DTO correspondant
     */
    AdresseDto versDto(Adresse adresse);

    /**
     * Convertit une liste d'entités Adresse en liste d'AdresseDto.
     *
     * @param adresses la liste d'entités à convertir
     * @return la liste de DTOs correspondante
     */
    List<AdresseDto> versDtoList(List<Adresse> adresses);

    /**
     * Convertit un AdresseDto en entité Adresse.
     *
     * @param adresseDto le DTO à convertir
     * @return l'entité correspondante
     */
    Adresse versEntite(AdresseDto adresseDto);

    /**
     * Convertit une liste d'AdresseDto en liste d'entités Adresse.
     *
     * @param adressesDto la liste de DTOs à convertir
     * @return la liste d'entités correspondante
     */
    List<Adresse> versEntiteList(List<AdresseDto> adressesDto);

    /**
     * Met à jour une entité Adresse existante avec les données d'un DTO.
     * Les propriétés null du DTO ne sont pas copiées vers l'entité.
     *
     * @param adresseDto le DTO contenant les nouvelles données
     * @param adresseExistante l'entité à mettre à jour
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void mettreAJourEntite(AdresseDto adresseDto, @MappingTarget Adresse adresseExistante);
}