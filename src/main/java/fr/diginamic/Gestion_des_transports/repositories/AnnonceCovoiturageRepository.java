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

    // Requête corrigée pour utiliser la nouvelle relation avec CovoituragePassagers
    @Query("SELECT DISTINCT cp.annonceCovoiturage FROM CovoituragePassagers cp WHERE cp.utilisateur = :utilisateur")
    List<AnnonceCovoiturage> findByUtilisateurParticipant(@Param("utilisateur") Utilisateur utilisateur);

    @Query("SELECT a FROM AnnonceCovoiturage a WHERE a.responsable.id = :responsableId")
    List<AnnonceCovoiturage> findByResponsableId(@Param("responsableId") Long responsableId);

    /**
     * Trouve toutes les annonces de covoiturage utilisant un véhicule de service donné
     * dont la période de trajet chevauche une période spécifiée.
     *
     * @param vehiculeServiceId ID du véhicule de service
     * @param dateDebut Date de début de la période à vérifier
     * @param dateFin Date de fin de la période à vérifier
     * @return Liste des annonces en conflit
     */
    /**
     * Trouve toutes les annonces de covoiturage utilisant un véhicule de service donné
     * qui démarrent dans une période donnée.
     *
     * @param vehiculeServiceId ID du véhicule de service
     * @param dateDebut Date de début de la période à vérifier
     * @param dateFin Date de fin de la période à vérifier
     * @return Liste des annonces à vérifier
     */
    @Query("SELECT a FROM AnnonceCovoiturage a WHERE a.vehiculeService.id = :vehiculeServiceId " +
            "AND a.heureDepart < :dateFin " +
            "AND a.heureDepart >= :dateDebut")
    List<AnnonceCovoiturage> findByVehiculeServiceIdBetweenDates(
            @Param("vehiculeServiceId") Long vehiculeServiceId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );
}