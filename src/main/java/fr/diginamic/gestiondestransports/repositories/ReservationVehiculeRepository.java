package fr.diginamic.gestiondestransports.repositories;

import fr.diginamic.gestiondestransports.entites.ReservationVehicule;
import fr.diginamic.gestiondestransports.entites.VehiculeEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
/**
 * Repository JPA pour la gestion des entités ReservationVehicule.
 * Fournit les opérations CRUD standard et des requêtes personnalisées pour gérer les réservations de véhicules de service.
 * Permet de vérifier la disponibilité des véhicules, trouver les réservations par utilisateur ou par véhicule.
 * Gère également les requêtes complexes pour détecter les chevauchements de périodes de réservation.
 * Conforme aux règles métier du cahier des charges concernant les réservations de véhicules de service.
 */
public interface ReservationVehiculeRepository extends JpaRepository<ReservationVehicule, Long> {
    /**
     * Recherche toutes les réservations effectuées par un utilisateur spécifique.
     * Méthode dérivée Spring Data JPA utilisant l'ID de l'utilisateur.
     * Permet à un utilisateur de consulter l'historique complet de ses réservations (passées, en cours, futures).
     *
     * @param utilisateurId l'identifiant de l'utilisateur
     * @return liste de toutes les réservations de cet utilisateur
     */
    List<ReservationVehicule> findByUtilisateurId(Long utilisateurId);

    /**
     * Recherche toutes les réservations associées à un véhicule d'entreprise spécifique par son identifiant.
     * Méthode dérivée Spring Data JPA utilisant l'ID du véhicule.
     * Utilisé pour afficher l'historique d'utilisation d'un véhicule (page de modification du véhicule par l'admin).
     * Permet de voir toutes les réservations passées, en cours et futures pour ce véhicule.
     *
     * @param vehiculeEntrepriseId l'identifiant du véhicule d'entreprise
     * @return liste de toutes les réservations de ce véhicule
     */
    List<ReservationVehicule> findByVehiculeEntrepriseId(Long vehiculeEntrepriseId);

    /**
     * Recherche toutes les réservations associées à un véhicule d'entreprise spécifique.
     * Méthode dérivée Spring Data JPA utilisant l'entité complète du véhicule.
     * Variante alternative de findByVehiculeEntrepriseId utile si l'entité VehiculeEntreprise est déjà chargée en mémoire.
     *
     * @param vehiculeEntreprise l'entité véhicule d'entreprise
     * @return liste de toutes les réservations de ce véhicule
     */
    List<ReservationVehicule> findByVehiculeEntreprise(VehiculeEntreprise vehiculeEntreprise);


    /**
     * Recherche une réservation d'un utilisateur qui couvre une période spécifique.
     * Requête JPQL pour vérifier si l'utilisateur a déjà réservé un véhicule pour une période donnée.
     * Une réservation couvre la période recherchée si :
     * - Sa date de début est antérieure ou égale au début de la période recherchée
     * - Sa date de fin est postérieure ou égale à la fin de la période recherchée
     *
     * Règle métier : utilisé lors de la création d'une annonce de covoiturage avec véhicule de service
     * pour vérifier que l'utilisateur a bien une réservation valide couvrant toute la durée du trajet.
     *
     * @param utilisateurId l'identifiant de l'utilisateur
     * @param dateDebutRecherche début de la période à vérifier
     * @param dateFinRecherche fin de la période à vérifier
     * @return Optional contenant la réservation si elle existe et couvre la période, vide sinon
     */
    @Query("SELECT r FROM ReservationVehicule r WHERE r.utilisateur.id = :utilisateurId " +
            "AND r.dateDebut <= :dateDebutRecherche " +
            "AND r.dateFin >= :dateFinRecherche")
    Optional<ReservationVehicule> findByUtilisateurIdAndPeriodeCouvrante(
            @Param("utilisateurId") Long utilisateurId,
            @Param("dateDebutRecherche") LocalDateTime dateDebutRecherche,
            @Param("dateFinRecherche") LocalDateTime dateFinRecherche
    );




}
