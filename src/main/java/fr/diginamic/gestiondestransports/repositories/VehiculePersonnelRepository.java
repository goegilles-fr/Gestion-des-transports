package fr.diginamic.gestiondestransports.repositories;

import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.entites.VehiculePersonnel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
/**
 * Repository JPA pour la gestion des entités VehiculePersonnel.
 * Fournit les opérations CRUD standard et des requêtes personnalisées pour gérer les véhicules personnels des collaborateurs.
 * Règle métier importante : chaque utilisateur est limité à un seul véhicule personnel.
 * Les véhicules personnels peuvent être utilisés pour créer des annonces de covoiturage.
 * Conforme aux règles métier du cahier des charges concernant les véhicules personnels.
 */
public interface VehiculePersonnelRepository extends JpaRepository<VehiculePersonnel, Long> {
    /**
     * Recherche tous les véhicules personnels d'un utilisateur par son identifiant.
     * Méthode dérivée Spring Data JPA utilisant l'ID de l'utilisateur.
     * Normalement retourne une liste avec un seul élément (règle métier : 1 véhicule par utilisateur).
     * Retourne une liste vide si l'utilisateur n'a pas de véhicule enregistré.
     *
     * @param utilisateurId l'identifiant de l'utilisateur
     * @return liste des véhicules de cet utilisateur (maximum 1 élément)
     */
    List<VehiculePersonnel> findByUtilisateurId(Long utilisateurId);

    /**
     * Recherche tous les véhicules personnels d'un utilisateur.
     * Méthode dérivée Spring Data JPA utilisant l'entité Utilisateur complète.
     * Variante alternative de findByUtilisateurId utile si l'entité Utilisateur est déjà chargée.
     * Règle métier : retourne une liste avec au maximum un véhicule.
     *
     * @param utilisateur l'entité utilisateur
     * @return liste des véhicules de cet utilisateur (maximum 1 élément)
     */
    List<VehiculePersonnel> findByUtilisateur(Utilisateur utilisateur); // this must be here //

    /**
     * Vérifie si une immatriculation existe déjà dans la base de données.
     * Utilisé lors de la création ou modification d'un véhicule personnel pour garantir l'unicité.
     * Règle métier : l'immatriculation doit être unique dans le système (tous véhicules confondus).
     *
     * @param immatriculation l'immatriculation à vérifier
     * @return true si l'immatriculation existe déjà, false sinon
     */
    boolean existsByImmatriculation(String immatriculation);

    /**
     * Vérifie si un utilisateur possède déjà un véhicule personnel.
     * Utilisé avant la création d'un nouveau véhicule pour respecter la limite d'un véhicule par utilisateur.
     * Règle métier : un utilisateur ne peut enregistrer qu'un seul véhicule personnel.
     *
     * @param utilisateurId l'identifiant de l'utilisateur
     * @return true si l'utilisateur possède déjà un véhicule, false sinon
     */
    boolean existsByUtilisateurId(Long utilisateurId);
    /**
     * Recherche le premier véhicule personnel d'un utilisateur.
     * Méthode optimisée pour récupérer directement l'unique véhicule de l'utilisateur.
     * Retourne un Optional vide si l'utilisateur n'a pas de véhicule enregistré.
     * Préférable à findByUtilisateurId quand on sait qu'il n'y a qu'un seul résultat.
     *
     * @param utilisateurId l'identifiant de l'utilisateur
     * @return Optional contenant le véhicule si trouvé, vide sinon
     */
    Optional<VehiculePersonnel> findFirstByUtilisateurId(Long utilisateurId);

    /**
     * Vérifie si un utilisateur possède un autre véhicule personnel que celui spécifié.
     * Utilisé lors de la modification d'un véhicule pour s'assurer que l'utilisateur n'essaie pas d'en créer un second.
     * Exclut le véhicule actuellement modifié de la vérification.
     * Règle métier : empêche un utilisateur d'avoir plusieurs véhicules personnels via la modification.
     *
     * @param utilisateurId l'identifiant de l'utilisateur
     * @param id l'identifiant du véhicule à exclure de la vérification
     * @return true si l'utilisateur possède un autre véhicule, false sinon
     */
    boolean existsByUtilisateurIdAndIdNot(Long utilisateurId, Long id);
}
