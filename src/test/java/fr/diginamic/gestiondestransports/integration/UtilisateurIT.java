package fr.diginamic.gestiondestransports.integration;

import fr.diginamic.gestiondestransports.dto.AdresseDto;
import fr.diginamic.gestiondestransports.dto.RegistrationDto;
import fr.diginamic.gestiondestransports.repositories.UtilisateurRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour l'inscription d'utilisateurs.
 * Utilise une vraie base de données (covoit_test) et teste l'ensemble de la pile applicative.
 * Execute avec Maven Failsafe plugin via la commande: mvn verify
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UtilisateurIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private static final String BASE_URL = "/api/auth";
    private static String emailUtilisateurCree;

    @BeforeEach
    void setUp() {
        System.out.println("=== Début du test d'intégration ===");
    }

    @AfterEach
    void tearDown() {
        System.out.println("=== Fin du test d'intégration ===");
    }

    @AfterAll
    static void nettoyageGlobal(@Autowired UtilisateurRepository repo) {
        // Nettoyer l'utilisateur créé pendant les tests
        if (emailUtilisateurCree != null) {
            repo.findByEmail(emailUtilisateurCree).ifPresent(repo::delete);
            System.out.println("Utilisateur de test supprimé: " + emailUtilisateurCree);
        }
    }

    @Test
    @Order(1)
    @DisplayName("IT - Inscription d'un nouvel utilisateur avec adresse complète")
    void testInscriptionUtilisateur_avecAdresse_success() {
        // Given - Préparation des données de test
        AdresseDto adresseDto = new AdresseDto(
                null,  // id
                42,    // numero
                "Rue de la République",  // libelle
                "34000",  // codePostal
                "Montpellier"  // ville
        );

        String emailUnique = "integration.test." + System.currentTimeMillis() + "@example.com";
        emailUtilisateurCree = emailUnique;

        RegistrationDto registrationDto = new RegistrationDto(
                "Dupont",      // nom
                "Jean",        // prenom
                emailUnique,   // email
                "Password123!",  // password
                adresseDto     // adresse
        );

        // When - Exécution de la requête HTTP POST
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                registrationDto,
                Map.class
        );

        // Then - Vérifications
        System.out.println("Statut de la réponse: " + response.getStatusCode());
        System.out.println("Corps de la réponse: " + response.getBody());

        // Vérifier le statut HTTP
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Le statut devrait être 201 CREATED");

        // Vérifier le corps de la réponse
        assertNotNull(response.getBody(), "Le corps de la réponse ne doit pas être null");
        assertTrue(response.getBody().containsKey("message"),
                "La réponse doit contenir un message");
        assertTrue(response.getBody().containsKey("userId"),
                "La réponse doit contenir l'ID de l'utilisateur");

        // Vérifier que l'utilisateur existe bien en base de données
        Long userId = ((Number) response.getBody().get("userId")).longValue();
        assertTrue(utilisateurRepository.existsById(userId),
                "L'utilisateur devrait exister en base de données");

        System.out.println("Utilisateur créé avec succès - ID: " + userId);
    }


    @Test
    @Order(2)
    @DisplayName("IT - Connexion réussie avec identifiants corrects")
    void testLogin_identifiants_corrects_success() {
        // Given - Utiliser l'utilisateur créé dans le test précédent
        // Il faut d'abord le vérifier pour permettre la connexion
        utilisateurRepository.findByEmail(emailUtilisateurCree).ifPresent(user -> {
            user.setEstVerifie(true);
            utilisateurRepository.save(user);
        });

        // When - Tentative de connexion avec identifiants corrects
        Map<String, String> loginRequest = Map.of(
                "username", emailUtilisateurCree,
                "password", "Password123!"
        );

        ResponseEntity<Map> responseLogin = restTemplate.postForEntity(
                BASE_URL + "/login",
                loginRequest,
                Map.class
        );

        // Then - Vérifications
        System.out.println("Statut de connexion: " + responseLogin.getStatusCode());
        System.out.println("Réponse: " + responseLogin.getBody());

        assertEquals(HttpStatus.OK, responseLogin.getStatusCode(),
                "La connexion devrait réussir avec les bons identifiants");

        assertNotNull(responseLogin.getBody());
        assertTrue(responseLogin.getBody().containsKey("jwt"),
                "La réponse doit contenir un token JWT");

        String jwt = (String) responseLogin.getBody().get("jwt");
        assertNotNull(jwt, "Le token JWT ne doit pas être null");
        assertFalse(jwt.isEmpty(), "Le token JWT ne doit pas être vide");

        System.out.println("Test de connexion réussie validé");
    }

    @Test
    @Order(3)
    @DisplayName("IT - Connexion échoue avec mot de passe incorrect")
    void testLogin_motDePasse_incorrect_echec() {
        // Given - Utiliser le même utilisateur (déjà vérifié par le test précédent)
        String motDePasseIncorrect = "MauvaisMotDePasse456!";

        // When - Tentative de connexion avec mauvais mot de passe
        Map<String, String> loginRequest = Map.of(
                "username", emailUtilisateurCree,
                "password", motDePasseIncorrect
        );

        ResponseEntity<String> responseLogin = restTemplate.postForEntity(
                BASE_URL + "/login",
                loginRequest,
                String.class
        );

        // Then - Vérifications
        System.out.println("Statut attendu: 401 UNAUTHORIZED");
        System.out.println("Statut reçu: " + responseLogin.getStatusCode());
        System.out.println("Message: " + responseLogin.getBody());

        assertEquals(HttpStatus.UNAUTHORIZED, responseLogin.getStatusCode(),
                "La connexion devrait échouer avec un mauvais mot de passe");

        assertNotNull(responseLogin.getBody());
        assertEquals("BAD_CREDENTIALS", responseLogin.getBody(),
                "Le message d'erreur devrait être 'BAD_CREDENTIALS'");

        System.out.println("Test d'échec de connexion validé");
    }




    @Test
    @Order(4)
    @DisplayName("IT - Inscription échoue avec un email déjà existant")
    void testInscriptionUtilisateur_emailDuplique_echec() {
        // Given - Créer un premier utilisateur
        String emailDuplique = "duplicate.test." + System.currentTimeMillis() + "@example.com";

        AdresseDto adresse = new AdresseDto(null, 10, "Rue Test", "75001", "Paris");
        RegistrationDto premierUtilisateur = new RegistrationDto(
                "Martin", "Alice", emailDuplique, "Pass123!", adresse
        );

        // Créer le premier utilisateur
        ResponseEntity<Map> premiereReponse = restTemplate.postForEntity(
                BASE_URL + "/register",
                premierUtilisateur,
                Map.class
        );
        assertEquals(HttpStatus.CREATED, premiereReponse.getStatusCode());

        // When - Tenter de créer un second utilisateur avec le même email
        RegistrationDto doublonUtilisateur = new RegistrationDto(
                "Durant", "Pierre", emailDuplique, "DifferentPass456!", adresse
        );

        ResponseEntity<Map> responseDoublon = restTemplate.postForEntity(
                BASE_URL + "/register",
                doublonUtilisateur,
                Map.class
        );

        // Then - Vérifier que la création échoue
        System.out.println("Statut attendu: 400 BAD_REQUEST");
        System.out.println("Statut reçu: " + responseDoublon.getStatusCode());
        System.out.println("Message d'erreur: " + responseDoublon.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, responseDoublon.getStatusCode(),
                "Le statut devrait être 400 BAD_REQUEST pour un email dupliqué");

        assertNotNull(responseDoublon.getBody());
        assertTrue(responseDoublon.getBody().containsKey("error"),
                "La réponse d'erreur doit contenir un champ 'error'");

        // Nettoyer l'utilisateur créé
        utilisateurRepository.findByEmail(emailDuplique).ifPresent(utilisateurRepository::delete);
        System.out.println("Test de duplication d'email réussi");
    }

    @Test
    @Order(5)
    @DisplayName("IT - Inscription échoue avec des données invalides (email manquant)")
    void testInscriptionUtilisateur_donneesInvalides_echec() {
        // Given - Données invalides (email null)
        AdresseDto adresse = new AdresseDto(null, 5, "Rue Invalid", "69001", "Lyon");
        RegistrationDto registrationInvalide = new RegistrationDto(
                "Test", "Invalid", null, "Pass123!", adresse  // email null
        );

        // When - Tenter l'inscription avec données invalides
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                registrationInvalide,
                Map.class
        );

        // Then - Vérifier que la validation échoue
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Erreurs de validation: " + response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Le statut devrait être 400 BAD_REQUEST pour des données invalides");

        assertNotNull(response.getBody());

        System.out.println("Test de validation des données réussi");
    }

    @Test
    @Order(6)
    @DisplayName("IT - Récupération du profil utilisateur connecté")
    void testObtenirProfil_success() {
        // Given - Utilisateur vérifié et connecté
        utilisateurRepository.findByEmail(emailUtilisateurCree).ifPresent(user -> {
            user.setEstVerifie(true);
            utilisateurRepository.save(user);
        });

        // Obtenir le JWT token
        Map<String, String> loginRequest = Map.of(
                "username", emailUtilisateurCree,
                "password", "Password123!"
        );

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                BASE_URL + "/login",
                loginRequest,
                Map.class
        );

        String jwtToken = (String) loginResponse.getBody().get("jwt");
        assertNotNull(jwtToken, "Le JWT token ne doit pas être null");

        // Créer les headers avec le token JWT
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        org.springframework.http.HttpEntity<Void> requestEntity = new org.springframework.http.HttpEntity<>(headers);

        // When - Récupérer le profil
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/utilisateurs/profile",
                org.springframework.http.HttpMethod.GET,
                requestEntity,
                Map.class
        );

        // Then - Vérifications
        System.out.println("Statut de la réponse: " + response.getStatusCode());
        System.out.println("Corps de la réponse: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Le statut devrait être 200 OK");

        assertNotNull(response.getBody(), "Le corps de la réponse ne doit pas être null");

        // Vérifier que les champs existent ET contiennent les bonnes valeurs
        assertTrue(response.getBody().containsKey("id"),
                "La réponse doit contenir l'ID");
        assertTrue(response.getBody().containsKey("email"),
                "La réponse doit contenir l'email");
        assertTrue(response.getBody().containsKey("nom"),
                "La réponse doit contenir le nom");
        assertTrue(response.getBody().containsKey("prenom"),
                "La réponse doit contenir le prénom");

        // Vérifier les valeurs réelles (données du Test 1)
        assertEquals(emailUtilisateurCree, response.getBody().get("email"),
                "L'email devrait correspondre à l'utilisateur connecté");
        assertEquals("Dupont", response.getBody().get("nom"),
                "Le nom devrait être 'Dupont'");
        assertEquals("Jean", response.getBody().get("prenom"),
                "Le prénom devrait être 'Jean'");


        System.out.println("Profil récupéré avec succès avec les bonnes données");
    }

    @Test
    @Order(7)
    @DisplayName("IT - Récupération du profil échoue sans authentification")
    void testObtenirProfil_sansAuthentification_echec() {
        // Given - Pas de token JWT
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        org.springframework.http.HttpEntity<Void> requestEntity = new org.springframework.http.HttpEntity<>(headers);

        // When - Tentative de récupération du profil sans authentification
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/utilisateurs/profile",
                org.springframework.http.HttpMethod.GET,
                requestEntity,
                String.class
        );

        // Then - Vérifications
        System.out.println("Statut attendu: 401 UNAUTHORIZED ou 403 FORBIDDEN");
        System.out.println("Statut reçu: " + response.getStatusCode());

        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                        response.getStatusCode() == HttpStatus.FORBIDDEN,
                "Le statut devrait être 401 UNAUTHORIZED ou 403 FORBIDDEN");

        System.out.println("Test d'accès non authentifié validé");
    }

    @Test
    @Order(8)
    @DisplayName("IT - Changement de mot de passe réussi")
    void testChangerMotDePasse_success() {


        // Obtenir le JWT token
        Map<String, String> loginRequest = Map.of(
                "username", emailUtilisateurCree,
                "password", "Password123!"
        );

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                BASE_URL + "/login",
                loginRequest,
                Map.class
        );

        String jwtToken = (String) loginResponse.getBody().get("jwt");

        // Créer les headers avec le token JWT
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        // Nouveau mot de passe
        Map<String, String> passwordChangeRequest = Map.of("newpassword", "NewPassword456!");
        org.springframework.http.HttpEntity<Map<String, String>> requestEntity =
                new org.springframework.http.HttpEntity<>(passwordChangeRequest, headers);

        // When - Changer le mot de passe
        restTemplate.exchange(
                "/api/utilisateurs/changepassword",
                org.springframework.http.HttpMethod.PUT,
                requestEntity,
                Map.class
        );

        // Then - Vérifier que la connexion avec le nouveau mot de passe fonctionne
        Map<String, String> newLoginRequest = Map.of(
                "username", emailUtilisateurCree,
                "password", "NewPassword456!"
        );

        ResponseEntity<Map> newLoginResponse = restTemplate.postForEntity(
                BASE_URL + "/login",
                newLoginRequest,
                Map.class
        );

        assertNotNull(newLoginResponse.getBody().get("jwt"),
                "Changement de mot de passe échoué - impossible de se connecter avec le nouveau mot de passe");
    }



}