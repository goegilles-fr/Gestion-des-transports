package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.VehiculeService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehiculeServiceRepository extends JpaRepository<VehiculeService, Long> {

    Optional<VehiculeService> findByImmatriculation(String immatriculation);


}