package fr.diginamic.gestiondestransports.security;

import java.util.Collections;

import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Utilisée par AuthController pour authentifier l'utilisateur en 2 temps:
 * - il est appelé une 1ère fois indirectement par AuthenticationManager pour vérifier le user/password
 * - il est appelé une 2nds fois de manière directe pour récupérer le détail.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /** Pour l'accès en base de données */
    @Autowired
    private UtilisateurRepository UtilisateurRepository;
    /**
     * Charge les détails d'un utilisateur depuis la base de données pour l'authentification Spring Security.
     * Utilisé en deux temps par le processus d'authentification :
     * 1. Appelé indirectement par AuthenticationManager pour vérifier username/password
     * 2. Appelé directement pour récupérer les détails complets de l'utilisateur après authentification
     *
     * Convertit l'entité Utilisateur en objet UserDetails de Spring Security.
     * Le compte est activé uniquement si estVerifie = true.
     * Le rôle de l'utilisateur est ajouté comme autorité (ROLE_COLLABORATEUR ou ROLE_ADMIN).
     *
     * @param email l'adresse email de l'utilisateur (utilisée comme username)
     * @return les détails de l'utilisateur pour Spring Security
     * @throws UsernameNotFoundException si aucun utilisateur ne correspond à cet email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur user = UtilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getEstVerifie(),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}