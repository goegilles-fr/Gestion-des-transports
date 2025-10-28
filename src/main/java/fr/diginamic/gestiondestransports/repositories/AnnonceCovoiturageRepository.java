package fr.diginamic.gestiondestransports.repositories;

import fr.diginamic.gestiondestransports.entites.AnnonceCovoiturage;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.entites.Adresse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
/**
 * Repository JPA pour la gestion des entités AnnonceCovoiturage.
 * Fournit les opérations CRUD standard et des requêtes personnalisées pour rechercher et filtrer les annonces.
 * Permet de trouver les annonces par responsable, adresses, dates, et participants.
 * Gère également les requêtes complexes pour vérifier les conflits avec les réservations de véhicules.
 * Conforme aux règles métier du cahier des charges concernant les annonces de covoiturage.
 */
@Repository
public interface AnnonceCovoiturageRepository extends JpaRepository<AnnonceCovoiturage, Long> {
    /**
     * Recherche toutes les annonces de covoiturage créées par un utilisateur spécifique.
     * Permet à un utilisateur de consulter les covoiturages dont il est le conducteur/organisateur.
     *
     * @param responsable l'utilisateur organisateur des annonces
     * @return liste de toutes les annonces organisées par cet utilisateur
     */
    List<AnnonceCovoiturage> findByResponsable(Utilisateur responsable);
    /**
     * Recherche toutes les annonces ayant une adresse de départ donnée.
     * Utile pour les recherches de covoiturage par point de départ.
     *
     * @param adresseDepart l'adresse de départ recherchée
     * @return liste des annonces partant de cette adresse
     */
    List<AnnonceCovoiturage> findByAdresseDepart(Adresse adresseDepart);
    /**
     * Recherche toutes les annonces ayant une adresse d'arrivée donnée.
     * Utile pour les recherches de covoiturage par destination.
     *
     * @param adresseArrivee l'adresse d'arrivée recherchée
     * @return liste des annonces arrivant à cette adresse
     */
    List<AnnonceCovoiturage> findByAdresseArrivee(Adresse adresseArrivee);
    /**
     * Recherche les annonces dont l'heure de départ se situe dans une plage horaire donnée.
     * Requête JPQL pour filtrer les covoiturages par période.
     * Utile pour affiner les recherches avec un critère de date/heure.
     *
     * @param dateDebut début de la période de recherche
     * @param dateFin fin de la période de recherche
     * @return liste des annonces partant dans cette plage horaire
     */
    @Query("SELECT a FROM AnnonceCovoiturage a WHERE a.heureDepart >= :dateDebut AND a.heureDepart <= :dateFin")
    List<AnnonceCovoiturage> findByHeureDepartBetween(
            @Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);
    /**
     * Recherche les annonces correspondant à un trajet précis (départ et arrivée).
     * Requête JPQL pour trouver les covoiturages entre deux adresses exactes.
     * Fonctionnalité principale pour rechercher des covoiturages disponibles.
     *
     * @param depart l'adresse de départ
     * @param arrivee l'adresse d'arrivée
     * @return liste des annonces pour ce trajet
     */
    @Query("SELECT a FROM AnnonceCovoiturage a WHERE a.adresseDepart = :depart AND a.adresseArrivee = :arrivee")
    List<AnnonceCovoiturage> findByDepartAndArrivee(
            @Param("depart") Adresse depart, @Param("arrivee") Adresse arrivee);
    /**
     * Recherche toutes les annonces futures (dont l'heure de départ est postérieure à maintenant).
     * Requête JPQL triée par ordre chronologique croissant.
     * Permet d'afficher les covoiturages à venir pour réservation.
     *
     * @param now la date/heure actuelle de référence
     * @return liste des annonces futures triées par date de départ
     */
    @Query("SELECT a FROM AnnonceCovoiturage a WHERE a.heureDepart > :now ORDER BY a.heureDepart ASC")
    List<AnnonceCovoiturage> findFutureAnnonces(@Param("now") LocalDateTime now);

    /**
     * Recherche toutes les annonces auxquelles un utilisateur participe en tant que passager.
     * Requête JPQL utilisant la table de jointure CovoituragePassagers.
     * N'inclut pas les annonces où l'utilisateur est le conducteur.
     * Permet à un utilisateur de consulter ses réservations de covoiturage.
     *
     * @param utilisateur l'utilisateur passager
     * @return liste des annonces où l'utilisateur a réservé une place
     */
    @Query("SELECT DISTINCT cp.annonceCovoiturage FROM CovoituragePassagers cp WHERE cp.utilisateur = :utilisateur")
    List<AnnonceCovoiturage> findByUtilisateurParticipant(@Param("utilisateur") Utilisateur utilisateur);
    /**
     * Recherche toutes les annonces créées par un utilisateur à partir de son identifiant.
     * Requête JPQL optimisée en utilisant directement l'ID du responsable.
     * Alternative à findByResponsable évitant de charger l'entité Utilisateur complète.
     *
     * @param responsableId l'identifiant de l'utilisateur organisateur
     * @return liste des annonces organisées par cet utilisateur
     */
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