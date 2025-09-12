package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import fr.diginamic.Gestion_des_transports.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;


    /**
     * Endpoint for getting all users.
     * Accessible at GET /api/utilisateurs
     */
    @GetMapping
    public ResponseEntity<List<Utilisateur>> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurService.obtenirTousLesUtilisateurs();
        return ResponseEntity.ok(utilisateurs);
    }

    /**
     * Endpoint for getting a user by ID.
     * Accessible at GET /api/utilisateurs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUtilisateurById(@PathVariable Long id) {
        try {
            Utilisateur utilisateur = utilisateurService.obtenirUtilisateurParId(id);
            return ResponseEntity.ok(utilisateur);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Endpoint for getting users by role.
     * Accessible at GET /api/utilisateurs/by-role
     */
    @GetMapping("/by-role")
    public ResponseEntity<List<Utilisateur>> getUtilisateursByRole(@RequestParam("role") RoleEnum role) {
        List<Utilisateur> utilisateurs = utilisateurService.obtenirUtilisateursParRole(role);
        return ResponseEntity.ok(utilisateurs);
    }

    /**
     * Endpoint for getting all banned users.
     * Accessible at GET /api/utilisateurs/bannis
     */
    @GetMapping("/bannis")
    public ResponseEntity<List<Utilisateur>> getUtilisateursBannis() {
        List<Utilisateur> utilisateurs = utilisateurService.obtenirUtilisateursBannis();
        return ResponseEntity.ok(utilisateurs);
    }

    /**
     * Endpoint for getting all unverified users.
     * Accessible at GET /api/utilisateurs/non-verifies
     */
    @GetMapping("/non-verifies")
    public ResponseEntity<List<Utilisateur>> getUtilisateursNonVerifies() {
        List<Utilisateur> utilisateurs = utilisateurService.obtenirUtilisateursNonVerifies();
        return ResponseEntity.ok(utilisateurs);
    }

    /**
     * Endpoint for banning or unbanning a user.
     * Accessible at PUT /api/utilisateurs/{id}/bannir
     */
    @PutMapping("/{id}/bannir")
    public ResponseEntity<?> bannirUtilisateur(@PathVariable Long id, @RequestParam("estBanni") boolean estBanni) {
        try {
            Utilisateur utilisateur = utilisateurService.bannirUtilisateur(id, estBanni);
            return ResponseEntity.ok(utilisateur);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Endpoint for verifying or un-verifying a user.
     * Accessible at PUT /api/utilisateurs/{id}/verifier
     */
    @PutMapping("/{id}/verifier")
    public ResponseEntity<?> verifierUtilisateur(@PathVariable Long id, @RequestParam("estVerifie") boolean estVerifie) {
        try {
            Utilisateur utilisateur = utilisateurService.verifierUtilisateur(id, estVerifie);
            return ResponseEntity.ok(utilisateur);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}