package fr.diginamic.gestiondestransports;

import fr.diginamic.gestiondestransports.dto.VehiculeDTO;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.entites.VehiculeEntreprise;
import fr.diginamic.gestiondestransports.enums.Categorie;
import fr.diginamic.gestiondestransports.enums.Motorisation;
import fr.diginamic.gestiondestransports.enums.RoleEnum;
import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import fr.diginamic.gestiondestransports.repositories.UtilisateurRepository;
import fr.diginamic.gestiondestransports.repositories.VehiculeEntrepriseRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour la gestion des véhicules d'entreprise.
 * Utilise une vraie base de données (TEST_covoit) et teste l'ensemble de la pile applicative.
 * Teste la création, modification, consultation et suppression des véhicules de service.
 * Vérifie les règles métier: gestion des statuts, disponibilité, restrictions d'accès admin.
 * Execute avec Maven Failsafe plugin via la commande: mvn verify
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VehiculeEntrepriseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VehiculeEntrepriseRepository vehiculeEntrepriseRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String BASE_URL = "/api/vehicules-entreprise";
    private static String jwtTokenAdmin;
    private static String jwtTokenUser;
    private static Long adminId;
    private static Long userId;
    private static Long vehiculeId;

    /**
     * Configuration initiale avant tous les tests.
     * Crée un utilisateur admin et un utilisateur standard pour les tests.
     * Génère les tokens JWT pour l'authentification.
     */
    @BeforeAll
    static void setupGlobal(@Autowired UtilisateurRepository userRepo,
                            @Autowired PasswordEncoder encoder,
                            @Autowired TestRestTemplate template) {
        System.out.println("=== Configuration globale des tests d'intégration VehiculeEntreprise ===");

        // Créer un administrateur de test
        Utilisateur admin = new Utilisateur();
        admin.setNom("AdminTest");
        admin.setPrenom("Vehicule");
        admin.setEmail("admin.vehicule.test." + System.currentTimeMillis() + "@example.com");
        admin.setPassword(encoder.encode("AdminPass123!"));
        admin.setEstVerifie(true);
        admin.setEstBanni(false);
        admin.setRole(RoleEnum.ROLE_ADMIN);
        admin = userRepo.save(admin);
        adminId = admin.getId();

        System.out.println("✓ Administrateur de test créé - ID: " + adminId);

        // Créer un utilisateur standard de test
        Utilisateur user = new Utilisateur();
        user.setNom("UserTest");
        user.setPrenom("Standard");
        user.setEmail("user.vehicule.test." + System.currentTimeMillis() + "@example.com");
        user.setPassword(encoder.encode("UserPass123!"));
        user.setEstVerifie(true);
        user.setEstBanni(false);
        user.setRole(RoleEnum.ROLE_USER);
        user = userRepo.save(user);
        userId = user.getId();

        System.out.println("✓ Utilisateur standard de test créé - ID: " + userId);

        // Obtenir un token JWT pour l'admin
        Map<String, String> loginRequestAdmin = Map.of(
                "username", admin.getEmail(),
                "password", "AdminPass123!"
        );

        System.out.println("Tentative de connexion admin avec email: " + admin.getEmail());

        ResponseEntity<Map> loginResponseAdmin = template.postForEntity(
                "/api/auth/login",
                loginRequestAdmin,
                Map.class
        );

        System.out.println("Statut de la connexion admin: " + loginResponseAdmin.getStatusCode());

        if (loginResponseAdmin.getStatusCode() == HttpStatus.OK && loginResponseAdmin.getBody() != null) {
            jwtTokenAdmin = (String) loginResponseAdmin.getBody().get("jwt");
            System.out.println("✓ Token JWT admin obtenu: OUI (" + jwtTokenAdmin.length() + " caractères)");
        } else {
            System.err.println("❌ Échec de l'obtention du token JWT admin");
        }

        // Obtenir un token JWT pour l'utilisateur standard
        Map<String, String> loginRequestUser = Map.of(
                "username", user.getEmail(),
                "password", "UserPass123!"
        );

        System.out.println("Tentative de connexion user avec email: " + user.getEmail());

        ResponseEntity<Map> loginResponseUser = template.postForEntity(
                "/api/auth/login",
                loginRequestUser,
                Map.class
        );

        System.out.println("Statut de la connexion user: " + loginResponseUser.getStatusCode());

        if (loginResponseUser.getStatusCode() == HttpStatus.OK && loginResponseUser.getBody() != null) {
            jwtTokenUser = (String) loginResponseUser.getBody().get("jwt");
            System.out.println("✓ Token JWT user obtenu: OUI (" + jwtTokenUser.length() + " caractères)");
        } else {
            System.err.println("❌ Échec de l'obtention du token JWT user");
        }
    }

    /**
     * Nettoyage après chaque test.
     * Affiche un message de fin de test.
     */
    @AfterEach
    void tearDown() {
        System.out.println("=== Fin du test d'intégration ===");
    }

    /**
     * Nettoyage global après tous les tests.
     * Supprime toutes les données de test créées (véhicules, utilisateurs).
     */
    @AfterAll
    static void nettoyageGlobal(@Autowired VehiculeEntrepriseRepository vehiculeRepo,
                                @Autowired UtilisateurRepository userRepo) {
        System.out.println("=== Nettoyage global ===");

        // Supprimer les véhicules de test
        if (vehiculeId != null) {
            vehiculeRepo.findById(vehiculeId).ifPresent(vehicule -> {
                vehiculeRepo.delete(vehicule);
                System.out.println("✓ Véhicule de test supprimé");
            });
        }

        // Supprimer l'administrateur de test
        if (adminId != null) {
            userRepo.findById(adminId).ifPresent(userRepo::delete);
            System.out.println("✓ Administrateur de test supprimé");
        }

        // Supprimer l'utilisateur standard de test
        if (userId != null) {
            userRepo.findById(userId).ifPresent(userRepo::delete);
            System.out.println("✓ Utilisateur standard de test supprimé");
        }
    }

    /**
     * Crée les headers HTTP avec le token JWT admin pour l'authentification.
     *
     * @return HttpHeaders contenant le token Bearer admin
     */
    private HttpHeaders createAdminAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (jwtTokenAdmin != null && !jwtTokenAdmin.isEmpty()) {
            headers.set("Authorization", "Bearer " + jwtTokenAdmin);
            System.out.println("Header Authorization admin ajouté");
        } else {
            System.err.println("❌ ERREUR: jwtTokenAdmin est null ou vide !");
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Crée les headers HTTP avec le token JWT user pour l'authentification.
     *
     * @return HttpHeaders contenant le token Bearer user
     */
    private HttpHeaders createUserAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (jwtTokenUser != null && !jwtTokenUser.isEmpty()) {
            headers.set("Authorization", "Bearer " + jwtTokenUser);
            System.out.println("Header Authorization user ajouté");
        } else {
            System.err.println("❌ ERREUR: jwtTokenUser est null ou vide !");
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Test de création d'un véhicule d'entreprise avec succès par un admin.
     * Vérifie qu'un administrateur peut créer un nouveau véhicule.
     * Valide que le véhicule est bien créé en base de données avec les bonnes informations.
     */
    @Test
    @Order(1)
    @DisplayName("IT - Création d'un véhicule d'entreprise par un admin réussie")
    void testCreerVehicule_admin_success() {
        // Given - Préparation du véhicule
        VehiculeDTO vehiculeDto = new VehiculeDTO(
                null,
                "AB-123-CD",
                "Renault",
                "Kangoo",
                5,
                Motorisation.THERMIQUE,
                120,
                "https://example.com/kangoo.jpg",
                Categorie.MINI_CITADINE,
                StatutVehicule.EN_SERVICE,
                null
                );

        HttpEntity<VehiculeDTO> request = new HttpEntity<>(vehiculeDto, createAdminAuthHeaders());

        // When - Création du véhicule
        ResponseEntity<VehiculeDTO> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.POST,
                request,
                VehiculeDTO.class
        );

        // Then - Vérifications
        System.out.println("Statut de la réponse: " + response.getStatusCode());
        System.out.println("Corps de la réponse: " + response.getBody());

        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Le statut devrait être 201 CREATED");

        assertNotNull(response.getBody(), "Le corps de la réponse ne doit pas être null");
        assertNotNull(response.getBody().id(), "L'ID du véhicule ne doit pas être null");

        vehiculeId = response.getBody().id();

        // Vérifier en base de données
        assertTrue(vehiculeEntrepriseRepository.existsById(vehiculeId),
                "Le véhicule devrait exister en base de données");

        System.out.println("✓ Véhicule créé avec succès - ID: " + vehiculeId);
    }

    /**
     * Test de création d'un véhicule par un utilisateur non-admin.
     * Vérifie qu'un utilisateur standard ne peut pas créer de véhicule (accès refusé).
     */
    @Test
    @Order(2)
    @DisplayName("IT - Création d'un véhicule par un utilisateur standard échoue")
    void testCreerVehicule_user_echec() {
        // Given - Préparation du véhicule
        VehiculeDTO vehiculeDto = new VehiculeDTO(
                null,
                "EF-456-GH",
                "Peugeot",
                "Partner",
                5,
                Motorisation.THERMIQUE,
                110,
                "https://example.com/partner.jpg",
                Categorie.BERLINE_M,
                StatutVehicule.EN_SERVICE,
                null
                );

        HttpEntity<VehiculeDTO> request = new HttpEntity<>(vehiculeDto, createUserAuthHeaders());

        // When - Tentative de création par un user standard
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Le statut devrait être 403 FORBIDDEN pour un user standard");

        System.out.println("✓ Test d'accès refusé validé");
    }

    /**
     * Test de récupération de tous les véhicules d'entreprise.
     * Vérifie qu'un utilisateur authentifié peut obtenir la liste de tous les véhicules.
     */
    @Test
    @Order(3)
    @DisplayName("IT - Récupération de tous les véhicules d'entreprise")
    void testObtenirTousLesVehicules_success() {
        // Given - Headers d'authentification
        HttpEntity<Void> request = new HttpEntity<>(createUserAuthHeaders());

        // When - Récupération de tous les véhicules
        ResponseEntity<List> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.GET,
                request,
                List.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Nombre de véhicules: " + response.getBody().size());

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Le statut devrait être 200 OK");

        assertNotNull(response.getBody(), "La liste ne doit pas être null");
        assertFalse(response.getBody().isEmpty(),
                "La liste ne devrait pas être vide (au moins le véhicule créé)");

        System.out.println("✓ Récupération de tous les véhicules réussie");
    }

    /**
     * Test de récupération d'un véhicule spécifique par son ID.
     * Vérifie qu'on peut consulter les détails d'un véhicule.
     */
    @Test
    @Order(4)
    @DisplayName("IT - Récupération d'un véhicule par ID")
    void testObtenirVehiculeParId_success() {
        // Given - Headers d'authentification
        HttpEntity<Void> request = new HttpEntity<>(createUserAuthHeaders());

        // When - Récupération du véhicule par ID
        ResponseEntity<VehiculeDTO> response = restTemplate.exchange(
                BASE_URL + "/" + vehiculeId,
                HttpMethod.GET,
                request,
                VehiculeDTO.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Véhicule: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Le statut devrait être 200 OK");

        assertNotNull(response.getBody(), "Le véhicule ne doit pas être null");
        assertEquals(vehiculeId, response.getBody().id(),
                "L'ID devrait correspondre");

        System.out.println("✓ Récupération du véhicule par ID réussie");
    }

    /**
     * Test de récupération des véhicules par statut.
     * Vérifie qu'on peut filtrer les véhicules selon leur statut.
     */
    @Test
    @Order(5)
    @DisplayName("IT - Récupération des véhicules par statut")
    void testObtenirVehiculesParStatut_success() {
        // Given - Headers d'authentification
        HttpEntity<Void> request = new HttpEntity<>(createUserAuthHeaders());

        // When - Récupération des véhicules EN_SERVICE
        ResponseEntity<List> response = restTemplate.exchange(
                BASE_URL + "/statut/EN_SERVICE",
                HttpMethod.GET,
                request,
                List.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Nombre de véhicules EN_SERVICE: " + response.getBody().size());

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Le statut devrait être 200 OK");

        assertNotNull(response.getBody(), "La liste ne doit pas être null");

        System.out.println("✓ Récupération des véhicules par statut réussie");
    }

    /**
     * Test de récupération des véhicules disponibles pour une période.
     * Vérifie qu'on peut obtenir les véhicules disponibles à la réservation.
     */
    @Test
    @Order(6)
    @DisplayName("IT - Récupération des véhicules disponibles pour une période")
    void testObtenirVehiculesDisponibles_success() {
        // Given - Période de recherche
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(10);
        LocalDateTime dateFin = dateDebut.plusHours(4);

        HttpEntity<Void> request = new HttpEntity<>(createUserAuthHeaders());

        // When - Récupération des véhicules disponibles
        String url = BASE_URL + "/dispo?dateDebut=" + dateDebut + "&dateFin=" + dateFin;
        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                List.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Nombre de véhicules disponibles: " + response.getBody().size());

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Le statut devrait être 200 OK");

        assertNotNull(response.getBody(), "La liste ne doit pas être null");

        System.out.println("✓ Récupération des véhicules disponibles réussie");
    }

    /**
     * Test de modification d'un véhicule par un admin.
     * Vérifie qu'un administrateur peut modifier un véhicule existant.
     */
    @Test
    @Order(7)
    @DisplayName("IT - Modification d'un véhicule par un admin réussie")
    void testModifierVehicule_admin_success() {
        // Given - Nouvelles données pour le véhicule
        VehiculeDTO vehiculeModifie = new VehiculeDTO(
                vehiculeId,
                "AB-123-CD",
                "Renault",
                "Kangoo Z.E.",
                5,
                Motorisation.ELECTRIQUE,
                0,
                "https://example.com/kangoo-ze.jpg",
                Categorie.CIDADINE_POLYVALANTE,
                StatutVehicule.EN_SERVICE,
                null
                );

        HttpEntity<VehiculeDTO> request = new HttpEntity<>(vehiculeModifie, createAdminAuthHeaders());

        // When - Modification du véhicule
        ResponseEntity<VehiculeDTO> response = restTemplate.exchange(
                BASE_URL + "/" + vehiculeId,
                HttpMethod.PUT,
                request,
                VehiculeDTO.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Véhicule modifié: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Le statut devrait être 200 OK");

        assertNotNull(response.getBody(), "Le véhicule modifié ne doit pas être null");
        assertEquals(Motorisation.ELECTRIQUE, response.getBody().motorisation(),
                "La motorisation devrait être mise à jour");

        System.out.println("✓ Modification du véhicule réussie");
    }

    /**
     * Test de modification d'un véhicule par un utilisateur standard.
     * Vérifie qu'un utilisateur non-admin ne peut pas modifier un véhicule.
     */
    @Test
    @Order(8)
    @DisplayName("IT - Modification d'un véhicule par un utilisateur standard échoue")
    void testModifierVehicule_user_echec() {
        // Given - Données de modification
        VehiculeDTO vehiculeModifie = new VehiculeDTO(
                vehiculeId,
                "AB-123-CD",
                "Renault",
                "Kangoo",
                5,
                Motorisation.HYBRIDE,
                120,
                "https://example.com/kangoo.jpg",
                Categorie.COMPACTE,
                StatutVehicule.HORS_SERVICE,
                null
                );

        HttpEntity<VehiculeDTO> request = new HttpEntity<>(vehiculeModifie, createUserAuthHeaders());

        // When - Tentative de modification par un user standard
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/" + vehiculeId,
                HttpMethod.PUT,
                request,
                String.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Le statut devrait être 403 FORBIDDEN pour un user standard");

        System.out.println("✓ Test de modification refusée validé");
    }

    /**
     * Test de suppression d'un véhicule par un admin.
     * Vérifie qu'un administrateur peut supprimer un véhicule.
     * Ce test doit être exécuté en dernier car il supprime le véhicule.
     */
    @Test
    @Order(9)
    @DisplayName("IT - Suppression d'un véhicule par un admin réussie")
    void testSupprimerVehicule_admin_success() {
        // Given - Headers d'authentification admin
        HttpEntity<Void> request = new HttpEntity<>(createAdminAuthHeaders());

        // When - Suppression du véhicule
        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + "/" + vehiculeId,
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(),
                "Le statut devrait être 204 NO_CONTENT");

        // Vérifier que le véhicule n'existe plus en base
        assertFalse(vehiculeEntrepriseRepository.existsById(vehiculeId),
                "Le véhicule ne devrait plus exister en base de données");

        System.out.println("✓ Suppression du véhicule réussie");
    }

    /**
     * Test d'accès sans authentification.
     * Vérifie qu'on ne peut pas accéder aux véhicules sans token JWT.
     */
    @Test
    @Order(10)
    @DisplayName("IT - Accès sans authentification échoue")
    void testAccesSansAuthentification_echec() {
        // Given - Pas de headers d'authentification
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());

        // When - Tentative d'accès sans authentification
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.GET,
                request,
                String.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());

        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED || response.getStatusCode() == HttpStatus.FORBIDDEN,
                "Le statut devrait être 401 UNAUTHORIZED ou 403 FORBIDDEN sans authentification");

        System.out.println("✓ Test d'accès non authentifié validé");
    }
}