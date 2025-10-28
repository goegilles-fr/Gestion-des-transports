package fr.diginamic.gestiondestransports.services;

import fr.diginamic.gestiondestransports.dto.VehiculeDTO;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Interface de service pour la gestion des véhicules d'entreprise (véhicules de service).
 * Définit les opérations métier pour :
 * - Créer, modifier, supprimer des véhicules (ADMIN uniquement)
 * - Consulter les véhicules disponibles pour une période donnée
 * - Filtrer les véhicules par statut (EN_SERVICE, EN_REPARATION, HORS_SERVICE)
 * - Gérer le cycle de vie des véhicules
 * Applique les règles métier du cahier des charges concernant le parc de véhicules de service.
 * Le changement de statut d'un véhicule impacte automatiquement les réservations existantes.
 * Implémentée par VehiculeEntrepriseServiceImpl.
 */
public interface VehiculeEntrepriseService {
    List<VehiculeDTO> findAll();
    List<VehiculeDTO> findByAvailability(LocalDateTime dateDebut, LocalDateTime dateFin);
    VehiculeDTO findById(Long id);
    VehiculeDTO create(VehiculeDTO dto);
    VehiculeDTO update(Long id, VehiculeDTO dto);
    void delete(Long id);
    List<VehiculeDTO> findByStatut(String statut);

}
