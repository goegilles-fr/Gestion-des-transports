package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.ReservationVehicule;
import fr.diginamic.Gestion_des_transports.entites.VehiculeEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationVehiculeRepository extends JpaRepository<ReservationVehicule, Long> {

    List<ReservationVehicule> findByUtilisateurId(Long utilisateurId);

    // Variante par ID (celle qu'on utilise côté service)
    List<ReservationVehicule> findByVehiculeEntrepriseId(Long vehiculeEntrepriseId);

    // Variante par entité (utile si tu as déjà l'entité en main)
    List<ReservationVehicule> findByVehiculeEntreprise(VehiculeEntreprise vehiculeEntreprise);


    // Recherche d'une réservation pour un utilisateur qui couvre une période donnée
    @Query("SELECT r FROM ReservationVehicule r WHERE r.utilisateur.id = :utilisateurId " +
            "AND r.dateDebut <= :dateDebutRecherche " +
            "AND r.dateFin >= :dateFinRecherche")
    Optional<ReservationVehicule> findByUtilisateurIdAndPeriodeCouvrante(
            @Param("utilisateurId") Long utilisateurId,
            @Param("dateDebutRecherche") LocalDateTime dateDebutRecherche,
            @Param("dateFinRecherche") LocalDateTime dateFinRecherche
    );




}
