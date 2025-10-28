package fr.diginamic.gestiondestransports.repositories;

import fr.diginamic.gestiondestransports.entites.CovoituragePassagers;
import fr.diginamic.gestiondestransports.entites.AnnonceCovoiturage;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
/**
 * Repository JPA pour la gestion de la table de jointure CovoituragePassagers.
 * Gère la relation many-to-many entre les utilisateurs (passagers) et les annonces de covoiturage.
 * Permet de réserver/annuler des places, compter les passagers, et vérifier les inscriptions existantes.
 * Essentiel pour la gestion des places disponibles et occupées dans les covoiturages.
 * Conforme aux règles métier du cahier des charges concernant les réservations de covoiturage.
 */
@Repository
public interface CovoituragePassagersRepository extends JpaRepository<CovoituragePassagers, Long> {

    /**
     * Recherche tous les passagers inscrits à une annonce de covoiturage donnée.
     * Permet de lister les participants d'un covoiturage.
     * Utilisé pour afficher les détails d'une annonce avec la liste complète des passagers.
     *
     * @param annonceCovoiturage l'annonce de covoiturage
     * @return liste de toutes les relations passagers pour cette annonce
     */
    List<CovoituragePassagers> findByAnnonceCovoiturage(AnnonceCovoiturage annonceCovoiturage);

    /**
     * Recherche tous les covoiturages auxquels un utilisateur participe en tant que passager.
     * N'inclut pas les annonces où l'utilisateur est le conducteur/organisateur.
     * Permet à un utilisateur de consulter ses réservations.
     *
     * @param utilisateur l'utilisateur passager
     * @return liste de toutes les relations de participation de cet utilisateur
     */
    List<CovoituragePassagers> findByUtilisateur(Utilisateur utilisateur);

    /**
     * Vérifie si un utilisateur est déjà inscrit comme passager à un covoiturage spécifique.
     * Règle métier : un utilisateur ne peut pas réserver deux fois la même annonce.
     * Utilisé avant de créer une nouvelle réservation pour éviter les doublons.
     *
     * @param utilisateur l'utilisateur à vérifier
     * @param annonceCovoiturage l'annonce de covoiturage
     * @return Optional contenant la relation si elle existe, vide sinon
     */
    Optional<CovoituragePassagers> findByUtilisateurAndAnnonceCovoiturage(
            Utilisateur utilisateur, AnnonceCovoiturage annonceCovoiturage);

    /**
     * Supprime la réservation d'un passager pour une annonce de covoiturage.
     * Libère une place dans le covoiturage.
     * Utilisé lors de l'annulation d'une réservation par un passager.
     * Attention : méthode de suppression personnalisée nécessitant @Transactional dans le service.
     *
     * @param utilisateur l'utilisateur passager
     * @param annonceCovoiturage l'annonce de covoiturage
     */
    void deleteByUtilisateurAndAnnonceCovoiturage(Utilisateur utilisateur, AnnonceCovoiturage annonceCovoiturage);

    /**
     * Compte le nombre de passagers inscrits à un covoiturage.
     * Requête JPQL pour calculer les places occupées.
     * Utilisé pour vérifier la disponibilité avant d'accepter une nouvelle réservation.
     * Permet d'afficher le nombre de places restantes (places totales - places occupées).
     *
     * @param annonceCovoiturage l'annonce de covoiturage
     * @return le nombre de passagers inscrits
     */
    @Query("SELECT COUNT(cp) FROM CovoituragePassagers cp WHERE cp.annonceCovoiturage = :annonceCovoiturage")
    Long countPassagersParCovoiturage(@Param("annonceCovoiturage") AnnonceCovoiturage annonceCovoiturage);

    /**
     * Recherche tous les covoiturages auxquels un utilisateur participe à partir de son identifiant.
     * Requête JPQL optimisée utilisant directement l'ID de l'utilisateur.
     * Alternative à findByUtilisateur évitant de charger l'entité Utilisateur complète.
     *
     * @param utilisateurId l'identifiant de l'utilisateur passager
     * @return liste des relations de participation de cet utilisateur
     */
    @Query("SELECT cp FROM CovoituragePassagers cp WHERE cp.utilisateur.id = :utilisateurId")
    List<CovoituragePassagers> findByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
    /**
     * Recherche tous les passagers d'une annonce à partir de son identifiant.
     * Méthode dérivée Spring Data JPA utilisant l'ID de l'annonce.
     * Utilisé pour récupérer rapidement la liste des passagers sans charger l'entité complète de l'annonce.
     *
     * @param idAnnonce l'identifiant de l'annonce de covoiturage
     * @return liste de toutes les relations passagers pour cette annonce
     */
    List<CovoituragePassagers> findByAnnonceCovoiturageId(Long idAnnonce);
}