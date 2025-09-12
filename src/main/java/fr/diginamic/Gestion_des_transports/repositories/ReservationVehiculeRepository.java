package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.ReservationVehicule;
import fr.diginamic.Gestion_des_transports.entites.VehiculeEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationVehiculeRepository extends JpaRepository<ReservationVehicule, Long> {

    List<ReservationVehicule> findByUtilisateurId(int utilisateurId);

    // Variante par ID (celle qu'on utilise côté service)
    List<ReservationVehicule> findByVehiculeEntrepriseId(int vehiculeEntrepriseId);

    // Variante par entité (utile si tu as déjà l'entité en main)
    List<ReservationVehicule> findByVehiculeEntreprise(VehiculeEntreprise vehiculeEntreprise);
}
