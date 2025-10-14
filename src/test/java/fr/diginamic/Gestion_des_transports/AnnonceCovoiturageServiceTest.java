package fr.diginamic.Gestion_des_transports;

import fr.diginamic.Gestion_des_transports.dto.AdresseDto;
import fr.diginamic.Gestion_des_transports.dto.AnnonceCovoiturageAvecPlacesDto;
import fr.diginamic.Gestion_des_transports.dto.AnnonceCovoiturageDto;
import fr.diginamic.Gestion_des_transports.entites.*;
import fr.diginamic.Gestion_des_transports.enums.RoleEnum;
import fr.diginamic.Gestion_des_transports.enums.StatutVehicule;
import fr.diginamic.Gestion_des_transports.mapper.AdresseMapper;
import fr.diginamic.Gestion_des_transports.mapper.AnnonceCovoiturageMapper;
import fr.diginamic.Gestion_des_transports.repositories.*;
import fr.diginamic.Gestion_des_transports.services.UtilisateurService;
import fr.diginamic.Gestion_des_transports.services.impl.AnnonceCovoiturageServiceImpl;
import fr.diginamic.Gestion_des_transports.tools.EmailSender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnnonceCovoiturageServiceTest {
    @Mock
    UtilisateurService utilisateurService;
    @Mock
    AnnonceCovoiturageRepository annonceCovoiturageRepository;
    @Mock
    AnnonceCovoiturageMapper annonceMapper;
    @Mock
    VehiculeEntrepriseRepository vehiculeEntrepriseRepository;
    @Mock
    VehiculePersonnelRepository vehiculePersonnelRepository;
    @Mock
    AdresseRepository adresseRepository;
    @Mock
    AdresseMapper adresseMapper;
    @Mock
    CovoituragePassagersRepository covoituragePassagersRepository;
    @Mock
    EmailSender emailSender;

    @Spy
    @InjectMocks
    AnnonceCovoiturageServiceImpl service;

    private Utilisateur responsable;
    private Utilisateur user;
    private VehiculeEntreprise vehicule;
    private VehiculePersonnel vehiculePerso;
    private Adresse adresseDepart;
    private Adresse adresseArrivee;
    private AdresseDto adresseDepartDto;
    private AdresseDto adresseArriveeDto;
    private AnnonceCovoiturage annonceExistante;
    private AnnonceCovoiturageDto inputDtoSansVehicule;
    private AnnonceCovoiturageDto inputDtoAvecVehicule;
    private LocalDateTime heureDepart;
    private int duree;
    private int distance;
    private Long idAnnonce;
    private Long idResponsable;
    private Long idUser;

    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(service, "emailSender", emailSender);

        idAnnonce = 123L;
        idResponsable = 42L;
        idUser = 98L;
        heureDepart = LocalDateTime.of(2099, 10, 1, 8, 0);
        duree = 45;
        distance = 100;

        responsable = new Utilisateur("Dupont", "Jean", "jean.dupont@mail.com", RoleEnum.ROLE_USER);
        responsable.setId(idResponsable);

        user = new Utilisateur("Dupont", "Jean", "jean@acme.com", RoleEnum.ROLE_USER);
        user.setId(idUser);

        vehicule = new VehiculeEntreprise(5L, "AA-123-AA", 4, "Renault", 142, null, "Clio", null, null, null);
        vehiculePerso = new VehiculePersonnel(5L, "BB-456-BB", 4, "Renault", 142, null, "Clio", null, null, null);
        vehiculePerso.setUtilisateur(responsable);

        adresseDepart = new Adresse(1, "rue de la paix", "34210", "Montpellier");
        adresseDepart.setId(100L);
        adresseArrivee = new Adresse(10, "avenue foch", "42000", "Nantes");
        adresseArrivee.setId(101L);

        adresseDepartDto = AdresseDto.nouvelle(1, "rue de la paix", "34210", "Montpellier");
        adresseArriveeDto = AdresseDto.nouvelle(10, "avenue foch", "42000", "Nantes");

        annonceExistante = new AnnonceCovoiturage(heureDepart, duree, distance, adresseDepart, adresseArrivee, responsable);
        annonceExistante.setId(idAnnonce);

        inputDtoSansVehicule = new AnnonceCovoiturageDto(null, heureDepart, duree, distance,
                adresseDepartDto, adresseArriveeDto, null);
        inputDtoAvecVehicule = new AnnonceCovoiturageDto(null, heureDepart, duree, distance,
                adresseDepartDto, adresseArriveeDto, vehicule.getId());
    }

    @Test
    @DisplayName("creerAnnonce → OK si vehiculeServiceId est fourni et trouvé")
    void creerAnnonce_ok_vehicule_service_specifie() {
        // GIVEN
        AnnonceCovoiturage entite = new AnnonceCovoiturage();
        when(utilisateurService.obtenirUtilisateurParId(42L)).thenReturn(responsable);
        when(annonceMapper.versEntite(inputDtoAvecVehicule)).thenReturn(entite);

        when(vehiculeEntrepriseRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));

        // save → renvoie l’entité enrichie
        AnnonceCovoiturage saved = new AnnonceCovoiturage(heureDepart, 45, 100, adresseDepart, adresseArrivee, responsable);
        saved.setId(idAnnonce);

        when(annonceCovoiturageRepository.save(any(AnnonceCovoiturage.class))).thenReturn(saved);

        AnnonceCovoiturageDto expected = AnnonceCovoiturageDto.of(
                idAnnonce, heureDepart, 45, 100, adresseDepartDto, adresseArriveeDto, 5L
        );
        when(annonceMapper.versDto(saved)).thenReturn(expected);

        // WHEN
        AnnonceCovoiturageDto out = service.creerAnnonce(inputDtoAvecVehicule, responsable.getId());

        // THEN
        assertEquals(expected, out);

        verify(utilisateurService).obtenirUtilisateurParId(responsable.getId());
        verify(annonceMapper).versEntite(inputDtoAvecVehicule);
        verify(vehiculeEntrepriseRepository).findById(vehicule.getId());
        verify(annonceMapper).versDto(saved);
    }

    @Test
    @DisplayName("creerAnnonce → IllegalArgumentException si vehiculeServiceId inexistant")
    void creerAnnonce_ko_vehicule_introuvable() {
        when(utilisateurService.obtenirUtilisateurParId(responsable.getId())).thenReturn(responsable);
        when(annonceMapper.versEntite(inputDtoAvecVehicule)).thenReturn(new AnnonceCovoiturage());
        when(vehiculeEntrepriseRepository.findById(vehicule.getId())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.creerAnnonce(inputDtoAvecVehicule, responsable.getId()));
        assertTrue(ex.getMessage().toLowerCase().contains("introuvable"));

        verify(utilisateurService).obtenirUtilisateurParId(responsable.getId());
        verify(annonceMapper).versEntite(inputDtoAvecVehicule);
        verify(vehiculeEntrepriseRepository).findById(vehicule.getId());
        verifyNoInteractions(annonceCovoiturageRepository);
    }

    @Test
    @DisplayName("creerAnnonce → OK sans vehiculeServiceId si l’utilisateur possède un véhicule personnel")
    void creerAnnonce_ok_vehicule_personnel_present() {
        when(utilisateurService.obtenirUtilisateurParId(responsable.getId())).thenReturn(responsable);
        when(annonceMapper.versEntite(inputDtoSansVehicule)).thenReturn(new AnnonceCovoiturage());

        when(vehiculePersonnelRepository.findByUtilisateur(responsable))
                .thenReturn(List.of(vehiculePerso));

        AnnonceCovoiturage saved = new AnnonceCovoiturage();
        saved.setId(222L);
        saved.setResponsable(responsable);
        saved.setHeureDepart(heureDepart);
        saved.setDureeTrajet(duree);
        saved.setDistance(distance);
        when(annonceCovoiturageRepository.save(any(AnnonceCovoiturage.class))).thenReturn(saved);

        AnnonceCovoiturageDto expected = AnnonceCovoiturageDto.of(
                222L, heureDepart, duree, distance, adresseDepartDto, adresseArriveeDto, null
        );
        when(annonceMapper.versDto(saved)).thenReturn(expected);

        AnnonceCovoiturageDto out = service.creerAnnonce(inputDtoSansVehicule, responsable.getId());

        assertEquals(expected, out);

        verify(vehiculePersonnelRepository).findByUtilisateur(responsable);
        verify(annonceMapper).versDto(saved);
    }

    @Test
    @DisplayName("creerAnnonce → IllegalArgumentException si aucun véhicule perso et pas de vehiculeServiceId")
    void creerAnnonce_ko_aucun_vehicule_perso_ni_service() {
        when(utilisateurService.obtenirUtilisateurParId(responsable.getId())).thenReturn(responsable);
        when(annonceMapper.versEntite(inputDtoSansVehicule)).thenReturn(new AnnonceCovoiturage());
        when(vehiculePersonnelRepository.findByUtilisateur(responsable))
                .thenReturn(Collections.emptyList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.creerAnnonce(inputDtoSansVehicule, responsable.getId()));
        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("véhicule de service") || msg.contains("véhicule personnel"));

        verify(vehiculePersonnelRepository).findByUtilisateur(responsable);
        verifyNoInteractions(annonceCovoiturageRepository);
    }

    @Test
    @DisplayName("modifierAnnonce → OK : met à jour adresses et véhicule, sauvegarde et retourne DTO")
    void modifierAnnonce_ok_update_addresses_and_vehicle() {
        // Nouveau véhicule
        VehiculeEntreprise v2 = new VehiculeEntreprise();
        v2.setId(7L);
        v2.setStatut(StatutVehicule.EN_SERVICE);
        when(vehiculeEntrepriseRepository.findById(7L)).thenReturn(Optional.of(v2));

        doReturn(0).when(service).obtenirNombrePlacesOccupees(idAnnonce);
        when(adresseMapper.versEntite(adresseDepartDto)).thenReturn(adresseDepart);
        when(adresseMapper.versEntite(adresseArriveeDto)).thenReturn(adresseArrivee);
        lenient().when(adresseRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(adresseRepository.save(any(Adresse.class)))
                .thenAnswer(inv -> {
                    Adresse a = inv.getArgument(0);
                    if (a.getId() == null) a.setId(999L);
                    return a;
                });

        LocalDateTime newDepart = LocalDateTime.of(2099, 10, 1, 9, 0);
        AnnonceCovoiturageDto patch = AnnonceCovoiturageDto.of(
                null,               // id (inutile pour patch, ignoré)
                newDepart,          // heureDepart
                45,                 // dureeTrajet
                12,                 // distance
                adresseDepartDto,          // adresseDepart
                adresseArriveeDto,         // adresseArrivee
                7L                  // vehiculeServiceId -> changer de véhicule
        );

        when(annonceCovoiturageRepository.findById(idAnnonce))
                .thenReturn(Optional.of(annonceExistante));

        // mapper.mettreAJourEntite(...) ne retourne rien : on vérifie qu'il est appelé
        doAnswer(inv -> {
            // On simule une mise à jour des champs simples
            AnnonceCovoiturageDto dto    = inv.getArgument(0);
            AnnonceCovoiturage target    = inv.getArgument(1);
            target.setHeureDepart(dto.heureDepart());
            target.setDureeTrajet(dto.dureeTrajet());
            return null;
        }).when(annonceMapper).mettreAJourEntite(eq(patch), same(annonceExistante));

        // save + mapping de sortie
        AnnonceCovoiturage saved = new AnnonceCovoiturage();
        saved.setId(idAnnonce);
        saved.setResponsable(responsable);
        saved.setAdresseDepart(adresseDepart);
        saved.setAdresseArrivee(adresseArrivee);
        saved.setVehiculeService(v2);
        saved.setHeureDepart(newDepart);
        saved.setDureeTrajet(45);
        when(annonceCovoiturageRepository.save(annonceExistante)).thenReturn(saved);

        AnnonceCovoiturageDto expected = AnnonceCovoiturageDto.of(
                idAnnonce, newDepart, 45, 12, adresseDepartDto, adresseArriveeDto, 7L
        );
        when(annonceMapper.versDto(saved)).thenReturn(expected);

        // WHEN
        AnnonceCovoiturageDto out = service.modifierAnnonce(idAnnonce, patch, responsable.getId());

        // THEN
        assertEquals(expected, out);
        assertSame(adresseDepart, annonceExistante.getAdresseDepart());
        assertSame(adresseArrivee, annonceExistante.getAdresseArrivee());
        assertEquals(7L, annonceExistante.getVehiculeService().getId());
        assertEquals(newDepart, annonceExistante.getHeureDepart());
        assertEquals(45, annonceExistante.getDureeTrajet());

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(service).obtenirNombrePlacesOccupees(idAnnonce);
        verify(vehiculeEntrepriseRepository).findById(7L);
        verify(annonceMapper).mettreAJourEntite(eq(patch), same(annonceExistante));
        verify(annonceCovoiturageRepository).save(annonceExistante);
        verify(annonceMapper).versDto(saved);
    }

    @Test
    @DisplayName("modifierAnnonce → IllegalArgumentException si annonce introuvable")
    void modifierAnnonce_not_found() {
        when(annonceCovoiturageRepository.findById(999L)).thenReturn(Optional.empty());

        AnnonceCovoiturageDto patch = AnnonceCovoiturageDto.of(
                null, LocalDateTime.now().plusDays(1), 30, 10, adresseDepartDto, adresseArriveeDto, null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.modifierAnnonce(999L, patch, responsable.getId()));
        assertTrue(ex.getMessage().toLowerCase().contains("introuvable"));

        verify(annonceCovoiturageRepository).findById(999L);
        verifyNoMoreInteractions(annonceCovoiturageRepository);
    }

    @Test
    @DisplayName("modifierAnnonce → IllegalArgumentException si utilisateur ≠ responsable")
    void modifierAnnonce_user_not_owner() {
        Utilisateur autre = new Utilisateur("Martin", "Alice", "alice@mail.com", RoleEnum.ROLE_USER);
        autre.setId(77L);

        AnnonceCovoiturage notOwnerAnnonce = new AnnonceCovoiturage(heureDepart, duree, distance, adresseDepart, adresseArrivee, autre);
        notOwnerAnnonce.setId(idAnnonce);

        AnnonceCovoiturageDto patch = AnnonceCovoiturageDto.of(
                null, LocalDateTime.now().plusDays(1), 30, 10, adresseDepartDto, adresseArriveeDto, null
        );

        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(notOwnerAnnonce));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.modifierAnnonce(idAnnonce, patch, responsable.getId()));
        assertTrue(ex.getMessage().toLowerCase().contains("pas autoris"));

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(service, never()).obtenirNombrePlacesOccupees(anyLong());
        verifyNoMoreInteractions(annonceCovoiturageRepository);
    }

    @Test
    @DisplayName("modifierAnnonce → IllegalArgumentException si des passagers ont déjà réservé")
    void modifierAnnonce_places_already_taken() {
        doReturn(3).when(service).obtenirNombrePlacesOccupees(idAnnonce); // 3 places prises
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonceExistante));

        AnnonceCovoiturageDto patch = AnnonceCovoiturageDto.of(
                null, LocalDateTime.now().plusDays(1), 30, 10, adresseDepartDto, adresseArriveeDto, null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.modifierAnnonce(idAnnonce, patch, responsable.getId()));
        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("impossible de modifier") || msg.contains("déjà réservé"));

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(service).obtenirNombrePlacesOccupees(idAnnonce);
        verifyNoMoreInteractions(annonceCovoiturageRepository);
    }

    @Test
    @DisplayName("modifierAnnonce → IllegalArgumentException si vehiculeServiceId inexistant")
    void modifierAnnonce_vehicle_not_found() {
        when(vehiculeEntrepriseRepository.findById(99L)).thenReturn(Optional.empty());
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonceExistante));
        doReturn(0).when(service).obtenirNombrePlacesOccupees(idAnnonce);

        AnnonceCovoiturageDto patch = AnnonceCovoiturageDto.of(
                null, LocalDateTime.now().plusDays(1), 30, 10, adresseDepartDto, adresseArriveeDto, 99L
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.modifierAnnonce(idAnnonce, patch, responsable.getId()));
        assertTrue(ex.getMessage().toLowerCase().contains("introuvable"));

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(service).obtenirNombrePlacesOccupees(idAnnonce);
        verify(vehiculeEntrepriseRepository).findById(99L);
        verify(annonceCovoiturageRepository, never()).save(any());
    }

    @Test
    @DisplayName("modifierAnnonce → OK : vehiculeServiceId null supprime le véhicule de service")
    void modifierAnnonce_ok_remove_vehicle_when_null() {
        // on place un véhicule existant sur l’annonce
        VehiculeEntreprise actuel = new VehiculeEntreprise();
        actuel.setId(5L);
        annonceExistante.setVehiculeService(actuel);

        AnnonceCovoiturageDto patch = AnnonceCovoiturageDto.of(
                null, LocalDateTime.now().plusDays(1), 40, 15, adresseDepartDto, adresseArriveeDto, null
        );

        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonceExistante));
        doReturn(0).when(service).obtenirNombrePlacesOccupees(idAnnonce);

        // mettreAJourEntite simule la màj des champs simples
        doAnswer(inv -> {
            AnnonceCovoiturageDto dto = inv.getArgument(0);
            AnnonceCovoiturage target = inv.getArgument(1);
            target.setHeureDepart(dto.heureDepart());
            target.setDureeTrajet(dto.dureeTrajet());
            return null;
        }).when(annonceMapper).mettreAJourEntite(eq(patch), same(annonceExistante));

        // save & retour
        when(annonceCovoiturageRepository.save(annonceExistante)).thenAnswer(inv -> annonceExistante);
        when(annonceMapper.versDto(annonceExistante)).thenReturn(
                AnnonceCovoiturageDto.of(idAnnonce, patch.heureDepart(), patch.dureeTrajet(),
                        patch.distance(), adresseDepartDto, adresseArriveeDto, null)
        );

        AnnonceCovoiturageDto out = service.modifierAnnonce(idAnnonce, patch, responsable.getId());

        assertNull(annonceExistante.getVehiculeService()); // retiré
        assertEquals(patch.heureDepart(), annonceExistante.getHeureDepart());
        assertEquals(patch.dureeTrajet(), annonceExistante.getDureeTrajet());
        assertEquals(null, out.vehiculeServiceId());

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(service).obtenirNombrePlacesOccupees(idAnnonce);
        verify(annonceMapper).mettreAJourEntite(eq(patch), same(annonceExistante));
        verify(annonceCovoiturageRepository).save(annonceExistante);
        verify(annonceMapper).versDto(annonceExistante);
        verify(vehiculeEntrepriseRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("supprimerAnnonce → OK (aucun passager) : pas d’email, suppression effectuée")
    void supprimerAnnonce_ok_sans_passagers() {
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonceExistante));
        when(covoituragePassagersRepository.findByAnnonceCovoiturageId(idAnnonce))
                .thenReturn(List.of());

        service.supprimerAnnonce(idAnnonce, responsable.getId());

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(covoituragePassagersRepository).findByAnnonceCovoiturageId(idAnnonce);
        verify(emailSender, never()).send(anyString(), anyString(), anyString(), anyString());
        verify(annonceCovoiturageRepository).delete(annonceExistante);
        verifyNoMoreInteractions(annonceCovoiturageRepository, covoituragePassagersRepository, emailSender);
    }

    @Test
    @DisplayName("supprimerAnnonce → OK (avec passagers) : envoi d’emails puis suppression")
    void supprimerAnnonce_ok_avec_passagers() {
        // Deux passagers inscrits
        Utilisateur u1 = new Utilisateur("Martin", "Alice", "alice@mail.com", RoleEnum.ROLE_USER); u1.setId(1L);
        Utilisateur u2 = new Utilisateur("Durand", "Bob",   "bob@mail.com",   RoleEnum.ROLE_USER); u2.setId(2L);

        CovoituragePassagers p1 = new CovoituragePassagers();
        p1.setUtilisateur(u1);
        CovoituragePassagers p2 = new CovoituragePassagers();
        p2.setUtilisateur(u2);

        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonceExistante));
        when(covoituragePassagersRepository.findByAnnonceCovoiturageId(idAnnonce))
                .thenReturn(List.of(p1, p2));

        service.supprimerAnnonce(idAnnonce, responsable.getId());

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(covoituragePassagersRepository).findByAnnonceCovoiturageId(idAnnonce);

        // 2 emails envoyés (on ne vérifie pas le contenu exact, seulement le destinataire + invocation)
        verify(emailSender, times(1)).send(eq("alice@mail.com"), anyString(), anyString(), anyString());
        verify(emailSender, times(1)).send(eq("bob@mail.com"),   anyString(), anyString(), anyString());

        verify(annonceCovoiturageRepository).delete(annonceExistante);
        verifyNoMoreInteractions(annonceCovoiturageRepository, covoituragePassagersRepository, emailSender);
    }

    @Test
    @DisplayName("supprimerAnnonce → KO annonce introuvable")
    void supprimerAnnonce_ko_introuvable() {
        when(annonceCovoiturageRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.supprimerAnnonce(999L, responsable.getId()));
        assertTrue(ex.getMessage().toLowerCase().contains("introuvable"));

        verify(annonceCovoiturageRepository).findById(999L);
        verifyNoInteractions(covoituragePassagersRepository, emailSender);
        verify(annonceCovoiturageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("supprimerAnnonce → KO utilisateur ≠ responsable : pas d’email, pas de suppression")
    void supprimerAnnonce_ko_not_owner() {
        // annonce existe mais propriétaire différent
        Utilisateur autre = new Utilisateur("X", "Y", "z@mail.com", RoleEnum.ROLE_USER);
        autre.setId(77L);
        AnnonceCovoiturage notOwnerAnnonce = new AnnonceCovoiturage(heureDepart, duree, distance, adresseDepart, adresseArrivee, autre);
        annonceExistante.setId(idAnnonce);

        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(notOwnerAnnonce));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.supprimerAnnonce(idAnnonce, responsable.getId()));

        assertTrue(ex.getMessage().contains("autoris"));

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verifyNoInteractions(covoituragePassagersRepository, emailSender);
        verify(annonceCovoiturageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("obtenirAnnonceParId → OK : retourne le DTO mappé")
    void obtenirAnnonceParId_ok() {
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonceExistante));

        LocalDateTime depart = LocalDateTime.now().plusDays(1);
        AdresseDto dep = new AdresseDto(null, 1, "rue de la paix", "75001", "Paris");
        AdresseDto arr = new AdresseDto(null, 10, "avenue foch", "75116", "Paris");
        AnnonceCovoiturageDto expected = AnnonceCovoiturageDto.of(idAnnonce, depart, 30, 10, dep, arr, null);

        when(annonceMapper.versDto(annonceExistante)).thenReturn(expected);

        // WHEN
        AnnonceCovoiturageDto out = service.obtenirAnnonceParId(idAnnonce);

        // THEN
        assertEquals(expected, out);
        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(annonceMapper).versDto(annonceExistante);
        verifyNoMoreInteractions(annonceCovoiturageRepository, annonceMapper);
    }

    @Test
    @DisplayName("obtenirAnnonceParId → KO : annonce introuvable")
    void obtenirAnnonceParId_not_found() {
        Long id = 999L;
        when(annonceCovoiturageRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.obtenirAnnonceParId(id));

        assertTrue(ex.getMessage().toLowerCase().contains("introuvable"));

        verify(annonceCovoiturageRepository).findById(id);
        verifyNoInteractions(annonceMapper);
        verifyNoMoreInteractions(annonceCovoiturageRepository);
    }

    @Test
    @DisplayName("obtenirNombrePlacesTotales → OK : véhicule de service présent")
    void obtenirNombrePlacesTotales_ok_vehicule_service() {
        // annonce avec véhicule de service
        Utilisateur resp = new Utilisateur("Dupont", "Jean", "jean@acme.com", RoleEnum.ROLE_USER);
        resp.setId(42L);

        VehiculeEntreprise vs = new VehiculeEntreprise();
        vs.setId(7L);
        vs.setNbPlaces(4);

        AnnonceCovoiturage annonce = new AnnonceCovoiturage();
        annonce.setId(idAnnonce);
        annonce.setResponsable(resp);
        annonce.setVehiculeService(vs);

        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonce));

        Integer out = service.obtenirNombrePlacesTotales(idAnnonce);

        assertEquals(4, out);
        verify(annonceCovoiturageRepository).findById(idAnnonce);
        // pas d’appel aux véhicules persos dans ce chemin
        verifyNoInteractions(vehiculePersonnelRepository);
    }

    @Test
    @DisplayName("obtenirNombrePlacesTotales → OK : pas de véhicule de service, on prend le 1er véhicule perso")
    void obtenirNombrePlacesTotales_ok_vehicule_perso() {
        Utilisateur resp = new Utilisateur("Martin", "Alice", "alice@acme.com", RoleEnum.ROLE_USER);
        resp.setId(77L);

        AnnonceCovoiturage annonce = new AnnonceCovoiturage();
        annonce.setId(idAnnonce);
        annonce.setResponsable(resp);
        annonce.setVehiculeService(null); // important

        VehiculePersonnel vp1 = new VehiculePersonnel();
        vp1.setId(1L);
        vp1.setNbPlaces(5);

        // même si d’autres existent, la méthode prend le premier
        VehiculePersonnel vp2 = new VehiculePersonnel();
        vp2.setId(2L);
        vp2.setNbPlaces(3);

        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonce));
        when(vehiculePersonnelRepository.findByUtilisateur(resp)).thenReturn(List.of(vp1, vp2));

        Integer out = service.obtenirNombrePlacesTotales(idAnnonce);

        assertEquals(5, out);
        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(vehiculePersonnelRepository).findByUtilisateur(resp);
        verifyNoMoreInteractions(annonceCovoiturageRepository, vehiculePersonnelRepository);
    }

    @Test
    @DisplayName("obtenirNombrePlacesTotales → KO : annonce introuvable")
    void obtenirNombrePlacesTotales_not_found() {
        Long idAnnonce = 999L;
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.obtenirNombrePlacesTotales(idAnnonce));
        assertTrue(ex.getMessage().toLowerCase().contains("introuvable"));

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verifyNoInteractions(vehiculePersonnelRepository);
    }

    @Test
    @DisplayName("obtenirNombrePlacesTotales → KO : pas de véhicule de service et aucun véhicule perso")
    void obtenirNombrePlacesTotales_aucun_vehicule_perso() {
        Utilisateur resp = new Utilisateur("Durand", "Bob", "bob@acme.com", RoleEnum.ROLE_USER);
        resp.setId(88L);

        AnnonceCovoiturage annonce = new AnnonceCovoiturage();
        annonce.setId(idAnnonce);
        annonce.setResponsable(resp);
        annonce.setVehiculeService(null);

        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonce));
        when(vehiculePersonnelRepository.findByUtilisateur(resp)).thenReturn(List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.obtenirNombrePlacesTotales(idAnnonce));
        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("aucun véhicule") || msg.contains("incorrectement"));

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(vehiculePersonnelRepository).findByUtilisateur(resp);
        verifyNoMoreInteractions(annonceCovoiturageRepository, vehiculePersonnelRepository);
    }

    @Test
    @DisplayName("obtenirNombrePlacesOccupees → OK : renvoie le nombre de passagers")
    void obtenirNombrePlacesOccupees_ok() {
        AnnonceCovoiturage annonce = new AnnonceCovoiturage();
        annonce.setId(idAnnonce);

        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonce));
        when(covoituragePassagersRepository.countPassagersParCovoiturage(annonce)).thenReturn(3L);

        Integer out = service.obtenirNombrePlacesOccupees(idAnnonce);

        assertEquals(3, out);
        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(covoituragePassagersRepository).countPassagersParCovoiturage(annonce);
        verifyNoMoreInteractions(annonceCovoiturageRepository, covoituragePassagersRepository);
    }

    @Test
    @DisplayName("obtenirNombrePlacesOccupees → KO : annonce introuvable")
    void obtenirNombrePlacesOccupees_not_found() {
        Long idAnnonce = 999L;
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.obtenirNombrePlacesOccupees(idAnnonce));
        assertTrue(ex.getMessage().toLowerCase().contains("introuvable"));

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verifyNoInteractions(covoituragePassagersRepository);
    }

    @Test
    @DisplayName("reserverPlace → OK : utilisateur admissible, places dispos → enregistrement")
    void reserverPlace_ok() {
        when(utilisateurService.obtenirUtilisateurParId(anyLong())).thenReturn(user);
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonceExistante));
        when(covoituragePassagersRepository.findByUtilisateurAndAnnonceCovoiturage(user, annonceExistante))
                .thenReturn(Optional.empty());

        // on force 3 places totales et 1 occupée
        doReturn(3).when(service).obtenirNombrePlacesTotales(idAnnonce);
        doReturn(1).when(service).obtenirNombrePlacesOccupees(idAnnonce);

        service.reserverPlace(idAnnonce, idUser);

        verify(utilisateurService).obtenirUtilisateurParId(idUser);
        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(utilisateurService).obtenirUtilisateurParId(idUser);
        verify(covoituragePassagersRepository).findByUtilisateurAndAnnonceCovoiturage(user, annonceExistante);
        verify(service).obtenirNombrePlacesTotales(idAnnonce);
        verify(service).obtenirNombrePlacesOccupees(idAnnonce);
        verify(covoituragePassagersRepository).save(any(CovoituragePassagers.class));
    }

    @Test
    @DisplayName("reserverPlace → KO : annonce introuvable")
    void reserverPlace_ko_annonce_introuvable() {
        when(annonceCovoiturageRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.reserverPlace(999L, idUser));
        assertTrue(ex.getMessage().toLowerCase().contains("introuvable"));

        verify(annonceCovoiturageRepository).findById(999L);
        verifyNoInteractions(utilisateurService, covoituragePassagersRepository);
    }

    @Test
    @DisplayName("reserverPlace → KO : utilisateur déjà inscrit")
    void reserverPlace_ko_deja_inscrit() {
        when(utilisateurService.obtenirUtilisateurParId(anyLong())).thenReturn(user);
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonceExistante));
        when(covoituragePassagersRepository.findByUtilisateurAndAnnonceCovoiturage(user, annonceExistante))
                .thenReturn(Optional.of(new CovoituragePassagers()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.reserverPlace(idAnnonce, idUser));
        assertTrue(ex.getMessage().toLowerCase().contains("inscrit"));

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(utilisateurService).obtenirUtilisateurParId(idUser);
        verify(covoituragePassagersRepository).findByUtilisateurAndAnnonceCovoiturage(user, annonceExistante);
        // pas de calcul de places ni de save
        verify(service, never()).obtenirNombrePlacesTotales(anyLong());
        verify(service, never()).obtenirNombrePlacesOccupees(anyLong());
        verify(covoituragePassagersRepository, never()).save(any());
    }

    @Test
    @DisplayName("reserverPlace → KO : utilisateur est le responsable")
    void reserverPlace_ko_est_responsable() {
        when(utilisateurService.obtenirUtilisateurParId(idResponsable)).thenReturn(responsable);
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonceExistante));

        when(covoituragePassagersRepository.findByUtilisateurAndAnnonceCovoiturage(responsable, annonceExistante))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.reserverPlace(idAnnonce, idResponsable));
        assertTrue(ex.getMessage().toLowerCase().contains("propre covoiturage")
                || ex.getMessage().toLowerCase().contains("ne pouvez pas réserver"));

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(utilisateurService).obtenirUtilisateurParId(idResponsable);
        verify(covoituragePassagersRepository).findByUtilisateurAndAnnonceCovoiturage(responsable, annonceExistante);
        verify(service, never()).obtenirNombrePlacesTotales(anyLong());
        verify(service, never()).obtenirNombrePlacesOccupees(anyLong());
        verify(covoituragePassagersRepository, never()).save(any());
    }

    @Test
    @DisplayName("reserverPlace → KO : aucune place disponible (occupées ≥ totales)")
    void reserverPlace_ko_aucune_place() {
        when(utilisateurService.obtenirUtilisateurParId(anyLong())).thenReturn(user);
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonceExistante));
        when(covoituragePassagersRepository.findByUtilisateurAndAnnonceCovoiturage(user, annonceExistante))
                .thenReturn(Optional.empty());

        // force la saturation : 2 totales, 2 occupées
        doReturn(2).when(service).obtenirNombrePlacesTotales(idAnnonce);
        doReturn(2).when(service).obtenirNombrePlacesOccupees(idAnnonce);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.reserverPlace(idAnnonce, idUser));
        assertTrue(ex.getMessage().toLowerCase().contains("aucune place")
                || ex.getMessage().toLowerCase().contains("disponible"));

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(utilisateurService).obtenirUtilisateurParId(idUser);
        verify(covoituragePassagersRepository).findByUtilisateurAndAnnonceCovoiturage(user, annonceExistante);
        verify(service).obtenirNombrePlacesTotales(idAnnonce);
        verify(service).obtenirNombrePlacesOccupees(idAnnonce);
        verify(covoituragePassagersRepository, never()).save(any());
    }

    @Test
    @DisplayName("annulerReservation → OK : la réservation existe → suppression")
    void annulerReservation_ok() {
        // GIVEN
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonceExistante));
        when(utilisateurService.obtenirUtilisateurParId(idUser)).thenReturn(user);

        CovoituragePassagers resa = new CovoituragePassagers(user, annonceExistante);
        when(covoituragePassagersRepository.findByUtilisateurAndAnnonceCovoiturage(user, annonceExistante))
                .thenReturn(Optional.of(resa));

        // WHEN
        service.annulerReservation(idAnnonce, idUser);

        // THEN
        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(utilisateurService).obtenirUtilisateurParId(idUser);
        verify(covoituragePassagersRepository)
                .findByUtilisateurAndAnnonceCovoiturage(user, annonceExistante);
        verify(covoituragePassagersRepository).delete(resa);
        verifyNoMoreInteractions(annonceCovoiturageRepository, utilisateurService, covoituragePassagersRepository);
    }

    @Test
    @DisplayName("annulerReservation → KO : annonce introuvable")
    void annulerReservation_ko_annonce_introuvable() {
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.annulerReservation(idAnnonce, idUser));
        assertTrue(ex.getMessage().toLowerCase().contains("introuvable"), ex.getMessage());

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verifyNoInteractions(utilisateurService, covoituragePassagersRepository);
    }

    @Test
    @DisplayName("annulerReservation → KO : aucune réservation pour cet utilisateur")
    void annulerReservation_ko_aucune_reservation() {
        when(annonceCovoiturageRepository.findById(idAnnonce)).thenReturn(Optional.of(annonceExistante));
        when(utilisateurService.obtenirUtilisateurParId(idUser)).thenReturn(user);
        when(covoituragePassagersRepository.findByUtilisateurAndAnnonceCovoiturage(user, annonceExistante))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.annulerReservation(idAnnonce, idUser));
        assertTrue(ex.getMessage().toLowerCase().contains("aucune réservation")
                || ex.getMessage().toLowerCase().contains("aucune reservation"), ex.getMessage());

        verify(annonceCovoiturageRepository).findById(idAnnonce);
        verify(utilisateurService).obtenirUtilisateurParId(idUser);
        verify(covoituragePassagersRepository)
                .findByUtilisateurAndAnnonceCovoiturage(user, annonceExistante);
        verify(covoituragePassagersRepository, never()).delete(any());
    }

    @Test
    @DisplayName("obtenirToutesLesAnnonces → OK : mappe chaque annonce et annote avec places totales/occupées (ordre conservé)")
    void obtenirToutesLesAnnonces_ok() {
        // GIVEN — 2 entités
        AnnonceCovoiturage a1 = new AnnonceCovoiturage(); a1.setId(10L);
        AnnonceCovoiturage a2 = new AnnonceCovoiturage(); a2.setId(20L);
        when(annonceCovoiturageRepository.findAll()).thenReturn(List.of(a1, a2));

        // DTOs correspondant (peu importe les valeurs internes pour ce test)
        var dep = new AdresseDto(null, 1, "rue A", "34000", "Montpellier");
        var arr = new AdresseDto(null, 2, "rue B", "44000", "Nantes");
        var dto1 = AnnonceCovoiturageDto.of(10L, LocalDateTime.now().plusDays(1), 30, 10, dep, arr, null);
        var dto2 = AnnonceCovoiturageDto.of(20L, LocalDateTime.now().plusDays(2), 45, 12, dep, arr, 7L);
        when(annonceMapper.versDto(a1)).thenReturn(dto1);
        when(annonceMapper.versDto(a2)).thenReturn(dto2);

        // Helpers internes → on les stub via le Spy
        doReturn(4).when(service).obtenirNombrePlacesTotales(10L);
        doReturn(2).when(service).obtenirNombrePlacesOccupees(10L);
        doReturn(5).when(service).obtenirNombrePlacesTotales(20L);
        doReturn(3).when(service).obtenirNombrePlacesOccupees(20L);

        // WHEN
        List<AnnonceCovoiturageAvecPlacesDto> out = service.obtenirToutesLesAnnonces();

        // THEN — on compare à la liste attendue
        var expected = List.of(
                AnnonceCovoiturageAvecPlacesDto.of(dto1, 4, 2),
                AnnonceCovoiturageAvecPlacesDto.of(dto2, 5, 3)
        );
        assertEquals(expected, out);

        // Interactions
        verify(annonceCovoiturageRepository).findAll();
        verify(annonceMapper).versDto(a1);
        verify(annonceMapper).versDto(a2);
        verify(service).obtenirNombrePlacesTotales(10L);
        verify(service).obtenirNombrePlacesOccupees(10L);
        verify(service).obtenirNombrePlacesTotales(20L);
        verify(service).obtenirNombrePlacesOccupees(20L);
        verifyNoMoreInteractions(annonceCovoiturageRepository, annonceMapper);
    }

    @Test
    @DisplayName("obtenirToutesLesAnnonces → OK : liste vide → retourne vide, pas d’appels aux helpers")
    void obtenirToutesLesAnnonces_vide() {
        // GIVEN
        when(annonceCovoiturageRepository.findAll()).thenReturn(List.of());

        // WHEN
        List<AnnonceCovoiturageAvecPlacesDto> out = service.obtenirToutesLesAnnonces();

        // THEN
        assertNotNull(out);
        assertTrue(out.isEmpty());

        verify(annonceCovoiturageRepository).findAll();
        // aucun mapping ni helpers appelés
        verifyNoInteractions(annonceMapper);
        verify(service, never()).obtenirNombrePlacesTotales(anyLong());
        verify(service, never()).obtenirNombrePlacesOccupees(anyLong());
    }

    @Test
    @DisplayName("obtenirReservationsUtilisateur → OK : mappe chaque annonce et annote avec places totales/occupées")
    void obtenirReservationsUtilisateur_ok() {
        when(utilisateurService.obtenirUtilisateurParId(idUser)).thenReturn(user);

        // Deux annonces auxquelles l’utilisateur participe
        AnnonceCovoiturage a1 = new AnnonceCovoiturage(); a1.setId(10L);
        AnnonceCovoiturage a2 = new AnnonceCovoiturage(); a2.setId(20L);
        when(annonceCovoiturageRepository.findByUtilisateurParticipant(user))
                .thenReturn(List.of(a1, a2));

        // Mapping entité -> DTO
        var dep = new AdresseDto(null, 1, "rue A", "34000", "Montpellier");
        var arr = new AdresseDto(null, 2, "rue B", "44000", "Nantes");
        var dto1 = AnnonceCovoiturageDto.of(10L, LocalDateTime.now().plusDays(1), 30, 10, dep, arr, null);
        var dto2 = AnnonceCovoiturageDto.of(20L, LocalDateTime.now().plusDays(2), 45, 12, dep, arr, 7L);
        when(annonceMapper.versDto(a1)).thenReturn(dto1);
        when(annonceMapper.versDto(a2)).thenReturn(dto2);

        // Helpers internes (stub via Spy)
        doReturn(4).when(service).obtenirNombrePlacesTotales(10L);
        doReturn(2).when(service).obtenirNombrePlacesOccupees(10L);
        doReturn(5).when(service).obtenirNombrePlacesTotales(20L);
        doReturn(3).when(service).obtenirNombrePlacesOccupees(20L);

        // WHEN
        List<AnnonceCovoiturageAvecPlacesDto> out = service.obtenirReservationsUtilisateur(idUser);

        // THEN
        var expected = List.of(
                AnnonceCovoiturageAvecPlacesDto.of(dto1, 4, 2),
                AnnonceCovoiturageAvecPlacesDto.of(dto2, 5, 3)
        );
        assertEquals(expected, out);

        verify(utilisateurService).obtenirUtilisateurParId(idUser);
        verify(annonceCovoiturageRepository).findByUtilisateurParticipant(user);
        verify(annonceMapper).versDto(a1);
        verify(annonceMapper).versDto(a2);
        verify(service).obtenirNombrePlacesTotales(10L);
        verify(service).obtenirNombrePlacesOccupees(10L);
        verify(service).obtenirNombrePlacesTotales(20L);
        verify(service).obtenirNombrePlacesOccupees(20L);
        verifyNoMoreInteractions(annonceCovoiturageRepository, annonceMapper, utilisateurService);
    }

    @Test
    @DisplayName("obtenirReservationsUtilisateur → KO : aucune réservation pour cet utilisateur")
    void obtenirReservationsUtilisateur_aucune_reservation() {
        when(utilisateurService.obtenirUtilisateurParId(idUser)).thenReturn(user);

        when(annonceCovoiturageRepository.findByUtilisateurParticipant(user))
                .thenReturn(List.of()); // vide

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.obtenirReservationsUtilisateur(idUser));
        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("aucune réservation") || msg.contains("aucune reservation"));

        verify(utilisateurService).obtenirUtilisateurParId(idUser);
        verify(annonceCovoiturageRepository).findByUtilisateurParticipant(user);
        // Pas d’appels aux helpers ni au mapper
        verifyNoInteractions(annonceMapper);
        verify(service, never()).obtenirNombrePlacesTotales(anyLong());
        verify(service, never()).obtenirNombrePlacesOccupees(anyLong());
    }
}
