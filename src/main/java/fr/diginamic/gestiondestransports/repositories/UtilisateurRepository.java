package fr.diginamic.gestiondestransports.repositories;


import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
/**
 * Repository JPA pour la gestion des entités Utilisateur.
 * Fournit les opérations CRUD standard et des requêtes personnalisées pour gérer les comptes utilisateurs.
 * Permet la recherche par email (authentification), rôle, statuts (banni, vérifié, supprimé).
 * Gère les collaborateurs et administrateurs du système de gestion des transports.
 * Conforme aux règles métier du cahier des charges concernant la gestion des utilisateurs.
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    /**
     * Recherche un utilisateur par son adresse email unique.
     * L'email sert d'identifiant de connexion dans le système d'authentification JWT.
     * Utilisé lors du login et pour récupérer l'utilisateur connecté depuis le token.
     *
     * Règle métier : l'email doit être unique dans la base de données.
     *
     * @param email l'adresse email de l'utilisateur
     * @return Optional contenant l'utilisateur si trouvé, vide sinon
     */
    Optional<Utilisateur> findByEmail(String email) ;
    /**
     * Recherche tous les utilisateurs ayant un rôle spécifique.
     * Rôles possibles : COLLABORATEUR, ADMINISTRATEUR.
     * Permet de lister les administrateurs ou les collaborateurs du système.
     *
     * @param role le rôle recherché (RoleEnum)
     * @return liste de tous les utilisateurs ayant ce rôle
     */
    List<Utilisateur> findByRole(RoleEnum role);
    /**
     * Recherche tous les utilisateurs selon leur statut de bannissement.
     * Un utilisateur banni ne peut plus se connecter au système.
     * Règle métier : seuls les administrateurs peuvent bannir/débannir des utilisateurs.
     *
     * @param estBanni true pour trouver les utilisateurs bannis, false pour les non bannis
     * @return liste des utilisateurs correspondant au critère
     */
    List<Utilisateur> findByEstBanni(Boolean estBanni);
    /**
     * Recherche tous les utilisateurs selon leur statut de vérification.
     * Les utilisateurs non vérifiés ne peuvent pas se connecter.
     * Règle métier : un email de vérification est envoyé lors de l'inscription.
     *
     * @param estVerifie true pour trouver les utilisateurs vérifiés, false pour les non vérifiés
     * @return liste des utilisateurs correspondant au critère
     */
    List<Utilisateur> findByEstVerifie(Boolean estVerifie);
    /**
     * Recherche tous les utilisateurs par nom et prénom.
     * Requête JPQL pour trouver des homonymes dans le système.
     * Utile pour distinguer les utilisateurs ayant des noms similaires.
     *
     * @param nom le nom de famille de l'utilisateur
     * @param prenom le prénom de l'utilisateur
     * @return liste de tous les utilisateurs correspondant à ce nom et prénom
     */
    @Query("SELECT u FROM Utilisateur u WHERE u.nom = :nom AND u.prenom = :prenom")
    List<Utilisateur> findByNomAndPrenom(@Param("nom") String nom, @Param("prenom") String prenom);
    /**
     * Recherche tous les utilisateurs actifs ayant un rôle spécifique.
     * Requête JPQL combinant trois critères : rôle, non banni, et vérifié.
     * Un utilisateur actif est un utilisateur vérifié et non banni.
     * Permet de lister uniquement les utilisateurs opérationnels du système.
     *
     * @param role le rôle recherché (RoleEnum)
     * @return liste des utilisateurs actifs ayant ce rôle
     */
    @Query("SELECT u FROM Utilisateur u WHERE u.role = :role AND u.estBanni = false AND u.estVerifie = true")
    List<Utilisateur> findActiveUsersByRole(@Param("role") RoleEnum role);
    /**
     * Vérifie si un email existe déjà dans la base de données.
     * Utilisé lors de l'inscription pour éviter les doublons.
     * Règle métier : l'email doit être unique, utilisé comme identifiant de connexion.
     *
     * @param email l'adresse email à vérifier
     * @return true si l'email existe déjà, false sinon
     */
    boolean existsByEmail(String email);
}