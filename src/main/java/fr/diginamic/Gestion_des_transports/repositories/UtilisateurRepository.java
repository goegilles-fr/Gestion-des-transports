package fr.diginamic.Gestion_des_transports.repositories;


import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email) ;

    List<Utilisateur> findByRole(RoleEnum role);

    List<Utilisateur> findByEstBanni(Boolean estBanni);

    List<Utilisateur> findByEstVerifie(Boolean estVerifie);

    @Query("SELECT u FROM Utilisateur u WHERE u.nom = :nom AND u.prenom = :prenom")
    List<Utilisateur> findByNomAndPrenom(@Param("nom") String nom, @Param("prenom") String prenom);

    @Query("SELECT u FROM Utilisateur u WHERE u.role = :role AND u.estBanni = false AND u.estVerifie = true")
    List<Utilisateur> findActiveUsersByRole(@Param("role") RoleEnum role);

    boolean existsByEmail(String email);
}