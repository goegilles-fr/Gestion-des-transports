package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.VehiculePersonnel;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.enums.Motorisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculePersonnelRepository extends JpaRepository<VehiculePersonnel, Long> {

    Optional<VehiculePersonnel> findByImmatriculation(String immatriculation);

    List<VehiculePersonnel> findByUtilisateur(Utilisateur utilisateur);

    List<VehiculePersonnel> findByMotorisation(Motorisation motorisation);

    List<VehiculePersonnel> findByMarque(String marque);

    @Query("SELECT v FROM VehiculePersonnel v WHERE v.nbPlaces >= :minPlaces")
    List<VehiculePersonnel> findByNbPlacesGreaterThanEqual(@Param("minPlaces") Integer minPlaces);

    @Query("SELECT v FROM VehiculePersonnel v WHERE v.utilisateur.id = :utilisateurId")
    List<VehiculePersonnel> findByUtilisateurId(@Param("utilisateurId") Long utilisateurId);

    boolean existsByImmatriculation(String immatriculation);
}
