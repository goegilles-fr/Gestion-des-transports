package fr.diginamic.gestiondestransports.repositories;

import fr.diginamic.gestiondestransports.entites.Adresse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
/**
 * Repository JPA pour la gestion des entités Adresse.
 * Fournit les opérations CRUD standard et des requêtes personnalisées pour rechercher des adresses.
 * Les adresses sont utilisées pour les points de départ et d'arrivée des annonces de covoiturage,
 * ainsi que pour les adresses personnelles des utilisateurs.
 * Permet d'éviter la duplication d'adresses identiques en base de données.
 */
@Repository
public interface AdresseRepository extends JpaRepository<Adresse, Long> {
    /**
     * Recherche une adresse exacte par tous ses composants.
     * Permet de vérifier si une adresse existe déjà en base avant d'en créer une nouvelle.
     * Utile pour éviter la duplication d'adresses identiques.
     *
     * @param numero le numéro de voie
     * @param libelle le libellé de la voie (nom de rue)
     * @param codePostal le code postal
     * @param ville le nom de la ville
     * @return Optional contenant l'adresse si elle existe, vide sinon
     */
    Optional<Adresse> findByNumeroAndLibelleAndCodePostalAndVille(
            Integer numero, String libelle, String codePostal, String ville);
    /**
     * Recherche toutes les adresses d'une ville donnée.
     * Permet de filtrer les annonces de covoiturage par ville de départ ou d'arrivée.
     *
     * @param ville le nom de la ville recherchée
     * @return liste de toutes les adresses dans cette ville
     */
    List<Adresse> findByVille(String ville);
    /**
     * Recherche toutes les adresses ayant un code postal donné.
     * Utile pour les recherches géographiques de covoiturages.
     *
     * @param codePostal le code postal recherché
     * @return liste de toutes les adresses avec ce code postal
     */
    List<Adresse> findByCodePostal(String codePostal);
    /**
     * Recherche toutes les adresses correspondant à une ville et un code postal.
     * Requête JPQL personnalisée pour affiner les recherches géographiques.
     * Utile pour valider la cohérence ville/code postal lors de la saisie d'adresses.
     *
     * @param ville le nom de la ville
     * @param codePostal le code postal
     * @return liste des adresses correspondant aux deux critères
     */
    @Query("SELECT a FROM Adresse a WHERE a.ville = :ville AND a.codePostal = :codePostal")
    List<Adresse> findByVilleAndCodePostal(@Param("ville") String ville, @Param("codePostal") String codePostal);
}
