package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.entites.VehiculePersonnel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehiculePersonnelRepository extends JpaRepository<VehiculePersonnel, Long> {

    List<VehiculePersonnel> findByUtilisateurId(Long utilisateurId);


    List<VehiculePersonnel> findByUtilisateur(Utilisateur utilisateur); // this must be here //
    

    boolean existsByImmatriculation(String immatriculation);


    boolean existsByUtilisateurId(Long utilisateurId);

    Optional<VehiculePersonnel> findFirstByUtilisateurId(Long utilisateurId);


    boolean existsByUtilisateurIdAndIdNot(Long utilisateurId, Long id);
}
