package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.ReservationVehicule;
import fr.diginamic.Gestion_des_transports.entites.VehiculeEntreprise;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationVehiculeRepository extends JpaRepository<ReservationVehicule, Long> {

    List<ReservationVehicule> findByUtilisateurId(int utilisateur_id);
    List<ReservationVehicule> findByVehiculeEntrepriseId(int vehiculeEntreprise_id);

    // Trouver toutes les réservations d'un utilisateur
    List<ReservationVehicule> findByUtilisateur(Utilisateur utilisateur);

    // Trouver toutes les réservations d'un véhicule
    List<ReservationVehicule> findByVehiculeService(VehiculeEntreprise vehiculeService);

    // Trouver les réservations futures pour un véhicule
    @Query("SELECT r FROM ReservationVehicule r WHERE r.vehiculeEntreprise = :vehicule AND r.dateDebut > :maintenant")
    List<ReservationVehicule> findReservationsFutures(@Param("vehicule") VehiculeEntreprise vehicule, @Param("maintenant") LocalDateTime maintenant);

    // Vérifier les conflits de réservation pour un véhicule
    @Query("SELECT r FROM ReservationVehicule r WHERE r.vehiculeEntreprise = :vehicule AND " +
            "((r.dateDebut <= :dateFin AND r.dateFin >= :dateDebut))")
    List<ReservationVehicule> findConflitsReservation(
            @Param("vehicule") VehiculeEntreprise vehicule,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);
}