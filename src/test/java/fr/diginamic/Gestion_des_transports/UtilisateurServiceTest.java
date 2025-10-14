package fr.diginamic.Gestion_des_transports;


import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.repositories.UtilisateurRepository;
import fr.diginamic.Gestion_des_transports.services.AdresseService;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import fr.diginamic.Gestion_des_transports.tools.EmailSender;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private AdresseService adresseService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private UtilisateurService utilisateurService;

    @BeforeAll
    static void beforeAll() {
        System.out.println("Début de la campagne de tests ");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Fin de la campagne de tests   ");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("créer des éléments nécessaires à tous les tests, par exemple un faux référentiel");
    }

    @AfterEach
    void afterEach() {
        System.out.println("choses claires qui ont été créées dans BeforeEach    Optionell");
    }

    @Test
    void obtenirUtilisateurParId_ShoudFindUserById() {

        Utilisateur utilisateurFactice = new Utilisateur();
        utilisateurFactice.setId(1L);
        utilisateurFactice.setNom("Dupont");

        // Mock - simuler le comportement du repository
        when(utilisateurRepository.findById(anyLong())).thenReturn(Optional.of(utilisateurFactice));

        // Act - exécuter du code réel du service
        Utilisateur resultat = utilisateurService.obtenirUtilisateurParId(1L);

        // Assert - vérifier le résultat
        assertNotNull(resultat);
        assertEquals("Dupont", resultat.getNom());
    }
}