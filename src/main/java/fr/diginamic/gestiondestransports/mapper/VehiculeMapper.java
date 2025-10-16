package fr.diginamic.gestiondestransports.mapper;

import fr.diginamic.gestiondestransports.dto.VehiculeDTO;
import fr.diginamic.gestiondestransports.entites.VehiculeEntreprise;
import fr.diginamic.gestiondestransports.entites.VehiculePersonnel;
import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehiculeMapper {

    // --------- ENTITE -> DTO ---------

    // Vehicule d'entreprise vers DTO
    @Mapping(target = "statut", source = "statut")
    @Mapping(target = "motorisation", source = "motorisation")
    @Mapping(target = "co2ParKm", source = "co2ParKm")
    @Mapping(target = "photo", source = "photo")
    @Mapping(target = "categorie", source = "categorie")
    @Mapping(target = "utilisateurId", ignore = true)
    VehiculeDTO toDto(VehiculeEntreprise v);

    List<VehiculeDTO> toDtoEntrepriseList(List<VehiculeEntreprise> list);

    // Vehicule personnel vers DTO
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "motorisation", source = "motorisation")
    @Mapping(target = "co2ParKm", source = "co2ParKm")
    @Mapping(target = "photo", source = "photo")
    @Mapping(target = "categorie", source = "categorie")
    @Mapping(target = "utilisateurId", source = "utilisateur.id")
    VehiculeDTO toDto(VehiculePersonnel v);

    List<VehiculeDTO> toDtoPersonnelList(List<VehiculePersonnel> list);

    // --------- DTO -> ENTITE ---------
    // NOTE : on ignore les associations (utilisateur, réservations). Lien à faire dans le service.

    @Mapping(target = "id", source = "id")
    @Mapping(target = "immatriculation", source = "immatriculation")
    @Mapping(target = "marque", source = "marque")
    @Mapping(target = "modele", source = "modele")
    @Mapping(target = "nbPlaces", source = "nbPlaces")
    @Mapping(target = "statut", source = "statut")
    @Mapping(target = "motorisation", source = "motorisation")
    @Mapping(target = "co2ParKm", source = "co2ParKm")
    @Mapping(target = "photo", source = "photo")
    @Mapping(target = "categorie", source = "categorie")
    VehiculeEntreprise toEntrepriseEntity(VehiculeDTO dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "immatriculation", source = "immatriculation")
    @Mapping(target = "marque", source = "marque")
    @Mapping(target = "modele", source = "modele")
    @Mapping(target = "nbPlaces", source = "nbPlaces")
    @Mapping(target = "motorisation", source = "motorisation")
    @Mapping(target = "co2ParKm", source = "co2ParKm")
    @Mapping(target = "photo", source = "photo")
    @Mapping(target = "categorie", source = "categorie")
    @Mapping(target = "utilisateur", ignore = true)
    VehiculePersonnel toPersonnelEntity(VehiculeDTO dto);

    // --------- Helpers statut ---------

    default StatutVehicule parseStatut(String statut) {
        if (statut == null) return null;
        return StatutVehicule.valueOf(statut);
    }

    default String statutToString(StatutVehicule statut) {
        return statut == null ? null : statut.name();
    }
}
