package fr.diginamic.Gestion_des_transports.controllers;

import fr.diginamic.Gestion_des_transports.dto.UtilisateurDto;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.mapper.UtilisateurMapper;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import fr.diginamic.Gestion_des_transports.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private UtilisateurMapper utilisateurMapper;

    /**
     * Récupère tous les utilisateurs
     * @return la liste des utilisateurs sous forme de DTOs
     */
    @GetMapping
    public ResponseEntity<List<UtilisateurDto>> obtenirTousLesUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurService.obtenirTousLesUtilisateurs();

        // ENTITE -> DTO pour chaque utilisateur
        List<UtilisateurDto> utilisateursDto = utilisateurs.stream()
                .map(utilisateurMapper::versDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(utilisateursDto);
    }

    /**
     * Met à jour un utilisateur existant
     * @param id l'identifiant de l'utilisateur à mettre à jour
     * @param utilisateurDto les nouvelles données de l'utilisateur
     * @return l'utilisateur mis à jour sous forme de DTO
     */

    /**
     * Endpoint for getting a user by ID.
     * Accessible at GET /api/utilisateurs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirUtilisateurParId(@PathVariable Long id) {
        try {
            Utilisateur utilisateur = utilisateurService.obtenirUtilisateurParId(id);
            // ENTITE -> DTO pour éviter les boucles infinies et masquer le mot de passe
            UtilisateurDto utilisateurDto = utilisateurMapper.versDto(utilisateur);
            return ResponseEntity.ok(utilisateurDto);
        } catch (RuntimeException e) {
            // Retourner le message d'erreur en JSON
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", e.getMessage()); // "Utilisateur non trouvé"
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erreur);
        }
    }
    //  EXAMPLE
    // GET  http://localhost:8080/api/utilisateurs/email/harrypot@gmail.com
    @GetMapping("/email/{email}")
    public ResponseEntity<?> obtenirUtilisateurParEmail(@PathVariable String email) {
        try {
            Utilisateur utilisateur = utilisateurService.obtenirUtilisateurParEmail(email);
            // ENTITE -> DTO
            UtilisateurDto utilisateurDto = utilisateurMapper.versDto(utilisateur);
            return ResponseEntity.ok(utilisateurDto);
        } catch (RuntimeException e) {
            // Retourner le message d'erreur en JSON
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", e.getMessage()); // Message d'erreur du service
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erreur);
        }
    }

    /**
     * Endpoint for getting users by role.
     * Accessible at GET /api/utilisateurs/by-role
     *
     *  http://localhost:8080/api/utilisateurs/by-role?role=ROLE_ADMINhttp://localhost:8080/api/utilisateurs/by-role?role=ROLE_ADMIN
     */

    @GetMapping("/by-role")
    public ResponseEntity<List<UtilisateurDto>> getUtilisateursByRole(@RequestParam("role") RoleEnum role) {
        List<Utilisateur> utilisateurs = utilisateurService.obtenirUtilisateursParRole(role);
        // ENTITE -> DTO
        List<UtilisateurDto> utilisateursDto = utilisateurMapper.versDtoList(utilisateurs);
        return ResponseEntity.ok(utilisateursDto);
    }

    /**
     * Endpoint for getting all banned users.
     * Accessible at GET /api/utilisateurs/bannis
     */


    /**
     * Endpoint for getting all unverified users.
     * Accessible at GET /api/utilisateurs/non-verifies
     */
    @GetMapping("/non-verifies")
    public ResponseEntity<List<UtilisateurDto>> getUtilisateursNonVerifies() {
        List<Utilisateur> utilisateurs = utilisateurService.obtenirUtilisateursNonVerifies();
        // ENTITE -> DTO
        List<UtilisateurDto> utilisateursDto = utilisateurMapper.versDtoList(utilisateurs);
        return ResponseEntity.ok(utilisateursDto);
    }

    /**
     * Endpoint for banning or unbanning a user.
     * Accessible at PUT /api/utilisateurs/{id}/bannir
     *
     * http://localhost:8080/api/utilisateurs/10/bannir?estBanni=true
     */

    @PutMapping("/{id}/bannir")
    public ResponseEntity<?> bannirUtilisateur(@PathVariable Long id, @RequestParam("estBanni") boolean estBanni) {
        try {
            Utilisateur utilisateur = utilisateurService.bannirUtilisateur(id, estBanni);
            // ENTITE -> DTO
            UtilisateurDto utilisateurDto = utilisateurMapper.versDto(utilisateur);

            Map<String, Object> reponse = new HashMap<>();
            reponse.put("message", estBanni ? "Utilisateur banni avec succès" : "Utilisateur débanni avec succès");
            reponse.put("utilisateur", utilisateurDto);

            return ResponseEntity.ok(reponse);
        } catch (RuntimeException e) {
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erreur);
        }
    }

    /**
     * Endpoint pour vérifier ou dé-vérifier un utilisateur
     * Accessible à PUT /api/utilisateurs/{id}/verifier?estVerifie=true
     *
     * http://localhost:8080/api/utilisateurs/9/verifier?estVerifie=true
     *
     */
    @PutMapping("/{id}/verifier")
    public ResponseEntity<?> verifierUtilisateur(@PathVariable Long id, @RequestParam("estVerifie") boolean estVerifie) {
        try {
            Utilisateur utilisateur = utilisateurService.verifierUtilisateur(id, estVerifie);
            // ENTITE -> DTO
            UtilisateurDto utilisateurDto = utilisateurMapper.versDto(utilisateur);

            Map<String, Object> reponse = new HashMap<>();
            reponse.put("message", estVerifie ? "Utilisateur vérifié avec succès" : "Utilisateur dé-vérifié avec succès");
            reponse.put("utilisateur", utilisateurDto);

            return ResponseEntity.ok(reponse);
        } catch (RuntimeException e) {
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erreur);
        }
    }
}