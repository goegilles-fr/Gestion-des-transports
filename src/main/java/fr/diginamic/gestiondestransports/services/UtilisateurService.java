package fr.diginamic.gestiondestransports.services;

import fr.diginamic.gestiondestransports.dto.ModifierProfilDto;
import fr.diginamic.gestiondestransports.entites.Adresse;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.enums.RoleEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UtilisateurService {
    Utilisateur inscrireUtilisateur(String nom, String prenom, String email, String motDePasse, Adresse adresse);

    @Transactional(readOnly = true)
    Utilisateur obtenirUtilisateurParEmail(String email);

    @Transactional(readOnly = true)
    Utilisateur obtenirProfilUtilisateur(Long utilisateurId);

    Utilisateur mettreAJourProfil(Long utilisateurId, String nom, String prenom, String email, Adresse adresse);

    @Transactional(readOnly = true)
    Page<Utilisateur> rechercherUtilisateurs(String recherche, Pageable pageable);

    @Transactional(readOnly = true)
    List<Utilisateur> obtenirUtilisateursParRole(RoleEnum role);

    @Transactional(readOnly = true)
    Utilisateur obtenirUtilisateurParId(Long id);

    Utilisateur bannirUtilisateur(Long utilisateurId, boolean estBanni);

    Utilisateur verifierUtilisateur(Long utilisateurId, boolean estVerifie);

    @Transactional(readOnly = true)
    List<Utilisateur> obtenirUtilisateursBannis();

    @Transactional(readOnly = true)
    List<Utilisateur> obtenirUtilisateursNonVerifies();

    @Transactional(readOnly = true)
    List<Utilisateur> obtenirTousLesUtilisateurs();

    Utilisateur modifierProfilUtilisateur(String emailUtilisateur, ModifierProfilDto modifierProfilDto);

    void changerMotDePasse(String emailUtilisateur, String newpassword);

    void demanderReinitialisationMotDePasse(String email);

    void reinitialiserMotDePasseAvecToken(String token);

    Utilisateur supprimerUtilisateur(Long utilisateurId);
}
