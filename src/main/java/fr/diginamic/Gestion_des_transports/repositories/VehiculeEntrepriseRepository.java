package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.VehiculeEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehiculeEntrepriseRepository extends JpaRepository<VehiculeEntreprise, Long> {
    List<VehiculeEntreprise> findByStatut(String statut); // ou enum si tu utilises un enum
}
