package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.diginamic.Gestion_des_transports.security.CustomUserDetailsService;
import fr.diginamic.Gestion_des_transports.security.JwtUtil;


// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!



/**
 * Contrôleur en charge de l'authentification
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** authenticationManager : permet d'authentifier l'utilisateur */
    @Autowired
    private AuthenticationManager authenticationManager;

    /** Pour générer un token lors de l'authentification */
    @Autowired
    private JwtUtil jwtUtil;

    /** Permet d'aller cherche en base les infos utilisateur */
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UtilisateurService utilisateurService;

    /** Endpoint de LOGIN qui reçoit un body contenant 2 infos : username et password (non crypté)
     * @param authRequest le body de la requête HTTP
     * @return {@link ResponseEntity}
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());

            return ResponseEntity.ok(new AuthResponse(jwt));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Utilisateur utilisateur) {
        try {
            // Appelle le service pour gérer la logique d'inscription
            Utilisateur nouvelUtilisateur = utilisateurService.inscrireUtilisateur(
                    utilisateur.getNom(),
                    utilisateur.getPrenom(),
                    utilisateur.getEmail(),
                    utilisateur.getPassword(),
                    utilisateur.getAdresse()
            );
            return ResponseEntity.ok("User " + nouvelUtilisateur.getEmail() + " registered successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }






}