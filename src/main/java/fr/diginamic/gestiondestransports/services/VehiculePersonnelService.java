package fr.diginamic.gestiondestransports.services;

import fr.diginamic.gestiondestransports.dto.VehiculeDTO;

import java.util.List;
/**
 * Interface de service pour la gestion des véhicules personnels des collaborateurs.
 * Définit les opérations métier pour :
 * - Créer, modifier, supprimer le véhicule personnel d'un utilisateur
 * - Consulter les véhicules personnels
 * Règle métier importante : chaque utilisateur est limité à un seul véhicule personnel.
 * Les véhicules personnels peuvent être utilisés pour créer des annonces de covoiturage.
 * Applique les règles métier du cahier des charges concernant les véhicules personnels.
 * Implémentée par VehiculePersonnelServiceImpl.
 */
public interface VehiculePersonnelService {
    List<VehiculeDTO> findAll();
    VehiculeDTO findById(Long id);
    VehiculeDTO create(Long id, VehiculeDTO dto);
    VehiculeDTO update(Long id, VehiculeDTO dto);
    void delete(Long id);
    void deleteByUtilisateurId(Long utilisateurId);
    List<VehiculeDTO> findByUtilisateurId(Long utilisateurId);
}
