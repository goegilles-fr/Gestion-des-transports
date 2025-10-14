package fr.diginamic.Gestion_des_transports;

import fr.diginamic.Gestion_des_transports.dto.AdresseDto;
import fr.diginamic.Gestion_des_transports.dto.ModifierProfilDto;
import fr.diginamic.Gestion_des_transports.entites.Adresse;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.enums.RoleEnum;
import fr.diginamic.Gestion_des_transports.mapper.ModifierProfilMapper;
import fr.diginamic.Gestion_des_transports.repositories.UtilisateurRepository;
import fr.diginamic.Gestion_des_transports.services.AdresseService;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import fr.diginamic.Gestion_des_transports.tools.EmailSender;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @Mock
    private ModifierProfilMapper modifierProfilMapper;

    @InjectMocks
    private UtilisateurService utilisateurService;

    private Utilisateur utilisateurFactice;
    private Adresse adresseFactice;
    private AdresseDto adresseDtoFactice;

    @BeforeAll
    static void beforeAll() {
        System.out.println("Début de la campagne de tests UtilisateurService");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Fin de la campagne de tests UtilisateurService");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("Préparation des données de test");

        // Créer un utilisateur factice
        utilisateurFactice = new Utilisateur();
        utilisateurFactice.setId(1L);
        utilisateurFactice.setNom("Dupont");
        utilisateurFactice.setPrenom("Jean");
        utilisateurFactice.setEmail("jean.dupont@example.com");
        utilisateurFactice.setPassword("motDePasseHache");
        utilisateurFactice.setRole(RoleEnum.ROLE_USER);
        utilisateurFactice.setEstBanni(false);
        utilisateurFactice.setEstVerifie(true);

        // Créer une adresse factice (entité)
        adresseFactice = new Adresse();
        adresseFactice.setId(1L);
        adresseFactice.setNumero(123);
        adresseFactice.setLibelle("Rue de la Paix");
        adresseFactice.setCodePostal("75001");
        adresseFactice.setVille("Paris");

        // Créer une adresse DTO factice
        adresseDtoFactice = new AdresseDto(1L, 123, "Rue de la Paix", "75001", "Paris");
    }

    @AfterEach
    void afterEach() {
        System.out.println("choses claires qui ont été créées dans BeforeEach    Optionell");
    }

    // ============================================
    // Tests pour obtenirUtilisateurParId
    // ============================================

    @Test
    void obtenirUtilisateurParId_ShouldFindUserById() {
        // Arrange
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurFactice));

        // Act
        Utilisateur resultat = utilisateurService.obtenirUtilisateurParId(1L);

        // Assert
        assertNotNull(resultat);
        assertEquals("Dupont", resultat.getNom());
        assertEquals("Jean", resultat.getPrenom());
        verify(utilisateurRepository, times(1)).findById(1L);
    }

    @Test
    void obtenirUtilisateurParId_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.obtenirUtilisateurParId(999L));

        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(utilisateurRepository, times(1)).findById(999L);
    }

    // ============================================
    // Tests pour inscrireUtilisateur
    // ============================================

    @Test
    void inscrireUtilisateur_ShouldCreateNewUserSuccessfully() {
        // Arrange
        when(utilisateurRepository.existsByEmail("nouveau@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("motDePasseHache");
        when(adresseService.creerAdresse(any(Adresse.class))).thenReturn(adresseFactice);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurFactice);
        doNothing().when(emailSender).send(anyString(), anyString(), anyString(), anyString());

        // Act
        Utilisateur resultat = utilisateurService.inscrireUtilisateur(
                "Nouveau", "User", "nouveau@example.com", "password123", adresseFactice
        );

        // Assert
        assertNotNull(resultat);
        verify(utilisateurRepository, times(1)).existsByEmail("nouveau@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
        verify(emailSender, times(1)).send(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void inscrireUtilisateur_ShouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        when(utilisateurRepository.existsByEmail("jean.dupont@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.inscrireUtilisateur(
                        "Dupont", "Jean", "jean.dupont@example.com", "password", null
                ));

        assertEquals("Un utilisateur avec cet email existe déjà", exception.getMessage());
        verify(utilisateurRepository, times(1)).existsByEmail("jean.dupont@example.com");
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void inscrireUtilisateur_ShouldCreateUserWithoutAddress() {
        // Arrange
        when(utilisateurRepository.existsByEmail("nouveau@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("motDePasseHache");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurFactice);
        doNothing().when(emailSender).send(anyString(), anyString(), anyString(), anyString());

        // Act
        Utilisateur resultat = utilisateurService.inscrireUtilisateur(
                "Nouveau", "User", "nouveau@example.com", "password123", null
        );

        // Assert
        assertNotNull(resultat);
        verify(adresseService, never()).creerAdresse(any(Adresse.class));
    }

    // ============================================
    // Tests pour obtenirUtilisateurParEmail
    // ============================================

    @Test
    void obtenirUtilisateurParEmail_ShouldFindUserByEmail() {
        // Arrange
        when(utilisateurRepository.findByEmail("jean.dupont@example.com"))
                .thenReturn(Optional.of(utilisateurFactice));

        // Act
        Utilisateur resultat = utilisateurService.obtenirUtilisateurParEmail("jean.dupont@example.com");

        // Assert
        assertNotNull(resultat);
        assertEquals("jean.dupont@example.com", resultat.getEmail());
        verify(utilisateurRepository, times(1)).findByEmail("jean.dupont@example.com");
    }

    @Test
    void obtenirUtilisateurParEmail_ShouldThrowExceptionWhenEmailNotFound() {
        // Arrange
        when(utilisateurRepository.findByEmail("inconnu@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.obtenirUtilisateurParEmail("inconnu@example.com"));

        assertEquals("Utilisateur non trouvé avec email: inconnu@example.com", exception.getMessage());
        verify(utilisateurRepository, times(1)).findByEmail("inconnu@example.com");
    }

    // ============================================
    // Tests pour obtenirProfilUtilisateur
    // ============================================

    @Test
    void obtenirProfilUtilisateur_ShouldReturnUserProfile() {
        // Arrange
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurFactice));

        // Act
        Utilisateur resultat = utilisateurService.obtenirProfilUtilisateur(1L);

        // Assert
        assertNotNull(resultat);
        assertEquals(1L, resultat.getId());
        verify(utilisateurRepository, times(1)).findById(1L);
    }

    @Test
    void obtenirProfilUtilisateur_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.obtenirProfilUtilisateur(999L));

        assertEquals("Utilisateur non trouvé", exception.getMessage());
    }

    // ============================================
    // Tests pour mettreAJourProfil
    // ============================================

    @Test
    void mettreAJourProfil_ShouldUpdateUserProfile() {
        // Arrange
        utilisateurFactice.setAdresse(adresseFactice);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurFactice));
        when(utilisateurRepository.existsByEmail("nouveau@example.com")).thenReturn(false);
        when(adresseService.mettreAJourAdresse(anyLong(), any(Adresse.class))).thenReturn(adresseFactice);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurFactice);

        // Act
        Utilisateur resultat = utilisateurService.mettreAJourProfil(
                1L, "NouveauNom", "NouveauPrenom", "nouveau@example.com", adresseFactice
        );

        // Assert
        assertNotNull(resultat);
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void mettreAJourProfil_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.mettreAJourProfil(999L, "Nom", "Prenom", null, null));

        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void mettreAJourProfil_ShouldThrowExceptionWhenEmailAlreadyUsed() {
        // Arrange
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurFactice));
        when(utilisateurRepository.existsByEmail("existe@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.mettreAJourProfil(
                        1L, "Nom", "Prenom", "existe@example.com", null
                ));

        assertEquals("Cet email est déjà utilisé par un autre utilisateur", exception.getMessage());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void mettreAJourProfil_ShouldCreateNewAddressWhenUserHasNoAddress() {
        // Arrange
        utilisateurFactice.setAdresse(null);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurFactice));
        when(adresseService.creerAdresse(any(Adresse.class))).thenReturn(adresseFactice);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurFactice);

        // Act
        Utilisateur resultat = utilisateurService.mettreAJourProfil(
                1L, null, null, null, adresseFactice
        );

        // Assert
        assertNotNull(resultat);
        verify(adresseService, times(1)).creerAdresse(any(Adresse.class));
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void mettreAJourProfil_ShouldUpdateWithoutChangingAddress() {
        // Arrange
        utilisateurFactice.setAdresse(adresseFactice);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurFactice));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurFactice);

        // Act
        Utilisateur resultat = utilisateurService.mettreAJourProfil(
                1L, "NouveauNom", "NouveauPrenom", null, null
        );

        // Assert
        assertNotNull(resultat);
        verify(adresseService, never()).creerAdresse(any(Adresse.class));
        verify(adresseService, never()).mettreAJourAdresse(anyLong(), any(Adresse.class));
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    // ============================================
    // Tests pour rechercherUtilisateurs
    // ============================================

    @Test
    void rechercherUtilisateurs_ShouldReturnPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Utilisateur> utilisateurs = Arrays.asList(utilisateurFactice);
        Page<Utilisateur> page = new PageImpl<>(utilisateurs, pageable, 1);

        when(utilisateurRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Utilisateur> resultat = utilisateurService.rechercherUtilisateurs("Dupont", pageable);

        // Assert
        assertNotNull(resultat);
        assertEquals(1, resultat.getTotalElements());
        verify(utilisateurRepository, times(1)).findAll(pageable);
    }

    @Test
    void rechercherUtilisateurs_ShouldReturnAllUsersWhenSearchIsEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Utilisateur> utilisateurs = Arrays.asList(utilisateurFactice);
        Page<Utilisateur> page = new PageImpl<>(utilisateurs, pageable, 1);

        when(utilisateurRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Utilisateur> resultat = utilisateurService.rechercherUtilisateurs("", pageable);

        // Assert
        assertNotNull(resultat);
        verify(utilisateurRepository, times(1)).findAll(pageable);
    }

    // ============================================
    // Tests pour obtenirUtilisateursParRole
    // ============================================

    @Test
    void obtenirUtilisateursParRole_ShouldReturnUsersByRole() {
        // Arrange
        List<Utilisateur> utilisateurs = Arrays.asList(utilisateurFactice);
        when(utilisateurRepository.findByRole(RoleEnum.ROLE_USER)).thenReturn(utilisateurs);

        // Act
        List<Utilisateur> resultat = utilisateurService.obtenirUtilisateursParRole(RoleEnum.ROLE_USER);

        // Assert
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        verify(utilisateurRepository, times(1)).findByRole(RoleEnum.ROLE_USER);
    }

    // ============================================
    // Tests pour bannirUtilisateur
    // ============================================

    @Test
    void bannirUtilisateur_ShouldBanUser() {
        // Arrange
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurFactice));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurFactice);

        // Act
        Utilisateur resultat = utilisateurService.bannirUtilisateur(1L, true);

        // Assert
        assertNotNull(resultat);
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void bannirUtilisateur_ShouldUnbanUser() {
        // Arrange
        utilisateurFactice.setEstBanni(true);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurFactice));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurFactice);

        // Act
        Utilisateur resultat = utilisateurService.bannirUtilisateur(1L, false);

        // Assert
        assertNotNull(resultat);
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void bannirUtilisateur_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.bannirUtilisateur(999L, true));

        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    // ============================================
    // Tests pour verifierUtilisateur
    // ============================================

    @Test
    void verifierUtilisateur_ShouldVerifyUser() {
        // Arrange
        utilisateurFactice.setEstVerifie(false);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurFactice));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurFactice);

        // Act
        Utilisateur resultat = utilisateurService.verifierUtilisateur(1L, true);

        // Assert
        assertNotNull(resultat);
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void verifierUtilisateur_ShouldUnverifyUser() {
        // Arrange
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurFactice));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurFactice);

        // Act
        Utilisateur resultat = utilisateurService.verifierUtilisateur(1L, false);

        // Assert
        assertNotNull(resultat);
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void verifierUtilisateur_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.verifierUtilisateur(999L, true));

        assertEquals("Utilisateur non trouvé", exception.getMessage());
    }

    // ============================================
    // Tests pour obtenirUtilisateursBannis
    // ============================================

    @Test
    void obtenirUtilisateursBannis_ShouldReturnBannedUsers() {
        // Arrange
        utilisateurFactice.setEstBanni(true);
        List<Utilisateur> utilisateursBannis = Arrays.asList(utilisateurFactice);
        when(utilisateurRepository.findByEstBanni(true)).thenReturn(utilisateursBannis);

        // Act
        List<Utilisateur> resultat = utilisateurService.obtenirUtilisateursBannis();

        // Assert
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertTrue(resultat.getFirst().getEstBanni());
        verify(utilisateurRepository, times(1)).findByEstBanni(true);
    }

    // ============================================
    // Tests pour obtenirUtilisateursNonVerifies
    // ============================================

    @Test
    void obtenirUtilisateursNonVerifies_ShouldReturnUnverifiedUsers() {
        // Arrange
        utilisateurFactice.setEstVerifie(false);
        List<Utilisateur> utilisateursNonVerifies = Arrays.asList(utilisateurFactice);
        when(utilisateurRepository.findByEstVerifie(false)).thenReturn(utilisateursNonVerifies);

        // Act
        List<Utilisateur> resultat = utilisateurService.obtenirUtilisateursNonVerifies();

        // Assert
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertFalse(resultat.getFirst().getEstVerifie());
        verify(utilisateurRepository, times(1)).findByEstVerifie(false);
    }

    // ============================================
    // Tests pour obtenirTousLesUtilisateurs
    // ============================================

    @Test
    void obtenirTousLesUtilisateurs_ShouldReturnAllUsers() {
        // Arrange
        Utilisateur utilisateur2 = new Utilisateur();
        utilisateur2.setId(2L);
        utilisateur2.setNom("Martin");

        List<Utilisateur> tousLesUtilisateurs = Arrays.asList(utilisateurFactice, utilisateur2);
        when(utilisateurRepository.findAll()).thenReturn(tousLesUtilisateurs);

        // Act
        List<Utilisateur> resultat = utilisateurService.obtenirTousLesUtilisateurs();

        // Assert
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(utilisateurRepository, times(1)).findAll();
    }

    // ============================================
    // Tests pour modifierProfilUtilisateur
    // ============================================

    @Test
    void modifierProfilUtilisateur_ShouldUpdateProfileWithoutPassword() {
        // Arrange
        ModifierProfilDto dto = new ModifierProfilDto("NouveauNom", "NouveauPrenom", adresseDtoFactice, null);
        when(utilisateurRepository.findByEmail("jean.dupont@example.com"))
                .thenReturn(Optional.of(utilisateurFactice));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurFactice);
        doNothing().when(modifierProfilMapper).mettreAJourProfil(any(), any());

        // Act
        Utilisateur resultat = utilisateurService.modifierProfilUtilisateur(
                "jean.dupont@example.com", dto
        );

        // Assert
        assertNotNull(resultat);
        verify(modifierProfilMapper, times(1)).mettreAJourProfil(any(), any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void modifierProfilUtilisateur_ShouldUpdateProfileWithPassword() {
        // Arrange
        ModifierProfilDto dto = new ModifierProfilDto("NouveauNom", "NouveauPrenom", adresseDtoFactice, "nouveauMotDePasse");
        when(utilisateurRepository.findByEmail("jean.dupont@example.com"))
                .thenReturn(Optional.of(utilisateurFactice));
        when(passwordEncoder.encode("nouveauMotDePasse")).thenReturn("nouveauMotDePasseHache");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurFactice);
        doNothing().when(modifierProfilMapper).mettreAJourProfil(any(), any());

        // Act
        Utilisateur resultat = utilisateurService.modifierProfilUtilisateur(
                "jean.dupont@example.com", dto
        );

        // Assert
        assertNotNull(resultat);
        verify(passwordEncoder, times(1)).encode("nouveauMotDePasse");
        verify(modifierProfilMapper, times(1)).mettreAJourProfil(any(), any());
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void modifierProfilUtilisateur_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        ModifierProfilDto dto = new ModifierProfilDto("Nom", "Prenom", null, null);
        when(utilisateurRepository.findByEmail("inconnu@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.modifierProfilUtilisateur("inconnu@example.com", dto));

        assertEquals("Utilisateur non trouvé avec email: inconnu@example.com", exception.getMessage());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }
}