package fr.diginamic.gestiondestransports.mapper;

import fr.diginamic.gestiondestransports.dto.ReservationVehiculeDTO;
import fr.diginamic.gestiondestransports.entites.ReservationVehicule;
import org.mapstruct.*;

import java.util.List;
/**
 * Mapper MapStruct pour convertir entre ReservationVehicule et ReservationVehiculeDTO.
 * Permet la conversion bidirectionnelle entre l'entité et son DTO.
 * Les associations avec l'utilisateur et le véhicule sont gérées par la couche service.
 */
@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationVehiculeMapper {

    /**
     * Convertit une entité ReservationVehicule en ReservationVehiculeDTO.
     * Mappe les identifiants de l'utilisateur et du véhicule d'entreprise.
     *
     * @param entity l'entité à convertir
     * @return le DTO correspondant
     */
    @Mapping(target = "utilisateurId", source = "utilisateur.id")
    @Mapping(target = "vehiculeId", source = "vehiculeEntreprise.id")
    ReservationVehiculeDTO toDto(ReservationVehicule entity);
    /**
     * Convertit une liste d'entités ReservationVehicule en liste de ReservationVehiculeDTO.
     *
     * @param list la liste d'entités à convertir
     * @return la liste de DTOs correspondante
     */
    List<ReservationVehiculeDTO> toDtoList(List<ReservationVehicule> list);

    /**
     * Convertit un ReservationVehiculeDTO en entité ReservationVehicule.
     * Les associations (utilisateur, véhicule) doivent être affectées dans la couche service.
     *
     * @param dto le DTO à convertir
     * @return l'entité correspondante
     */
    @Mapping(target = "utilisateur", ignore = true)
    @Mapping(target = "vehiculeEntreprise", ignore = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "dateDebut", source = "dateDebut")
    @Mapping(target = "dateFin", source = "dateFin")
    ReservationVehicule toEntity(ReservationVehiculeDTO dto);
}
