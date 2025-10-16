package fr.diginamic.gestiondestransports.mapper;

import fr.diginamic.gestiondestransports.dto.ReservationVehiculeDTO;
import fr.diginamic.gestiondestransports.entites.ReservationVehicule;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationVehiculeMapper {

    // ENTITE -> DTO
    @Mapping(target = "utilisateurId", source = "utilisateur.id")
    @Mapping(target = "vehiculeId", source = "vehiculeEntreprise.id")
    ReservationVehiculeDTO toDto(ReservationVehicule entity);

    List<ReservationVehiculeDTO> toDtoList(List<ReservationVehicule> list);

    // DTO -> ENTITE
    // On ignore les associations : elles sont affectées dans le service (userRepo / vehiculeEntrepriseRepo)
    @Mapping(target = "utilisateur", ignore = true)
    @Mapping(target = "vehiculeEntreprise", ignore = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "dateDebut", source = "dateDebut")
    @Mapping(target = "dateFin", source = "dateFin")
    ReservationVehicule toEntity(ReservationVehiculeDTO dto);
}
