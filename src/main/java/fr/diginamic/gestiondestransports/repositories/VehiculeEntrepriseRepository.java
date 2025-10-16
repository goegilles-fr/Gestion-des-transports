package fr.diginamic.gestiondestransports.repositories;

import fr.diginamic.gestiondestransports.entites.VehiculeEntreprise;
import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehiculeEntrepriseRepository extends JpaRepository<VehiculeEntreprise, Long> {
    List<VehiculeEntreprise> findByStatut(StatutVehicule statut);


}
