package fr.diginamic.Gestion_des_transports.services;

import fr.diginamic.Gestion_des_transports.entites.Adresse;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.enums.RoleEnum;
import fr.diginamic.Gestion_des_transports.repositories.UtilisateurRepository;
import fr.diginamic.Gestion_des_transports.tools.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AdresseService adresseService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailSender emailSender;
    /**
     * Inscrire un nouvel utilisateur
     * @param nom Le nom de l'utilisateur
     * @param prenom Le prénom de l'utilisateur
     * @param email L'email de l'utilisateur
     * @param motDePasse Le mot de passe en clair
     * @param adresse L'adresse de l'utilisateur (optionnelle)
     * @return L'utilisateur créé
     * @throws RuntimeException Si l'email existe déjà
     */
    public Utilisateur inscrireUtilisateur(String nom, String prenom, String email, String motDePasse, Adresse adresse) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(email)) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Créer le nouvel utilisateur
        Utilisateur nouvelUtilisateur = new Utilisateur();
        nouvelUtilisateur.setNom(nom);
        nouvelUtilisateur.setPrenom(prenom);
        nouvelUtilisateur.setEmail(email);
        nouvelUtilisateur.setPassword(passwordEncoder.encode(motDePasse));
        nouvelUtilisateur.setRole(RoleEnum.ROLE_USER); // Rôle par défaut
        nouvelUtilisateur.setEstBanni(false);
        nouvelUtilisateur.setEstVerifie(true);//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!user is created already verified for testing

        // Créer ou assigner l'adresse si fournie
        if (adresse != null) {
            Adresse adresseSauvegardee = adresseService.creerAdresse(adresse);
            nouvelUtilisateur.setAdresse(adresseSauvegardee);
        }

        emailSender.send(
                email,
                "Bienvenue dans notre plateforme de covoiturage !",
                "Hello " + prenom + " " + nom + ", welcome to covoit",
                "Bienvenue sur Covoit"
        );


        return utilisateurRepository.save(nouvelUtilisateur);
    }

    /**
     * Obtenir un utilisateur par son email
     * @param email L'email de l'utilisateur
     * @return L'utilisateur trouvé
     * @throws RuntimeException Si l'utilisateur n'est pas trouvé
     */
    @Transactional(readOnly = true)
    public Utilisateur obtenirUtilisateurParEmail(String email) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findByEmail(email);
        if (utilisateur.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé avec email: " + email);
        }
        return utilisateur.get();
    }

    /**
     * Obtenir le profil d'un utilisateur par ID
     * @param utilisateurId L'ID de l'utilisateur
     * @return L'utilisateur trouvé
     * @throws RuntimeException Si l'utilisateur n'est pas trouvé
     */
    @Transactional(readOnly = true)
    public Utilisateur obtenirProfilUtilisateur(Long utilisateurId) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findById(utilisateurId);
        if (utilisateur.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        return utilisateur.get();
    }

    /**
     * Mettre à jour le profil d'un utilisateur
     * @param utilisateurId L'ID de l'utilisateur
     * @param nom Nouveau nom (optionnel)
     * @param prenom Nouveau prénom (optionnel)
     * @param email Nouvel email (optionnel)
     * @param adresse Nouvelle adresse (optionnelle)
     * @return L'utilisateur mis à jour
     * @throws RuntimeException Si l'utilisateur n'est pas trouvé ou email déjà utilisé
     */
    public Utilisateur mettreAJourProfil(Long utilisateurId, String nom, String prenom, String email, Adresse adresse) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(utilisateurId);
        if (utilisateurOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        // Vérifier si le nouvel email est déjà utilisé par un autre utilisateur
        if (email != null && !email.equals(utilisateur.getEmail()) && utilisateurRepository.existsByEmail(email)) {
            throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur");
        }

        // Mettre à jour les champs
        if (nom != null) utilisateur.setNom(nom);
        if (prenom != null) utilisateur.setPrenom(prenom);
        if (email != null) utilisateur.setEmail(email);

        // Mettre à jour l'adresse
        if (adresse != null) {
            if (utilisateur.getAdresse() != null) {
                // Mettre à jour l'adresse existante
                adresseService.mettreAJourAdresse((long) utilisateur.getAdresse().getId(), adresse);
            } else {
                // Créer une nouvelle adresse
                Adresse nouvelleAdresse = adresseService.creerAdresse(adresse);
                utilisateur.setAdresse(nouvelleAdresse);
            }
        }

        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Rechercher des utilisateurs par nom et/ou prénom (pour admin)
     * @param recherche Terme de recherche (nom ou prénom)
     * @param pageable Paramètres de pagination
     * @return Page des utilisateurs trouvés
     */
    @Transactional(readOnly = true)
    public Page<Utilisateur> rechercherUtilisateurs(String recherche, Pageable pageable) {
        if (recherche == null || recherche.trim().isEmpty()) {
            return utilisateurRepository.findAll(pageable);
        }
        // Pour une recherche plus flexible, vous pourriez ajouter une méthode au repository
        // Pour l'instant, nous utilisons findAll et filtrerons côté service
        return utilisateurRepository.findAll(pageable);
    }

    /**
     * Obtenir tous les utilisateurs par rôle
     * @param role Le rôle à rechercher
     * @return Liste des utilisateurs avec ce rôle
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> obtenirUtilisateursParRole(RoleEnum role) {
        return utilisateurRepository.findByRole(role);
    }

    /**
     * Obtenir un utilisateur par ID (pour admin)
     * @param id L'ID de l'utilisateur
     * @return L'utilisateur trouvé
     * @throws RuntimeException Si l'utilisateur n'est pas trouvé
     */
    @Transactional(readOnly = true)
    public Utilisateur obtenirUtilisateurParId(Long id) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findById(id);
        if (utilisateur.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        return utilisateur.get();
    }

    /**
     * Bannir ou débannir un utilisateur (admin seulement)
     * @param utilisateurId L'ID de l'utilisateur
     * @param estBanni true pour bannir, false pour débannir
     * @return L'utilisateur mis à jour
     * @throws RuntimeException Si l'utilisateur n'est pas trouvé
     */
    public Utilisateur bannirUtilisateur(Long utilisateurId, boolean estBanni) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(utilisateurId);
        if (utilisateurOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        utilisateur.setEstBanni(estBanni);

        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Vérifier ou dévérifier un utilisateur (admin seulement)
     * @param utilisateurId L'ID de l'utilisateur
     * @param estVerifie true pour vérifier, false pour dévérifier
     * @return L'utilisateur mis à jour
     * @throws RuntimeException Si l'utilisateur n'est pas trouvé
     */
    public Utilisateur verifierUtilisateur(Long utilisateurId, boolean estVerifie) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(utilisateurId);
        if (utilisateurOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        utilisateur.setEstVerifie(estVerifie);

        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Obtenir les utilisateurs bannis
     * @return Liste des utilisateurs bannis
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> obtenirUtilisateursBannis() {
        return utilisateurRepository.findByEstBanni(true);
    }

    /**
     * Obtenir les utilisateurs non vérifiés
     * @return Liste des utilisateurs non vérifiés
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> obtenirUtilisateursNonVerifies() {
        return utilisateurRepository.findByEstVerifie(false);
    }


    /**
     * Obtenir tous les utilisateurs
     * @return Liste de tous les utilisateurs
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> obtenirTousLesUtilisateurs() {
        return utilisateurRepository.findAll();
    }
}