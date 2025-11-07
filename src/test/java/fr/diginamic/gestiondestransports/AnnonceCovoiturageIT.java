package fr.diginamic.gestiondestransports;

import fr.diginamic.gestiondestransports.dto.AdresseDto;
import fr.diginamic.gestiondestransports.dto.AnnonceCovoiturageAvecPlacesDto;
import fr.diginamic.gestiondestransports.dto.AnnonceCovoiturageDto;
import fr.diginamic.gestiondestransports.dto.ParticipantsCovoiturageDto;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.entites.VehiculeEntreprise;
import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import fr.diginamic.gestiondestransports.repositories.AnnonceCovoiturageRepository;
import fr.diginamic.gestiondestransports.repositories.UtilisateurRepository;
import fr.diginamic.gestiondestransports.repositories.VehiculeEntrepriseRepository;
import fr.diginamic.gestiondestransports.enums.RoleEnum;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour les réservations de véhicules de service.
 * Utilise une vraie base de données (TEST_covoit) et teste l'ensemble de la pile applicative.
 * Teste la création, modification, consultation et suppression des réservations de véhicules d'entreprise.
 * Vérifie les règles métier: disponibilité des véhicules, conflits avec covoiturages, restrictions de modification.
 * Execute avec Maven Failsafe plugin via la commande: mvn verify
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AnnonceCovoiturageIT {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AnnonceCovoiturageRepository annonceCovoiturageRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private VehiculeEntrepriseRepository vehiculeEntrepriseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Endpoints
    private static final String AUTH_BASE = "/api/auth";
    private static final String AUTH_REGISTER = AUTH_BASE + "/register";
    private static final String AUTH_LOGIN = AUTH_BASE + "/login";
    private static final String COVOIT_BASE   = "/api/covoit";
    private static final String COVOIT_CREATE = COVOIT_BASE + "/create";
    private static final String COVOIT_RESERVE = COVOIT_BASE + "/reserve";


    private static String jwtToken;
    private static Long utilisateurId;
    private static String jwtToken2 = "";
    private static Long utilisateurId2;
    private static Long vehiculeServiceId;
    private static Long annonceId;

    /**
     * Configuration initiale avant tous les tests.
     * Crée un utilisateur de test et un véhicule de service pour les tests.
     * Génère un token JWT pour l'authentification.
     */
    @BeforeAll
    static void setupGlobal(@Autowired UtilisateurRepository userRepo,
                            @Autowired VehiculeEntrepriseRepository vehiculeRepo,
                            @Autowired PasswordEncoder encoder,
                            @Autowired TestRestTemplate template) {
        System.out.println("=== Configuration globale des tests d'intégration ===");

        // Créer un utilisateur de test
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom("John");
        utilisateur.setPrenom("Doe");
        utilisateur.setEmail("reservation.test." + System.currentTimeMillis() + "@example.com");
        utilisateur.setPassword(encoder.encode("TestPass123!"));
        utilisateur.setEstVerifie(true);
        utilisateur.setEstBanni(false);
        utilisateur.setRole(RoleEnum.ROLE_USER);
        utilisateur = userRepo.save(utilisateur);
        utilisateurId = utilisateur.getId();

        // Creer un utilisateur participant
        Utilisateur utilisateur2 = new Utilisateur();
        utilisateur2.setNom("John2");
        utilisateur2.setPrenom("Doe2");
        utilisateur2.setEmail("reservation.test2." + System.currentTimeMillis() + "@example.com");
        utilisateur2.setPassword(encoder.encode("TestPass123!"));
        utilisateur2.setEstVerifie(true);
        utilisateur2.setEstBanni(false);
        utilisateur2.setRole(RoleEnum.ROLE_USER);
        utilisateur2 = userRepo.save(utilisateur2);
        utilisateurId2 = utilisateur2.getId();


        System.out.println("✓ Utilisateur de test créé - ID: " + utilisateurId);

        // Créer un vehicule d'entreprise de test
        VehiculeEntreprise vehicule = new VehiculeEntreprise();
        vehicule.setMarque("Renault");
        vehicule.setModele("Clio");
        vehicule.setImmatriculation("AB-123-CD");
        vehicule.setStatut(StatutVehicule.EN_SERVICE);
        vehicule.setNbPlaces(5);
        vehicule = vehiculeRepo.save(vehicule);
        vehiculeServiceId = vehicule.getId();


        // Obtenir un token JWT
        Map<String, String> loginRequest = Map.of(
                "username", utilisateur.getEmail(),
                "password", "TestPass123!"
        );

        System.out.println("Tentative de connexion avec email: " + utilisateur.getEmail());

        ResponseEntity<Map> loginResponse = template.postForEntity(
                AUTH_LOGIN,
                loginRequest,
                Map.class
        );

        System.out.println("Statut de la connexion: " + loginResponse.getStatusCode());
        System.out.println("Corps de la réponse login: " + loginResponse.getBody());

        if (loginResponse.getStatusCode() == HttpStatus.OK && loginResponse.getBody() != null) {
            jwtToken = (String) loginResponse.getBody().get("jwt");
            System.out.println("✓ Token JWT obtenu: " + (jwtToken != null && !jwtToken.isEmpty() ? "OUI (" + jwtToken.length() + " caractères)" : "NON - TOKEN VIDE OU NULL"));
        } else {
            System.err.println("❌ Échec de l'obtention du token JWT");
        }

        // Obtenir un token JWT pour l'utilisateur 2'
        Map<String, String> loginRequest2 = Map.of(
                "username", utilisateur2.getEmail(),
                "password", "TestPass123!"
        );

        System.out.println("Tentative de connexion avec email: " + utilisateur2.getEmail());

        ResponseEntity<Map> loginResponse2 = template.postForEntity(
                AUTH_LOGIN,
                loginRequest2,
                Map.class
        );

        System.out.println("Statut de la connexion: " + loginResponse2.getStatusCode());
        System.out.println("Corps de la réponse login: " + loginResponse2.getBody());

        if (loginResponse2.getStatusCode() == HttpStatus.OK && loginResponse2.getBody() != null) {
            jwtToken2 = (String) loginResponse2.getBody().get("jwt");
            System.out.println("✓ Token JWT obtenu: " + (jwtToken2 != null && !jwtToken2.isEmpty() ? "OUI (" + jwtToken2.length() + " caractères)" : "NON - TOKEN VIDE OU NULL"));
        } else {
            System.err.println("❌ Échec de l'obtention du token JWT");
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
     * Supprime toutes les données de test créées (réservations, véhicule, utilisateur).
     */
    @AfterAll
    static void nettoyageGlobal(@Autowired AnnonceCovoiturageRepository annonceRepo,
                                @Autowired UtilisateurRepository userRepo,
                                @Autowired VehiculeEntrepriseRepository vehiculeRepo) {
        System.out.println("=== Nettoyage global ===");

        // Supprimer les annonces de test
        if(annonceId != null) {
            annonceRepo.findById(annonceId).ifPresent(annonce -> {
                annonceRepo.delete(annonce);
                System.out.println("Annonce de test supprimé");
            });
        }

        // Supprimer le véhicule de test
        if (vehiculeServiceId != null) {
            vehiculeRepo.findById(vehiculeServiceId).ifPresent(vehicule -> {
                vehiculeRepo.delete(vehicule);
                System.out.println("Vehicule de test supprimé");
            });
        }

        // Supprimer les utilisateurs de test
        if (utilisateurId != null) {
            userRepo.findById(utilisateurId).ifPresent(user -> {
                userRepo.delete(user);
                System.out.println("✓ Utilisateur de test supprimé");
            });
        }

        if(utilisateurId2 != null) {
            userRepo.findById(utilisateurId2).ifPresent(user -> {
                userRepo.delete(user);
                System.out.println("✓ Utilisateur 2 de test supprimé");
            });
        }
    }

    /**
     * Crée les headers HTTP avec le token JWT pour l'authentification.
     *
     * @return HttpHeaders contenant le token Bearer
     */
    private HttpHeaders createAuthHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        if (jwt != null && !jwt.isEmpty()) {
            headers.set("Authorization", "Bearer " + jwt);
            System.out.println("Header Authorization ajouté: Bearer " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
        } else {
            System.err.println("❌ ERREUR: jwtToken est null ou vide !");
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Test de création d'une annonce de covoiturage avec succès.
     * Vérifie qu'un utilisateur authentifié peut créer une annonce de covoiturage..
     * Valide que la l'annonce est bien créée en base de données avec les bonnes informations.
     */
    @Test
    @Order(1)
    @DisplayName("IT - Création d'une annonce de covoiturage réussie")
    void testCreerAnnonce_success() {
        // Given - Préparation de la réservation
        LocalDateTime dateDepart = LocalDateTime.now().plusDays(7);
        AdresseDto depart = new AdresseDto(null, 10, "Rue des Lilas", "34000", "Montpellier");
        AdresseDto arrivee = new AdresseDto(null, 1, "Avenue de la Paix", "75000", "Paris");

        AnnonceCovoiturageDto annonceDto = AnnonceCovoiturageDto.of(
                null,
                dateDepart,
                90,
                745,
                depart,
                arrivee,
                vehiculeServiceId
        );

        HttpEntity<AnnonceCovoiturageDto> request = new HttpEntity<>(annonceDto, createAuthHeaders(jwtToken));

        // When - Création de la réservation
        ResponseEntity<AnnonceCovoiturageDto> response = restTemplate.exchange(
                COVOIT_CREATE,
                HttpMethod.POST,
                request,
                AnnonceCovoiturageDto.class
        );

        // Then - Vérifications
        System.out.println("Statut de la réponse: " + response.getStatusCode());
        System.out.println("Corps de la réponse: " + response.getBody());

        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Le statut devrait être 201 CREATED");

        assertNotNull(response.getBody(), "Le corps de la réponse ne doit pas être null");
        assertNotNull(response.getBody().id(), "L'ID de l'annonce ne doit pas être null");

        annonceId = response.getBody().id();

        // Vérifier en base de données
        assertTrue(annonceCovoiturageRepository.existsById(annonceId),
                "La réservation devrait exister en base de données");

        System.out.println("✓ Réservation créée avec succès - ID: " + annonceId);
    }

    // ------------------- 2) GET /{id} -------------------
    @Test
    @Order(2)
    @DisplayName("IT - Consultation d'une annonce (200) + places (totales/occupées)")
    void getById_ok() {
        HttpEntity<AnnonceCovoiturageDto> request = new HttpEntity<>(null, createAuthHeaders(jwtToken));

        ResponseEntity<AnnonceCovoiturageAvecPlacesDto> response = restTemplate.exchange(
                COVOIT_BASE + "/" + annonceId,
                HttpMethod.GET,
                request,
                AnnonceCovoiturageAvecPlacesDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(annonceId, response.getBody().annonce().id());
        assertEquals(5, response.getBody().placesTotales()); // nbPlaces du véhicule
        assertEquals(0, response.getBody().placesOccupees()); // personne n'a réservé pour l'instant
    }

    // ------------------- 3) PUT /{id} (sans réservations) -------------------
    @Test
    @Order(3)
    @DisplayName("IT - Modification d'une annonce par le propriétaire (200) si aucune réservation")
    void update_ok_no_reservations() {
        LocalDateTime newDate = LocalDateTime.now().plusDays(10).withSecond(0).withNano(0);
        AdresseDto depart = new AdresseDto(null, 22, "Rue Révisée", "33000", "Bordeaux");
        AdresseDto arrivee = new AdresseDto(null, 5, "Boulevard Modifié", "69000", "Lyon");

        AnnonceCovoiturageDto dto = AnnonceCovoiturageDto.of(
                annonceId, newDate, 120, 800, depart, arrivee, vehiculeServiceId
        );

        HttpEntity<AnnonceCovoiturageDto> request = new HttpEntity<>(dto, createAuthHeaders(jwtToken));
        ResponseEntity<AnnonceCovoiturageDto> response = restTemplate.exchange(
                COVOIT_BASE + "/" + annonceId,
                HttpMethod.PUT,
                request,
                AnnonceCovoiturageDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(annonceId, response.getBody().id());
        assertEquals(120, response.getBody().dureeTrajet());
        assertEquals(800, response.getBody().distance());
    }

    // ------------------- 4) POST /reserve/{id} -------------------
    @Test
    @Order(4)
    @DisplayName("IT - Réserver une place par un autre utilisateur (200) + places occupées = 1")
    void reserve_ok() {
        HttpEntity<AnnonceCovoiturageDto> request = new HttpEntity<>(null, createAuthHeaders(jwtToken2));

        ResponseEntity<String> resp = restTemplate.exchange(
                COVOIT_RESERVE + "/" + annonceId,
                HttpMethod.POST,
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, resp.getStatusCode());

        HttpEntity<AnnonceCovoiturageDto> requestGet = new HttpEntity<>(null, createAuthHeaders(jwtToken));

        // Vérifier places occupées = 1
        ResponseEntity<AnnonceCovoiturageAvecPlacesDto> get = restTemplate.exchange(
                COVOIT_BASE + "/" + annonceId,
                HttpMethod.GET,
                requestGet,
                AnnonceCovoiturageAvecPlacesDto.class
        );
        assertNotNull(get.getBody());
        assertEquals(1, get.getBody().placesOccupees());
    }

    // ------------------- 5) PUT /{id} doit échouer quand il y a une réservation -------------------
    @Test
    @Order(5)
    @DisplayName("IT - Modification refusée si réservation existante (400)")
    void update_ko_when_reserved() {
        LocalDateTime newDate = LocalDateTime.now().plusDays(12).withSecond(0).withNano(0);
        AdresseDto d = new AdresseDto(null, 3, "Rue Impossible", "13000", "Marseille");
        AdresseDto a = new AdresseDto(null, 7, "Cours Refusé", "06000", "Nice");

        AnnonceCovoiturageDto dto = AnnonceCovoiturageDto.of(
                annonceId, newDate, 60, 600, d, a, vehiculeServiceId
        );

        HttpEntity<AnnonceCovoiturageDto> request = new HttpEntity<>(dto, createAuthHeaders(jwtToken));
        ResponseEntity<Void> resp = restTemplate.exchange(
                COVOIT_BASE + "/" + annonceId,
                HttpMethod.PUT,
                request,
                Void.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ------------------- 6) GET /{id}/participants -------------------
    @Test
    @Order(6)
    @DisplayName("IT - Participants (conducteur + 1 passager)")
    void participants_ok() {
        HttpEntity<ParticipantsCovoiturageDto> request = new HttpEntity<>(null, createAuthHeaders(jwtToken));

        ResponseEntity<ParticipantsCovoiturageDto> resp = restTemplate.exchange(
                COVOIT_BASE + "/" + annonceId + "/participants",
                HttpMethod.GET,
                request,
                ParticipantsCovoiturageDto.class
        );

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().conducteur());
        assertNotNull(resp.getBody().passagers());
        assertFalse(resp.getBody().passagers().isEmpty(), "Il doit y avoir au moins 1 passager après réservation");
    }

    // ------------------- 7) GET /mes-reservations (user2) -------------------
    @Test
    @Order(7)
    @DisplayName("IT - Mes réservations (user2) contient l'annonce")
    void mesReservations_user2_contains() {
        HttpEntity<Void> req = new HttpEntity<>(createAuthHeaders(jwtToken2));

        ResponseEntity<List<AnnonceCovoiturageAvecPlacesDto>> resp = restTemplate.exchange(
                COVOIT_BASE + "/mes-reservations",
                HttpMethod.GET,
                req,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().stream().anyMatch(a -> annonceId.equals(a.annonce().id())));
    }

    // ------------------- 8) GET /mes-annonces (user1) -------------------
    @Test
    @Order(8)
    @DisplayName("IT - Mes annonces (user1) contient l'annonce")
    void mesAnnonces_user1_contains() {
        HttpEntity<Void> req = new HttpEntity<>(createAuthHeaders(jwtToken));

        ResponseEntity<List<AnnonceCovoiturageAvecPlacesDto>> resp = restTemplate.exchange(
                COVOIT_BASE + "/mes-annonces",
                HttpMethod.GET,
                req,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().stream().anyMatch(a -> annonceId.equals(a.annonce().id())));
    }

    // ------------------- 9) GET / (liste toutes annonces) -------------------
    @Test
    @Order(9)
    @DisplayName("IT - Liste toutes les annonces")
    void list_all_contains() {
        HttpEntity<Void> req = new HttpEntity<>(createAuthHeaders(jwtToken));

        ResponseEntity<List<AnnonceCovoiturageAvecPlacesDto>> resp = restTemplate.exchange(
                COVOIT_BASE + "/",
                HttpMethod.GET,
                req,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().stream().anyMatch(a -> annonceId.equals(a.annonce().id())));
    }

    // ------------------- 10) DELETE /reserve/{id} (user2) -------------------
    @Test
    @Order(10)
    @DisplayName("IT - Annulation de réservation par user2 (200) + places occupées = 0")
    void cancel_reservation_ok() {
        HttpEntity<Void> req = new HttpEntity<>(createAuthHeaders(jwtToken2));

        ResponseEntity<String> resp = restTemplate.exchange(
                COVOIT_RESERVE + "/" + annonceId,
                HttpMethod.DELETE,
                req,
                String.class
        );

        assertEquals(HttpStatus.OK, resp.getStatusCode());

        HttpEntity<Void> req2 = new HttpEntity<>(createAuthHeaders(jwtToken));

        ResponseEntity<AnnonceCovoiturageAvecPlacesDto> get = restTemplate.exchange(
                COVOIT_BASE + "/" + annonceId,
                HttpMethod.GET,
                req2,
                AnnonceCovoiturageAvecPlacesDto.class
        );
        assertNotNull(get.getBody());
        assertEquals(0, get.getBody().placesOccupees());
    }

    // ------------------- 11) DELETE /{id} par le propriétaire -------------------
    @Test
    @Order(11)
    @DisplayName("IT - Suppression d'une annonce par le propriétaire (204)")
    void delete_ok_owner() {
        HttpEntity<Void> req = new HttpEntity<>(createAuthHeaders(jwtToken));
        ResponseEntity<Void> resp = restTemplate.exchange(
                COVOIT_BASE + "/" + annonceId,
                HttpMethod.DELETE,
                req,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        assertFalse(annonceCovoiturageRepository.existsById(annonceId));
    }
}
