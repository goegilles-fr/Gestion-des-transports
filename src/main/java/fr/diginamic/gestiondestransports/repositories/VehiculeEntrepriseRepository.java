package fr.diginamic.gestiondestransports.repositories;

import fr.diginamic.gestiondestransports.entites.VehiculeEntreprise;
import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * Repository JPA pour la gestion des entités VehiculeEntreprise (véhicules de service).
 * Fournit les opérations CRUD standard et des requêtes personnalisées pour gérer le parc automobile de l'entreprise.
 * Permet la recherche de véhicules par statut (en service, en réparation, hors service).
 * Les véhicules de service peuvent être réservés par les collaborateurs et utilisés dans les annonces de covoiturage.
 * Règle métier : seuls les administrateurs peuvent créer, modifier ou supprimer des véhicules d'entreprise.
 * Conforme aux règles métier du cahier des charges concernant la gestion des véhicules de service.
 */
public interface VehiculeEntrepriseRepository extends JpaRepository<VehiculeEntreprise, Long> {
    /**
     * Recherche tous les véhicules d'entreprise ayant un statut spécifique.
     * Méthode dérivée Spring Data JPA utilisant l'énumération StatutVehicule.
     * Statuts possibles : EN_SERVICE, EN_REPARATION, HORS_SERVICE.
     *
     * @param statut le statut recherché (StatutVehicule enum)
     * @return liste de tous les véhicules ayant ce statut
     */
    List<VehiculeEntreprise> findByStatut(StatutVehicule statut);


}
