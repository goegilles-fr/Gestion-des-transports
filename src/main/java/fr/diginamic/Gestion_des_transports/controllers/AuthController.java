package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.dto.RegistrationDto;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.mapper.AdresseMapper;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.diginamic.Gestion_des_transports.security.CustomUserDetailsService;
import fr.diginamic.Gestion_des_transports.security.JwtUtil;

import java.util.HashMap;
import java.util.Map;


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

    /** Permet d'aller chercher en base les infos utilisateur */
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private AdresseMapper adresseMapper;

    /** Endpoint de LOGIN qui reçoit un body contenant 2 infos : username et password (non crypté)
     * @param authRequest le body de la requête HTTP
     * @return {@link ResponseEntity}
     */
    @PostMapping("/login")
    @Operation(
            summary = "Login. username+password ")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());

            Utilisateur utilisateur = utilisateurService.obtenirUtilisateurParEmail(userDetails.getUsername());

            if (utilisateur.getEstBanni()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("BANNED");
            if (utilisateur.getEstSupprime()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("DELETED");


            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            return ResponseEntity.ok(new AuthResponse(jwt));

        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("NON_VERIFIED");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("BAD_CREDENTIALS");
        }


    }

    @PostMapping("/register")
    @Operation(
            summary = "Register. Nom Prenom email password, adress complete ")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationDto registrationDto,
                                      BindingResult bindingResult) {
        //System.out.println("Libelle value: '" + registrationDto.adresse().libelle() + "'");
        // Vérifier s'il y a des erreurs de validation
        if (bindingResult.hasErrors()) {
            Map<String, String> erreurs = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    erreurs.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreurs);
        }

        try {
            // Convertir l'adresse DTO en entité si elle existe
            var adresseEntite = registrationDto.adresse() != null ?
                    adresseMapper.versEntite(registrationDto.adresse()) : null;

            // Appelle le service pour gérer la logique d'inscription
            Utilisateur nouvelUtilisateur = utilisateurService.inscrireUtilisateur(
                    registrationDto.nom(),
                    registrationDto.prenom(),
                    registrationDto.email(),
                    registrationDto.password(),
                    adresseEntite
            );

            // Créer une réponse de succès
            Map<String, Object> reponse = new HashMap<>();
            reponse.put("message", "Utilisateur " + nouvelUtilisateur.getEmail() + " inscrit avec succès!");
            reponse.put("userId", nouvelUtilisateur.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(reponse);

        } catch (RuntimeException e) {
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
        }
    }





}