package fr.diginamic.gestiondestransports.mapper;

import fr.diginamic.gestiondestransports.dto.VehiculeDTO;
import fr.diginamic.gestiondestransports.entites.VehiculeEntreprise;
import fr.diginamic.gestiondestransports.entites.VehiculePersonnel;
import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import org.mapstruct.*;

import java.util.List;
/**
 * Mapper MapStruct pour convertir entre les entités Vehicule et VehiculeDTO.
 * Gère la conversion bidirectionnelle pour les véhicules d'entreprise et personnels.
 * Les associations (utilisateur, réservations) doivent être gérées dans la couche service.
 */
@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehiculeMapper {

    /**
     * Convertit une entité VehiculeEntreprise en VehiculeDTO.
     * Mappe le statut du véhicule d'entreprise. Le champ utilisateurId est ignoré.
     *
     * @param v l'entité véhicule d'entreprise à convertir
     * @return le DTO correspondant
     */
    @Mapping(target = "statut", source = "statut")
    @Mapping(target = "motorisation", source = "motorisation")
    @Mapping(target = "co2ParKm", source = "co2ParKm")
    @Mapping(target = "photo", source = "photo")
    @Mapping(target = "categorie", source = "categorie")
    @Mapping(target = "utilisateurId", ignore = true)
    VehiculeDTO toDto(VehiculeEntreprise v);
    /**
     * Convertit une liste d'entités VehiculeEntreprise en liste de VehiculeDTO.
     *
     * @param list la liste d'entités véhicules d'entreprise à convertir
     * @return la liste de DTOs correspondante
     */
    List<VehiculeDTO> toDtoEntrepriseList(List<VehiculeEntreprise> list);

    /**
     * Convertit une entité VehiculePersonnel en VehiculeDTO.
     * Le statut est ignoré (n'existe pas pour les véhicules personnels). Mappe l'identifiant de l'utilisateur propriétaire.
     *
     * @param v l'entité véhicule personnel à convertir
     * @return le DTO correspondant
     */
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "motorisation", source = "motorisation")
    @Mapping(target = "co2ParKm", source = "co2ParKm")
    @Mapping(target = "photo", source = "photo")
    @Mapping(target = "categorie", source = "categorie")
    @Mapping(target = "utilisateurId", source = "utilisateur.id")
    VehiculeDTO toDto(VehiculePersonnel v);
    /**
     * Convertit une liste d'entités VehiculePersonnel en liste de VehiculeDTO.
     *
     * @param list la liste d'entités véhicules personnels à convertir
     * @return la liste de DTOs correspondante
     */
    List<VehiculeDTO> toDtoPersonnelList(List<VehiculePersonnel> list);

    /**
     * Convertit un VehiculeDTO en entité VehiculeEntreprise.
     * Mappe tous les champs du véhicule incluant le statut spécifique aux véhicules d'entreprise.
     *
     * @param dto le DTO à convertir
     * @return l'entité véhicule d'entreprise correspondante
     */
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
    /**
     * Convertit un VehiculeDTO en entité VehiculePersonnel.
     * L'association avec l'utilisateur propriétaire doit être affectée dans la couche service.
     *
     * @param dto le DTO à convertir
     * @return l'entité véhicule personnel correspondante
     */
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

    /**
     * Convertit une chaîne de caractères en énumération StatutVehicule.
     *
     * @param statut la chaîne représentant le statut
     * @return l'énumération StatutVehicule correspondante, ou null si la chaîne est null
     */
    default StatutVehicule parseStatut(String statut) {
        if (statut == null) return null;
        return StatutVehicule.valueOf(statut);
    }
    /**
     * Convertit une énumération StatutVehicule en chaîne de caractères.
     *
     * @param statut l'énumération StatutVehicule
     * @return le nom du statut sous forme de chaîne, ou null si le statut est null
     */
    default String statutToString(StatutVehicule statut) {
        return statut == null ? null : statut.name();
    }
}
