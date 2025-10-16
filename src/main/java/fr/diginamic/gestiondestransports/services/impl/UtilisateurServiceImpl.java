package fr.diginamic.gestiondestransports.services.impl;

import fr.diginamic.gestiondestransports.dto.ModifierProfilDto;
import fr.diginamic.gestiondestransports.entites.Adresse;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.enums.RoleEnum;
import fr.diginamic.gestiondestransports.mapper.ModifierProfilMapper;
import fr.diginamic.gestiondestransports.repositories.UtilisateurRepository;
import fr.diginamic.gestiondestransports.services.AdresseService;
import fr.diginamic.gestiondestransports.services.UtilisateurService;
import fr.diginamic.gestiondestransports.tools.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AdresseService adresseService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired

    private EmailSender emailSender;

    @Autowired
    private ModifierProfilMapper modifierProfilMapper;

    @Value("${app.base.url}")
    private String urlBase;
    // Stockage en mémoire des tokens de réinitialisation
    // Clé: token, Valeur: email de l'utilisateur
    private final Map<String, String> tokensReinitialisation = new ConcurrentHashMap<>();

    // Clé: token, Valeur: date d'expiration
    private final Map<String, Long> tokensExpiration = new ConcurrentHashMap<>();


    /**
     * Inscrire un nouvel utilisateur
     *
     * @param nom        Le nom de l'utilisateur
     * @param prenom     Le prénom de l'utilisateur
     * @param email      L'email de l'utilisateur
     * @param motDePasse Le mot de passe en clair
     * @param adresse    L'adresse de l'utilisateur (optionnelle)
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
        String corps =
                 "Voici les informations de votre compte :<br>"
                + "- Email : <b>" + email + "</b><br>"
                + "- Statut : Votre profil a été créé, mais n’est pas encore vérifié.<br><br>"
                + "Merci de rejoindre notre communauté de covoiturage !<br>"
                + "À bientôt,<br>"
                + "L’équipe Covoit";

        emailSender.send(
                email,
                corps,
                "Hello " + prenom + " " + nom + ", welcome to covoit",
                "Bienvenue sur Covoit. "
        );


        return utilisateurRepository.save(nouvelUtilisateur);
    }

    /**
     * Obtenir un utilisateur par son email
     *
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
     *
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
     *
     * @param utilisateurId L'ID de l'utilisateur
     * @param nom           Nouveau nom (optionnel)
     * @param prenom        Nouveau prénom (optionnel)
     * @param email         Nouvel email (optionnel)
     * @param adresse       Nouvelle adresse (optionnelle)
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
     *
     * @param recherche Terme de recherche (nom ou prénom)
     * @param pageable  Paramètres de pagination
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
     *
     * @param role Le rôle à rechercher
     * @return Liste des utilisateurs avec ce rôle
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> obtenirUtilisateursParRole(RoleEnum role) {
        return utilisateurRepository.findByRole(role);
    }

    /**
     * Obtenir un utilisateur par ID (pour admin)
     *
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
     *
     * @param utilisateurId L'ID de l'utilisateur
     * @param estBanni      true pour bannir, false pour débannir
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
     *
     * @param utilisateurId L'ID de l'utilisateur
     * @param estVerifie    true pour vérifier, false pour dévérifier
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
     *
     * @return Liste des utilisateurs bannis
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> obtenirUtilisateursBannis() {
        return utilisateurRepository.findByEstBanni(true);
    }

    /**
     * Obtenir les utilisateurs non vérifiés
     *
     * @return Liste des utilisateurs non vérifiés
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> obtenirUtilisateursNonVerifies() {
        return utilisateurRepository.findByEstVerifie(false);
    }


    /**
     * Obtenir tous les utilisateurs
     *
     * @return Liste de tous les utilisateurs
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> obtenirTousLesUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    /**
     * Modifie le profil d'un utilisateur (mise à jour partielle)
     * Seuls les champs autorisés peuvent être modifiés : nom, prénom, adresse, mot de passe
     *
     * @param emailUtilisateur  email de l'utilisateur à modifier
     * @param modifierProfilDto données à mettre à jour (peut contenir seulement les champs à modifier)
     * @return l'utilisateur mis à jour
     * @throws RuntimeException si l'utilisateur n'est pas trouvé
     */
    public Utilisateur modifierProfilUtilisateur(String emailUtilisateur, ModifierProfilDto modifierProfilDto) {
        // Récupérer l'utilisateur existant par email
        Utilisateur utilisateurExistant = obtenirUtilisateurParEmail(emailUtilisateur);

        // Si un nouveau mot de passe est fourni, le hacher avant la mise à jour
        if (modifierProfilDto.motDePasse() != null && !modifierProfilDto.motDePasse().trim().isEmpty()) {
            // Créer un nouveau DTO avec le mot de passe haché
            ModifierProfilDto dtoAvecMotDePasseHache = new ModifierProfilDto(
                    modifierProfilDto.nom(),
                    modifierProfilDto.prenom(),
                    modifierProfilDto.adresse(),
                    passwordEncoder.encode(modifierProfilDto.motDePasse())
            );

            // Utiliser le mapper pour mettre à jour seulement les champs fournis
            modifierProfilMapper.mettreAJourProfil(dtoAvecMotDePasseHache, utilisateurExistant);
        } else {
            // Pas de mot de passe à modifier, utiliser le DTO tel quel
            modifierProfilMapper.mettreAJourProfil(modifierProfilDto, utilisateurExistant);
        }

        // Sauvegarder les modifications
        return utilisateurRepository.save(utilisateurExistant);
    }


    /**
     * Demande de réinitialisation de mot de passe
     * Génère un token unique et envoie un email avec le lien
     *
     * @param email L'email de l'utilisateur
     * @throws RuntimeException Si l'email est vide ou l'utilisateur n'existe pas
     */
    public void demanderReinitialisationMotDePasse(String email) {
        // Validation de l'email
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("L'email est requis");
        }

        // Vérifier que l'utilisateur existe
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);
        if (utilisateurOpt.isEmpty()) {
            throw new RuntimeException("Aucun utilisateur trouvé avec cet email");
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        // Vérifier que l'utilisateur n'est pas banni ou supprimé
        if (Boolean.TRUE.equals(utilisateur.getEstBanni())) {
            throw new RuntimeException("Ce compte est banni");
        }

        if (Boolean.TRUE.equals(utilisateur.getEstSupprime())) {
            throw new RuntimeException("Ce compte a été supprimé");
        }

        // Générer un token unique et sécurisé
        String token = genererTokenSecurise();

        // Stocker le token avec une expiration de 1 heure (3600000 ms)
        tokensReinitialisation.put(token, email);
        tokensExpiration.put(token, System.currentTimeMillis() + 3600000);

        // Construire le lien de réinitialisation
        String lienReinitialisation = urlBase + "/reset-password?token=" + token;
        // Envoyer l'email avec le lien
        emailSender.send(
                email,
                "Réinitialisation de votre mot de passe",
                "Bonjour " + utilisateur.getPrenom() + " " + utilisateur.getNom() +
                        ",\n\nVous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                        "Cliquez sur ce lien pour réinitialiser votre mot de passe :\n" +
                        lienReinitialisation + "\n\n" +
                        "Ce lien expirera dans 1 heure.\n\n" +
                        "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.",
                "Réinitialisation de mot de passe"
        );
    }

    /**
     * Réinitialise le mot de passe avec le token reçu
     * Génère un nouveau mot de passe aléatoire et l'envoie par email
     *
     * @param token Le token de réinitialisation
     * @throws RuntimeException Si le token est invalide, expiré ou utilisateur non trouvé
     */
    public void reinitialiserMotDePasseAvecToken(String token) {
        // Validation du token
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Token invalide");
        }

        // Vérifier que le token existe
        if (!tokensReinitialisation.containsKey(token)) {
            throw new RuntimeException("Token invalide ou déjà utilisé");
        }

        // Vérifier que le token n'a pas expiré
        Long dateExpiration = tokensExpiration.get(token);
        if (dateExpiration == null || System.currentTimeMillis() > dateExpiration) {
            // Nettoyer les tokens expirés
            tokensReinitialisation.remove(token);
            tokensExpiration.remove(token);
            throw new RuntimeException("Le token a expiré");
        }

        // Récupérer l'email associé au token
        String email = tokensReinitialisation.get(token);

        // Récupérer l'utilisateur
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);
        if (utilisateurOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        // Générer un nouveau mot de passe aléatoire
        String nouveauMotDePasse = genererMotDePasseAleatoire();

        // Encoder et sauvegarder le nouveau mot de passe
        utilisateur.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(utilisateur);

        // Invalider le token (suppression)
        tokensReinitialisation.remove(token);
        tokensExpiration.remove(token);

        // Envoyer le nouveau mot de passe par email
        emailSender.send(
                email,
                "Votre nouveau mot de passe",
                "Bonjour " + utilisateur.getPrenom() + " " + utilisateur.getNom() +
                        ",\n\nVotre mot de passe a été réinitialisé avec succès.\n\n" +
                        "Votre nouveau mot de passe est : " + nouveauMotDePasse + "\n\n" +
                        "Pour des raisons de sécurité, nous vous recommandons de le changer " +
                        "après votre prochaine connexion.",
                "Nouveau mot de passe"
        );
    }

    /**
     * Génère un token sécurisé de 32 caractères
     *
     * @return Le token généré
     */
    private String genererTokenSecurise() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        // Convertir en hexadécimal
        StringBuilder token = new StringBuilder();
        for (byte b : bytes) {
            token.append(String.format("%02x", b));
        }
        return token.toString();
    }

    /**
     * Génère un mot de passe aléatoire de 12 caractères
     * Contient majuscules, minuscules, chiffres et caractères spéciaux
     *
     * @return Le mot de passe généré
     */
    private String genererMotDePasseAleatoire() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
        SecureRandom random = new SecureRandom();
        StringBuilder motDePasse = new StringBuilder(12);

        for (int i = 0; i < 12; i++) {
            motDePasse.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return motDePasse.toString();
    }


    /**
     * Change le mot de passe d'un utilisateur authentifié
     *
     * @param email             L'email de l'utilisateur
     * @param nouveauMotDePasse Le nouveau mot de passe
     * @throws RuntimeException Si l'email est invalide, le mot de passe vide, ou l'utilisateur non trouvé
     */
    public void changerMotDePasse(String email, String nouveauMotDePasse) {
        // Validation de l'email
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email invalide");
        }

        // Validation du nouveau mot de passe
        if (nouveauMotDePasse == null || nouveauMotDePasse.trim().isEmpty()) {
            throw new RuntimeException("Le nouveau mot de passe est requis");
        }

        if (nouveauMotDePasse.length() < 6) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
        }

        // Récupérer l'utilisateur
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);
        if (utilisateurOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        // Vérifier que l'utilisateur n'est pas banni ou supprimé
        if (Boolean.TRUE.equals(utilisateur.getEstBanni())) {
            throw new RuntimeException("Ce compte est banni");
        }

        if (Boolean.TRUE.equals(utilisateur.getEstSupprime())) {
            throw new RuntimeException("Ce compte a été supprimé");
        }

        // Encoder et sauvegarder le nouveau mot de passe
        utilisateur.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(utilisateur);
    }

    /**
     * Supprimer (soft delete) un utilisateur
     * @param utilisateurId L'ID de l'utilisateur à supprimer
     *
     */
    public Utilisateur supprimerUtilisateur(Long utilisateurId) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(utilisateurId);
        if (utilisateurOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        utilisateur.setEstSupprime(true);

        return utilisateurRepository.save(utilisateur);
    }
}