package fr.diginamic.Gestion_des_transports.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.diginamic.Gestion_des_transports.entites.RichardUser;



// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!


/**
 * Accès base de données
 */
public interface UserRepository extends JpaRepository<RichardUser, Long> {
    
	/** Recherche un User via son username
     * @param username username
     * @return {@link Optional}
     */
    Optional<RichardUser> findByUsername(String username);
}