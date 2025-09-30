package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.CovoituragePassagers;
import fr.diginamic.Gestion_des_transports.entites.AnnonceCovoiturage;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CovoituragePassagersRepository extends JpaRepository<CovoituragePassagers, Long> {

    // Trouver tous les passagers d'une annonce de covoiturage
    List<CovoituragePassagers> findByAnnonceCovoiturage(AnnonceCovoiturage annonceCovoiturage);

    // Trouver tous les covoiturages auxquels un utilisateur participe
    List<CovoituragePassagers> findByUtilisateur(Utilisateur utilisateur);

    // Vérifier si un utilisateur est déjà inscrit à un covoiturage
    Optional<CovoituragePassagers> findByUtilisateurAndAnnonceCovoiturage(
            Utilisateur utilisateur, AnnonceCovoiturage annonceCovoiturage);

    // Supprimer un passager d'un covoiturage
    void deleteByUtilisateurAndAnnonceCovoiturage(Utilisateur utilisateur, AnnonceCovoiturage annonceCovoiturage);

    // Compter le nombre de passagers pour un covoiturage
    @Query("SELECT COUNT(cp) FROM CovoituragePassagers cp WHERE cp.annonceCovoiturage = :annonceCovoiturage")
    Long countPassagersParCovoiturage(@Param("annonceCovoiturage") AnnonceCovoiturage annonceCovoiturage);

    // Trouver les covoiturages d'un utilisateur par ID
    @Query("SELECT cp FROM CovoituragePassagers cp WHERE cp.utilisateur.id = :utilisateurId")
    List<CovoituragePassagers> findByUtilisateurId(@Param("utilisateurId") Long utilisateurId);

    List<CovoituragePassagers> findByAnnonceCovoiturageId(Long idAnnonce);
}