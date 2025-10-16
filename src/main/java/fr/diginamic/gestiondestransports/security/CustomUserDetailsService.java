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