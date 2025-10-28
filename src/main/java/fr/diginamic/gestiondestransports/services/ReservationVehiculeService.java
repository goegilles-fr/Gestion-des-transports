package fr.diginamic.gestiondestransports.services;

import fr.diginamic.gestiondestransports.dto.ReservationVehiculeDTO;
import fr.diginamic.gestiondestransports.entites.Utilisateur;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Interface de service pour la gestion des réservations de véhicules d'entreprise.
 * Définit les opérations métier pour :
 * - Créer, modifier, supprimer des réservations
 * - Consulter les réservations par utilisateur ou par véhicule
 * - Vérifier la disponibilité des véhicules
 * - Valider les périodes de réservation
 * Applique les règles métier du cahier des charges concernant les réservations de véhicules de service.
 * Implémentée par ReservationVehiculeServiceImpl.
 */
public interface ReservationVehiculeService {
    List<ReservationVehiculeDTO> findAll();
    ReservationVehiculeDTO findById(Utilisateur user, Long id);
    ReservationVehiculeDTO create(Utilisateur user, ReservationVehiculeDTO dto);
    ReservationVehiculeDTO update(Utilisateur user, Long id, ReservationVehiculeDTO dto);
    void delete(Utilisateur user, Long id);
    List<ReservationVehiculeDTO> findByUtilisateurId(Utilisateur user);
    List<ReservationVehiculeDTO> findByVehiculeId(Long vehiculeId);
    ReservationVehiculeDTO findByUtilisateurAndPeriode(Utilisateur user, LocalDateTime dateDebut, Integer dureeMinutes);
}
