package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.AnnonceCovoiturage;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.entites.Adresse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnonceCovoiturageRepository extends JpaRepository<AnnonceCovoiturage, Long> {

    List<AnnonceCovoiturage> findByResponsable(Utilisateur responsable);

    List<AnnonceCovoiturage> findByAdresseDepart(Adresse adresseDepart);

    List<AnnonceCovoiturage> findByAdresseArrivee(Adresse adresseArrivee);

    @Query("SELECT a FROM AnnonceCovoiturage a WHERE a.heureDepart >= :dateDebut AND a.heureDepart <= :dateFin")
    List<AnnonceCovoiturage> findByHeureDepartBetween(
            @Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT a FROM AnnonceCovoiturage a WHERE a.adresseDepart = :depart AND a.adresseArrivee = :arrivee")
    List<AnnonceCovoiturage> findByDepartAndArrivee(
            @Param("depart") Adresse depart, @Param("arrivee") Adresse arrivee);

    @Query("SELECT a FROM AnnonceCovoiturage a WHERE a.heureDepart > :now ORDER BY a.heureDepart ASC")
    List<AnnonceCovoiturage> findFutureAnnonces(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM AnnonceCovoiturage a WHERE :utilisateur MEMBER OF a.utilisateurs")
    List<AnnonceCovoiturage> findByUtilisateurParticipant(@Param("utilisateur") Utilisateur utilisateur);

    @Query("SELECT a FROM AnnonceCovoiturage a WHERE a.responsable.id = :responsableId")
    List<AnnonceCovoiturage> findByResponsableId(@Param("responsableId") Long responsableId);
}
