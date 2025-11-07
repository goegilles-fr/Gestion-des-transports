package fr.diginamic.gestiondestransports;

import fr.diginamic.gestiondestransports.dto.ReservationVehiculeDTO;
import fr.diginamic.gestiondestransports.entites.VehiculeEntreprise;
import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import fr.diginamic.gestiondestransports.repositories.ReservationVehiculeRepository;
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
 * Tests d'intégration pour les réservations de véhicules de service.
 * Utilise une vraie base de données (TEST_covoit) et teste l'ensemble de la pile applicative.
 * Teste la création, modification, consultation et suppression des réservations de véhicules d'entreprise.
 * Vérifie les règles métier: disponibilité des véhicules, conflits avec covoiturages, restrictions de modification.
 * Execute avec Maven Failsafe plugin via la commande: mvn verify
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservationVehiculeIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ReservationVehiculeRepository reservationRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private VehiculeEntrepriseRepository vehiculeEntrepriseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String BASE_URL = "/api/reservations-vehicules";
    private static String jwtToken;
    private static Long utilisateurId;
    private static Long vehiculeId;
    private static Long reservationId;
    private static String emailUtilisateur;

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

        // Créer un utilisateur de test via l'endpoint d'inscription
        emailUtilisateur = "reservation.test." + System.currentTimeMillis() + "@example.com";

        Map<String, Object> registrationRequest = Map.of(
                "nom", "TestReservation",
                "prenom", "User",
                "email", emailUtilisateur,
                "password", "TestPass123!",
                "adresse", Map.of(
                        "numero", 1,
                        "libelle", "Rue Test",
                        "codePostal", "34000",
                        "ville", "Montpellier"
                )
        );

        ResponseEntity<Map> registrationResponse = template.postForEntity(
                "/api/auth/register",
                registrationRequest,
                Map.class
        );

        if (registrationResponse.getStatusCode() == HttpStatus.CREATED && registrationResponse.getBody() != null) {
            utilisateurId = ((Number) registrationResponse.getBody().get("userId")).longValue();
            System.out.println("✓ Utilisateur de test créé via endpoint - ID: " + utilisateurId);

            // COMPROMIS : Modifier directement en base pour vérifier l'utilisateur
            userRepo.findById(utilisateurId).ifPresent(user -> {
                user.setEstVerifie(true);
                userRepo.save(user);
                System.out.println("✓ Utilisateur vérifié (via repo - compromis pour les tests)");
            });
        } else {
            System.err.println("❌ Échec de la création de l'utilisateur via endpoint");
        }

        // Créer un véhicule d'entreprise disponible
        VehiculeEntreprise vehicule = new VehiculeEntreprise();
        vehicule.setMarque("Renault");
        vehicule.setModele("Kangoo");
        vehicule.setImmatriculation("TEST-" + System.currentTimeMillis());
        vehicule.setNbPlaces(5);
        vehicule.setStatut(StatutVehicule.EN_SERVICE);
        vehicule.setPhoto("https://example.com/vehicule.jpg");
        vehicule = vehiculeRepo.save(vehicule);
        vehiculeId = vehicule.getId();

        System.out.println("✓ Véhicule de service créé - ID: " + vehiculeId);

        // Obtenir un token JWT
        Map<String, String> loginRequest = Map.of(
                "username", emailUtilisateur,
                "password", "TestPass123!"
        );

        System.out.println("Tentative de connexion avec email: " + emailUtilisateur);

        ResponseEntity<Map> loginResponse = template.postForEntity(
                "/api/auth/login",
                loginRequest,
                Map.class
        );

        System.out.println("Statut de la connexion: " + loginResponse.getStatusCode());
        System.out.println("Corps de la réponse login: " + loginResponse.getBody());

        if (loginResponse.getStatusCode() == HttpStatus.OK && loginResponse.getBody() != null) {
            jwtToken = (String) loginResponse.getBody().get("jwt");
            System.out.println("✓ Token JWT obtenu: OUI (" + jwtToken.length() + " caractères)");
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
    static void nettoyageGlobal(@Autowired ReservationVehiculeRepository resaRepo,
                                @Autowired UtilisateurRepository userRepo,
                                @Autowired VehiculeEntrepriseRepository vehiculeRepo,
                                @Autowired TestRestTemplate template) {
        System.out.println("=== Nettoyage global ===");

        // Supprimer les réservations via l'endpoint DELETE
        if (reservationId != null && jwtToken != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            try {
                template.exchange(
                        BASE_URL + "/" + reservationId,
                        HttpMethod.DELETE,
                        request,
                        Void.class
                );
                System.out.println("✓ Réservation de test supprimée via endpoint DELETE");
            } catch (Exception e) {
                System.out.println("⚠ Réservation déjà supprimée ou inexistante");
            }
        }

        // Supprimer le véhicule via le repository
        if (vehiculeId != null) {
            vehiculeRepo.findById(vehiculeId).ifPresent(vehicule -> {
                vehiculeRepo.delete(vehicule);
                System.out.println("✓ Véhicule de test supprimé via repo");
            });
        }

        // Supprimer l'utilisateur de test via le repository
        if (emailUtilisateur != null) {
            userRepo.findByEmail(emailUtilisateur).ifPresent(user -> {
                userRepo.delete(user);
                System.out.println("✓ Utilisateur de test supprimé via repo: " + emailUtilisateur);
            });
        }
    }

    /**
     * Crée les headers HTTP avec le token JWT pour l'authentification.
     *
     * @return HttpHeaders contenant le token Bearer
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (jwtToken != null && !jwtToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + jwtToken);
            System.out.println("Header Authorization ajouté");
        } else {
            System.err.println("❌ ERREUR: jwtToken est null ou vide !");
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Test de création d'une réservation de véhicule avec succès.
     * Vérifie qu'un utilisateur authentifié peut réserver un véhicule disponible.
     * Valide que la réservation est bien créée en base de données avec les bonnes informations.
     */
    @Test
    @Order(1)
    @DisplayName("IT - Création d'une réservation de véhicule réussie")
    void testCreerReservation_success() {
        // Given - Préparation de la réservation
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(1);
        LocalDateTime dateFin = dateDebut.plusHours(4);

        ReservationVehiculeDTO reservationDto = new ReservationVehiculeDTO(
                null,           // id
                null,           // utilisateurId (sera rempli par le service)
                vehiculeId,     // vehiculeId
                dateDebut,      // dateDebut
                dateFin         // dateFin
        );

        HttpEntity<ReservationVehiculeDTO> request = new HttpEntity<>(reservationDto, createAuthHeaders());

        // When - Création de la réservation
        ResponseEntity<ReservationVehiculeDTO> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.POST,
                request,
                ReservationVehiculeDTO.class
        );

        // Then - Vérifications
        System.out.println("Statut de la réponse: " + response.getStatusCode());
        System.out.println("Corps de la réponse: " + response.getBody());

        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Le statut devrait être 201 CREATED");

        assertNotNull(response.getBody(), "Le corps de la réponse ne doit pas être null");
        assertNotNull(response.getBody().id(), "L'ID de la réservation ne doit pas être null");

        reservationId = response.getBody().id();

        // Vérifier en base de données
        assertTrue(reservationRepository.existsById(reservationId),
                "La réservation devrait exister en base de données");

        System.out.println("✓ Réservation créée avec succès - ID: " + reservationId);
    }

    /**
     * Test de récupération de toutes les réservations.
     * Vérifie qu'un utilisateur authentifié peut obtenir la liste de toutes les réservations.
     */
    @Test
    @Order(2)
    @DisplayName("IT - Récupération de toutes les réservations")
    void testObtenirToutesLesReservations_success() {
        // Given - Headers d'authentification
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());

        // When - Récupération de toutes les réservations
        ResponseEntity<List> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.GET,
                request,
                List.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Nombre de réservations: " + response.getBody().size());

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Le statut devrait être 200 OK");

        assertNotNull(response.getBody(), "La liste ne doit pas être null");
        assertFalse(response.getBody().isEmpty(),
                "La liste ne devrait pas être vide (au moins la réservation créée)");

        System.out.println("✓ Récupération de toutes les réservations réussie");
    }

    /**
     * Test de récupération d'une réservation spécifique par son ID.
     * Vérifie que le propriétaire peut consulter sa propre réservation.
     */
    @Test
    @Order(3)
    @DisplayName("IT - Récupération d'une réservation par ID")
    void testObtenirReservationParId_success() {
        // Given - Headers d'authentification
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());

        // When - Récupération de la réservation par ID
        ResponseEntity<ReservationVehiculeDTO> response = restTemplate.exchange(
                BASE_URL + "/" + reservationId,
                HttpMethod.GET,
                request,
                ReservationVehiculeDTO.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Réservation: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Le statut devrait être 200 OK");

        assertNotNull(response.getBody(), "La réservation ne doit pas être null");
        assertEquals(reservationId, response.getBody().id(),
                "L'ID devrait correspondre");

        System.out.println("✓ Récupération de la réservation par ID réussie");
    }

    /**
     * Test de récupération des réservations de l'utilisateur connecté.
     * Vérifie qu'un utilisateur peut obtenir la liste de ses propres réservations.
     */
    @Test
    @Order(4)
    @DisplayName("IT - Récupération des réservations de l'utilisateur connecté")
    void testObtenirReservationsUtilisateur_success() {
        // Given - Headers d'authentification
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());

        // When - Récupération des réservations de l'utilisateur
        ResponseEntity<List> response = restTemplate.exchange(
                BASE_URL + "/utilisateur",
                HttpMethod.GET,
                request,
                List.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Nombre de réservations: " + response.getBody().size());

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Le statut devrait être 200 OK");

        assertNotNull(response.getBody(), "La liste ne doit pas être null");
        assertFalse(response.getBody().isEmpty(),
                "L'utilisateur devrait avoir au moins une réservation");

        System.out.println("✓ Récupération des réservations de l'utilisateur réussie");
    }

    /**
     * Test de modification d'une réservation existante.
     * Vérifie que le propriétaire peut modifier les dates de sa réservation.
     */
    @Test
    @Order(5)
    @DisplayName("IT - Modification d'une réservation réussie")
    void testModifierReservation_success() {
        // Given - Nouvelles dates pour la réservation
        LocalDateTime nouvelleDateDebut = LocalDateTime.now().plusDays(2);
        LocalDateTime nouvelleDateFin = nouvelleDateDebut.plusHours(6);

        ReservationVehiculeDTO reservationModifiee = new ReservationVehiculeDTO(
                reservationId,        // id
                null,                 // utilisateurId
                vehiculeId,           // vehiculeId
                nouvelleDateDebut,    // dateDebut
                nouvelleDateFin       // dateFin
        );

        HttpEntity<ReservationVehiculeDTO> request = new HttpEntity<>(reservationModifiee, createAuthHeaders());

        // When - Modification de la réservation
        ResponseEntity<ReservationVehiculeDTO> response = restTemplate.exchange(
                BASE_URL + "/" + reservationId,
                HttpMethod.PUT,
                request,
                ReservationVehiculeDTO.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Réservation modifiée: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Le statut devrait être 200 OK");

        assertNotNull(response.getBody(), "La réservation modifiée ne doit pas être null");
        assertEquals(nouvelleDateDebut.withNano(0), response.getBody().dateDebut().withNano(0),
                "La date de début devrait être mise à jour");

        System.out.println("✓ Modification de la réservation réussie");
    }

    /**
     * Test de recherche d'une réservation pour une période spécifique.
     * Vérifie qu'on peut retrouver une réservation de l'utilisateur pour une date donnée.
     */
    @Test
    @Order(6)
    @DisplayName("IT - Recherche de réservation par période")
    void testRechercherReservationParPeriode_success() {
        // Given - Date et durée pour la recherche
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(2);
        Integer dureeMinutes = 60;

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());

        // When - Recherche de la réservation
        String url = BASE_URL + "/utilisateur/recherche?dateDebut=" + dateDebut + "&dureeMinutes=" + dureeMinutes;
        ResponseEntity<ReservationVehiculeDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                ReservationVehiculeDTO.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Réservation trouvée: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Le statut devrait être 200 OK");

        assertNotNull(response.getBody(), "Une réservation devrait être trouvée");

        System.out.println("✓ Recherche de réservation par période réussie");
    }

    /**
     * Test de récupération des réservations d'un véhicule spécifique.
     * Vérifie qu'on peut obtenir toutes les réservations associées à un véhicule.
     */
    @Test
    @Order(7)
    @DisplayName("IT - Récupération des réservations d'un véhicule")
    void testObtenirReservationsParVehicule_success() {
        // Given - Headers d'authentification
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());

        // When - Récupération des réservations du véhicule
        ResponseEntity<List> response = restTemplate.exchange(
                BASE_URL + "/vehicule/" + vehiculeId,
                HttpMethod.GET,
                request,
                List.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Nombre de réservations pour le véhicule: " + response.getBody().size());

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Le statut devrait être 200 OK");

        assertNotNull(response.getBody(), "La liste ne doit pas être null");
        assertFalse(response.getBody().isEmpty(),
                "Le véhicule devrait avoir au moins une réservation");

        System.out.println("✓ Récupération des réservations du véhicule réussie");
    }

    /**
     * Test de création d'une réservation avec conflit de dates.
     * Vérifie qu'on ne peut pas créer une réservation sur un créneau déjà occupé.
     */
    @Test
    @Order(8)
    @DisplayName("IT - Création d'une réservation avec conflit de dates échoue")
    void testCreerReservation_conflit_echec() {
        // Given - Réservation avec les mêmes dates qu'une réservation existante
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(2);
        LocalDateTime dateFin = dateDebut.plusHours(4);

        ReservationVehiculeDTO reservationDto = new ReservationVehiculeDTO(
                null,           // id
                null,           // utilisateurId
                vehiculeId,     // vehiculeId
                dateDebut,      // dateDebut
                dateFin         // dateFin
        );

        HttpEntity<ReservationVehiculeDTO> request = new HttpEntity<>(reservationDto, createAuthHeaders());

        // When - Tentative de création
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());
        System.out.println("Message d'erreur: " + response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Le statut devrait être 400 BAD_REQUEST pour un conflit");

        System.out.println("✓ Test de conflit de dates validé");
    }

    /**
     * Test de suppression d'une réservation.
     * Vérifie que le propriétaire peut supprimer sa réservation.
     * Ce test doit être exécuté en dernier car il supprime la réservation.
     */
    @Test
    @Order(9)
    @DisplayName("IT - Suppression d'une réservation réussie")
    void testSupprimerReservation_success() {
        // Given - Headers d'authentification
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());

        // When - Suppression de la réservation
        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + "/" + reservationId,
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // Then - Vérifications
        System.out.println("Statut: " + response.getStatusCode());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(),
                "Le statut devrait être 204 NO_CONTENT");

        // Vérifier que la réservation n'existe plus en base
        assertFalse(reservationRepository.existsById(reservationId),
                "La réservation ne devrait plus exister en base de données");

        System.out.println("✓ Suppression de la réservation réussie");
    }

    /**
     * Test d'accès sans authentification.
     * Vérifie qu'on ne peut pas accéder aux réservations sans token JWT.
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