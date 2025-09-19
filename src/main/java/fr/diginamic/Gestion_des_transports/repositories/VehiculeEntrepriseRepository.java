package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.VehiculeEntreprise;
import fr.diginamic.Gestion_des_transports.enums.StatutVehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface VehiculeEntrepriseRepository extends JpaRepository<VehiculeEntreprise, Long> {
    List<VehiculeEntreprise> findByStatut(StatutVehicule statut);

    @Query("""
       select v from VehiculeEntreprise v
       where not exists (
         select 1 from ReservationVehicule r
         where r.vehiculeEntreprise = v
           and r.dateDebut < :fin
           and r.dateFin   > :debut
       )
    """)
    List<VehiculeEntreprise> findAllAvailableBetween(LocalDateTime debut, LocalDateTime fin);
}
