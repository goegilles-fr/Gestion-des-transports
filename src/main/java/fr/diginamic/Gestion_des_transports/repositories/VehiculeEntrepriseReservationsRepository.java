package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.VehiculeEntrepriseReservations;
import fr.diginamic.Gestion_des_transports.entites.VehiculeEntreprise;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VehiculeEntrepriseReservationsRepository extends JpaRepository<VehiculeEntrepriseReservations, Long> {

    // Trouver toutes les réservations d'un utilisateur
    List<VehiculeEntrepriseReservations> findByUtilisateur(Utilisateur utilisateur);

    // Trouver toutes les réservations d'un véhicule
    List<VehiculeEntrepriseReservations> findByVehiculeService(VehiculeEntreprise vehiculeService);

    // Trouver les réservations en cours pour un utilisateur
    @Query("SELECT r FROM VehiculeEntrepriseReservations r WHERE r.utilisateur = :utilisateur AND r.dateFin >= :maintenant")
    List<VehiculeEntrepriseReservations> findReservationsEnCours(@Param("utilisateur") Utilisateur utilisateur, @Param("maintenant") LocalDateTime maintenant);

    // Trouver les réservations futures pour un véhicule
    @Query("SELECT r FROM VehiculeEntrepriseReservations r WHERE r.vehiculeService = :vehicule AND r.dateDebut > :maintenant")
    List<VehiculeEntrepriseReservations> findReservationsFutures(@Param("vehicule") VehiculeEntreprise vehicule, @Param("maintenant") LocalDateTime maintenant);

    // Vérifier les conflits de réservation pour un véhicule
    @Query("SELECT r FROM VehiculeEntrepriseReservations r WHERE r.vehiculeService = :vehicule AND " +
            "((r.dateDebut <= :dateFin AND r.dateFin >= :dateDebut))")
    List<VehiculeEntrepriseReservations> findConflitsReservation(
            @Param("vehicule") VehiculeEntreprise vehicule,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);

    // Trouver l'historique des réservations d'un utilisateur
    @Query("SELECT r FROM VehiculeEntrepriseReservations r WHERE r.utilisateur = :utilisateur AND r.dateFin < :maintenant ORDER BY r.dateFin DESC")
    List<VehiculeEntrepriseReservations> findHistoriqueReservations(@Param("utilisateur") Utilisateur utilisateur, @Param("maintenant") LocalDateTime maintenant);
}