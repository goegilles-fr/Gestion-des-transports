package fr.diginamic.Gestion_des_transports.repositories;

import fr.diginamic.Gestion_des_transports.entites.Adresse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdresseRepository extends JpaRepository<Adresse, Long> {

    Optional<Adresse> findByNumeroAndLibelleAndCodePostalAndVille(
            Integer numero, String libelle, String codePostal, String ville);

    List<Adresse> findByVille(String ville);

    List<Adresse> findByCodePostal(String codePostal);

    @Query("SELECT a FROM Adresse a WHERE a.ville = :ville AND a.codePostal = :codePostal")
    List<Adresse> findByVilleAndCodePostal(@Param("ville") String ville, @Param("codePostal") String codePostal);
}
