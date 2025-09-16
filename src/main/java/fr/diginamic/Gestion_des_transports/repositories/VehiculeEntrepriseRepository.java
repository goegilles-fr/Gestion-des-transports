package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.VehiculeEntreprise;
import fr.diginamic.Gestion_des_transports.enums.StatutVehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehiculeEntrepriseRepository extends JpaRepository<VehiculeEntreprise, Long> {
    List<VehiculeEntreprise> findByStatut(StatutVehicule statut);
}
