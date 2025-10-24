package fr.diginamic.gestiondestransports.controllers;

import fr.diginamic.gestiondestransports.dto.ModifierProfilDto;
import fr.diginamic.gestiondestransports.dto.UtilisateurDto;
import fr.diginamic.gestiondestransports.dto.VehiculeDTO;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.entites.VehiculePersonnel;
import fr.diginamic.gestiondestransports.mapper.UtilisateurMapper;
import fr.diginamic.gestiondestransports.mapper.VehiculeMapper;
import fr.diginamic.gestiondestransports.services.UtilisateurService;
import fr.diginamic.gestiondestransports.enums.RoleEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/utilisateurs")
@Tag(name = "Utilisateurs", description = "Gestion des comptes utilisateurs et profils")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private UtilisateurMapper utilisateurMapper;

    @Autowired
    private VehiculeMapper vehiculeMapper;
    /**
     * Récupère tous les utilisateurs
     * @return la liste des utilisateurs sous forme de DTOs
     */
    @GetMapping
    @Operation(
            summary = "Obtenir la liste de tous les utilisateurs.")
    public ResponseEntity<List<UtilisateurDto>> obtenirTousLesUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurService.obtenirTousLesUtilisateurs();

        // ENTITE -> DTO pour chaque utilisateur
        List<UtilisateurDto> utilisateursDto = utilisateurs.stream()
                .map(utilisateurMapper::versDto)
                .toList();

        return ResponseEntity.ok(utilisateursDto);
    }
    /**
     * Demande de réinitialisation de mot de passe
     * Envoie un email avec un lien de réinitialisation
     *
     * @param emailRequest Map contenant l'email
     * @return ResponseEntity avec message de confirmation
     *
     * POST /api/utilisateurs/passwordreset
     * Body: {"email": "user@example.com"}
     */
    @PostMapping("/passwordreset")
    @Operation(summary = "Demander la réinitialisation du mot de passe par email")
    public ResponseEntity<Map<String, String>> demanderReinitialisationMotDePasse(@RequestBody Map<String, String> emailRequest) {
        try {
            String email = emailRequest.get("email");
            utilisateurService.demanderReinitialisationMotDePasse(email);

            Map<String, String> reponse = new HashMap<>();
            reponse.put("message", "Un lien de réinitialisation a été envoyé à votre email");
            return ResponseEntity.ok(reponse);

        } catch (RuntimeException e) {
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
        }
    }

    /**
     * Changement de mot de passe pour l'utilisateur connecté
     * Utilise le token JWT pour identifier l'utilisateur
     *
     * @param authentication L'authentification JWT
     * @param requestBody Map contenant le nouveau mot de passe
     * @return ResponseEntity avec message de confirmation
     *
     * PUT api/auth/changepassword
     * Body: {"newpassword": "monNouveauMotDePasse123"}
     */
    @PutMapping("/changepassword")
    @Operation(summary = "Changer son mot de passe (authentification requise)")
    public ResponseEntity<?> changerMotDePasse(
            Authentication authentication,
            @RequestBody Map<String, String> requestBody) {
        try {
            // Récupérer l'email de l'utilisateur connecté depuis le token JWT
            String emailUtilisateur = authentication.getName();

            String newpassword = requestBody.get("newpassword");

            // Appeler le service pour changer le mot de passe
            utilisateurService.changerMotDePasse(emailUtilisateur, newpassword);

            Map<String, String> reponse = new HashMap<>();
            reponse.put("message", "Votre mot de passe a été modifié avec succès");
            return ResponseEntity.ok(reponse);

        } catch (RuntimeException e) {
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
        }
    }
    /**
     * Endpoint for getting a user by ID.
     * Accessible at GET /api/utilisateurs/{id}
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtenir des informations sur l'utilisateur par id.")
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
    @Operation(
            summary = "Obtenir des informations sur l'utilisateur par email.")
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
     * @param authentication
     * @return
     *
     *   http://localhost:8080/api/utilisateurs/profile
     */
    @GetMapping("/profile")
    @Operation(
            summary = "Obtenir des informations sur l'utilisateur par token. (Obtenez des informations sur vous-même) ")
    public ResponseEntity<?> obtenirProfilUtilisateurConnecte(Authentication authentication) {
        try {
            // Récupérer l'email de l'utilisateur connecté depuis le token JWT
            String emailUtilisateurConnecte = authentication.getName();

            // Chercher l'utilisateur par email
            Utilisateur utilisateur = utilisateurService.obtenirUtilisateurParEmail(emailUtilisateurConnecte);

            // ENTITE -> DTO pour éviter l'exposition du mot de passe
            UtilisateurDto utilisateurDto = utilisateurMapper.versDto(utilisateur);

            return ResponseEntity.ok(utilisateurDto);

        } catch (RuntimeException e) {
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", "Utilisateur non trouvé");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erreur);
        }
    }


    /**
     * @param authentication
     * @return
     *
     * http://localhost:8080/api/utilisateurs/mavoiture
     */
    @GetMapping("/mavoiture")
    @Operation(
            summary = "Obtenir des informations sur vouiture perso par token. (prends ma voiture)")
    public ResponseEntity<?> obtenirMaVoiture(Authentication authentication) {
        try {
            // Récupérer l'email de l'utilisateur connecté depuis le token JWT
            String emailUtilisateurConnecte = authentication.getName();

            // Chercher l'utilisateur par email
            Utilisateur utilisateur = utilisateurService.obtenirUtilisateurParEmail(emailUtilisateurConnecte);

            // Vérifier si l'utilisateur a un véhicule personnel
            Set<VehiculePersonnel> vehiculesPersonnels = utilisateur.getVehiculesPersonnels();

            if (vehiculesPersonnels != null && !vehiculesPersonnels.isEmpty()) {
                // L'utilisateur ne peut avoir qu'une seule voiture personnelle
                VehiculePersonnel maVoiture = vehiculesPersonnels.iterator().next();

                // ENTITE -> DTO
                VehiculeDTO voitureDto = vehiculeMapper.toDto(maVoiture);

                return ResponseEntity.ok(voitureDto);
            } else {
                // Aucun véhicule personnel enregistré
                Map<String, String> message = new HashMap<>();
                message.put("message", "Vous n'avez aucune voiture personnelle enregistrée");
                return ResponseEntity.ok(message);
            }

        } catch (RuntimeException e) {
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", "Utilisateur non trouvé");
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
    @Operation(
            summary = "rechercher tous les utilisateurs avec un rôle")
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
    @Operation(
            summary = "rechercher tous les utilisateurs non vérifiés")
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
    @Operation(
            summary = "bannir l'utilisateur par identifiant (avec le paramètre vrai ou faux)")

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
    @Operation(
            summary = "vérifier l'utilisateur par identifiant (avec le paramètre vrai ou faux)")
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


    /**
     * Endpoint pour modifier le profil de l'utilisateur connecté
     * Accessible à PUT /api/utilisateurs/profile
     *
     * @param authentication authentification JWT pour récupérer l'utilisateur connecté
     * @param modifierProfilDto données à mettre à jour (peut être partiel)
     * @return le profil utilisateur mis à jour
     */
    @PutMapping("/profile")
    @Operation(
            summary = "Modifier le profil de l'utilisateur connecté (nom, prénom, adresse, mot de passe)")
    public ResponseEntity<?> modifierProfilUtilisateurConnecte(
            Authentication authentication,
            @RequestBody @Valid ModifierProfilDto modifierProfilDto) {
        try {
            // Vérifier qu'au moins un champ est fourni pour la mise à jour
            if (!modifierProfilDto.aDesChampsPourMiseAJour()) {
                Map<String, String> erreur = new HashMap<>();
                erreur.put("error", "Au moins un champ doit être fourni pour la mise à jour");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
            }

            // Récupérer l'email de l'utilisateur connecté depuis le token JWT
            String emailUtilisateurConnecte = authentication.getName();

            // Appeler le service pour mettre à jour le profil
            Utilisateur utilisateurMisAJour = utilisateurService.modifierProfilUtilisateur(
                    emailUtilisateurConnecte, modifierProfilDto);

            // ENTITE -> DTO pour éviter l'exposition du mot de passe
            UtilisateurDto utilisateurDtoMisAJour = utilisateurMapper.versDto(utilisateurMisAJour);

            return ResponseEntity.ok(utilisateurDtoMisAJour);

        } catch (RuntimeException e) {
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erreur);
        }
    }

    /**
     *
     * Accessible à PUT /api/utilisateurs/{id}/supprimer     *
     * @param id L'identifiant de l'utilisateur à supprimer
     * @return ResponseEntity avec le message de confirmation et l'utilisateur supprimé
     *
     * */
    @PutMapping("/{id}/supprimer")
    @Operation(
            summary = "Supprimer un utilisateur (soft delete - marque comme supprimé)")
    public ResponseEntity<?> supprimerUtilisateur(@PathVariable Long id) {
        try {
            Utilisateur utilisateur = utilisateurService.supprimerUtilisateur(id);
            // ENTITE -> DTO
            UtilisateurDto utilisateurDto = utilisateurMapper.versDto(utilisateur);

            Map<String, Object> reponse = new HashMap<>();
            reponse.put("message", "Utilisateur supprimé avec succès");
            reponse.put("utilisateur", utilisateurDto);

            return ResponseEntity.ok(reponse);
        } catch (RuntimeException e) {
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erreur);
        }
    }
    /**
     * Récupère le véhicule personnel d'un utilisateur spécifique
     * @param id L'identifiant de l'utilisateur
     * @return Le véhicule personnel de l'utilisateur sous forme de DTO
     */
    @GetMapping("/{id}/vehicule-perso")
    public ResponseEntity<VehiculeDTO> obtenirVehiculePersonnelParId(@PathVariable Long id) {
        VehiculePersonnel vehicule = utilisateurService.obtenirVehiculePersonnelParUtilisateurId(id);

        if (vehicule == null) {
            return ResponseEntity.noContent().build();
        }

        VehiculeDTO vehiculeDto = vehiculeMapper.toDto(vehicule);
        return ResponseEntity.ok(vehiculeDto);
    }



}