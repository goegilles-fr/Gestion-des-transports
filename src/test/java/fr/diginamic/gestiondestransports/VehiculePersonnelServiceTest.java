package fr.diginamic.gestiondestransports;


import fr.diginamic.gestiondestransports.dto.VehiculeDTO;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.entites.VehiculePersonnel;
import fr.diginamic.gestiondestransports.enums.Categorie;
import fr.diginamic.gestiondestransports.enums.Motorisation;
import fr.diginamic.gestiondestransports.enums.RoleEnum;
import fr.diginamic.gestiondestransports.mapper.VehiculeMapper;
import fr.diginamic.gestiondestransports.repositories.UtilisateurRepository;
import fr.diginamic.gestiondestransports.repositories.VehiculePersonnelRepository;
import fr.diginamic.gestiondestransports.services.impl.VehiculePersonnelServiceImpl;
import fr.diginamic.gestiondestransports.shared.BadRequestException;
import fr.diginamic.gestiondestransports.shared.ConflictException;
import fr.diginamic.gestiondestransports.shared.NotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehiculePersonnelServiceTest {

    @Mock
    private VehiculePersonnelRepository vehiculePersonnelRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private VehiculeMapper vehiculeMapper;

    @InjectMocks
    private VehiculePersonnelServiceImpl vehiculePersonnelService;

    private Utilisateur utilisateurTest;
    private VehiculePersonnel vehiculePersonnelTest;
    private VehiculeDTO vehiculeDtoTest;

    @BeforeAll
    static void beforeAll() {
        System.out.println("Début de la campagne de tests VehiculePersonnelServiceTest");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Fin de la campagne de tests VehiculePersonnelServiceTest");
    }

    @BeforeEach
    void BeforeEach() {
        // Préparer un utilisateur de test
        utilisateurTest = new Utilisateur("Dupont", "Jean", "jean.dupont@example.com", RoleEnum.ROLE_USER);
        utilisateurTest.setId(1L);
        utilisateurTest.setEstVerifie(true);
        utilisateurTest.setEstBanni(false);

        // Préparer un véhicule personnel de test
        vehiculePersonnelTest = new VehiculePersonnel(
                1L,
                "AB-123-CD",
                5,
                "308",
                120,
                "http://example.com/photo.jpg",
                "Peugeot",
                Motorisation.HYBRIDE,
                Categorie.COMPACTE,
                utilisateurTest
        );

        // Préparer un DTO de test
        vehiculeDtoTest = new VehiculeDTO(
                1L,
                "AB-123-CD",
                "Peugeot",
                "308",
                5,
                Motorisation.HYBRIDE,
                120,
                "http://example.com/photo.jpg",
                Categorie.COMPACTE,
                null,
                1L
        );
    }



    @Test
    @DisplayName("Devrait retourner la liste des véhicules personnels appartenant à l'utilisateur spécifié")
    void findByUtilisateurId_ShouldReturnListOfVehiculesForGivenUtilisateur() {
        // Arrange
        VehiculePersonnel vehicule2 = new VehiculePersonnel(
                2L,
                "EF-456-GH",
                4,
                "Clio",
                110,
                "http://example.com/clio.jpg",
                "Renault",
                Motorisation.THERMIQUE,
                Categorie.MINI_CITADINE,
                utilisateurTest
        );

        List<VehiculePersonnel> vehicules = Arrays.asList(vehiculePersonnelTest, vehicule2);

        VehiculeDTO dto2 = new VehiculeDTO(
                2L,
                "EF-456-GH",
                "Renault",
                "Clio",
                4,
                Motorisation.THERMIQUE,
                110,
                "http://example.com/clio.jpg",
                Categorie.MINI_CITADINE,
                null,
                1L
        );

        List<VehiculeDTO> vehiculeDtos = Arrays.asList(vehiculeDtoTest, dto2);

        when(vehiculePersonnelRepository.findByUtilisateurId(1L)).thenReturn(vehicules);
        when(vehiculeMapper.toDtoPersonnelList(vehicules)).thenReturn(vehiculeDtos);

        // Act
        List<VehiculeDTO> resultat = vehiculePersonnelService.findByUtilisateurId(1L);

        // Assert
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        assertEquals("AB-123-CD", resultat.get(0).immatriculation());
        assertEquals("EF-456-GH", resultat.get(1).immatriculation());
        assertEquals(1L, resultat.get(0).utilisateurId());
        assertEquals(1L, resultat.get(1).utilisateurId());

        verify(vehiculePersonnelRepository, times(1)).findByUtilisateurId(1L);
        verify(vehiculeMapper, times(1)).toDtoPersonnelList(vehicules);
    }

    @Test
    @DisplayName("Devrait retourner une liste vide lorsque l'utilisateur n'a aucun véhicule personnel")
    void findByUtilisateurId_ShouldReturnEmptyListWhenUtilisateurHasNoVehicules() {
        // Arrange
        when(vehiculePersonnelRepository.findByUtilisateurId(1L)).thenReturn(new ArrayList<>());
        when(vehiculeMapper.toDtoPersonnelList(any())).thenReturn(new ArrayList<>());

        // Act
        List<VehiculeDTO> resultat = vehiculePersonnelService.findByUtilisateurId(1L);

        // Assert
        assertNotNull(resultat);
        assertTrue(resultat.isEmpty());
        assertEquals(0, resultat.size());

        verify(vehiculePersonnelRepository, times(1)).findByUtilisateurId(1L);
        verify(vehiculeMapper, times(1)).toDtoPersonnelList(any());
    }

    // ========== Tests pour findAll ==========

    @Test
    @DisplayName("Devrait retourner la liste complète de tous les véhicules personnels présents dans la base de données")
    void findAll_ShouldReturnCompleteListOfAllVehiculesPersonnels() {
        // Arrange
        Utilisateur utilisateur2 = new Utilisateur("Martin", "Sophie", "sophie.martin@example.com", RoleEnum.ROLE_USER);
        utilisateur2.setId(2L);

        VehiculePersonnel vehicule2 = new VehiculePersonnel(
                2L,
                "IJ-789-KL",
                4,
                "Corsa",
                100,
                "http://example.com/corsa.jpg",
                "Opel",
                Motorisation.ELECTRIQUE,
                Categorie.MINI_CITADINE,
                utilisateur2
        );

        List<VehiculePersonnel> tousLesVehicules = Arrays.asList(vehiculePersonnelTest, vehicule2);

        VehiculeDTO dto2 = new VehiculeDTO(
                2L,
                "IJ-789-KL",
                "Opel",
                "Corsa",
                4,
                Motorisation.ELECTRIQUE,
                100,
                "http://example.com/corsa.jpg",
                Categorie.MINI_CITADINE,
                null,
                2L
        );

        List<VehiculeDTO> tousLesDtos = Arrays.asList(vehiculeDtoTest, dto2);

        when(vehiculePersonnelRepository.findAll()).thenReturn(tousLesVehicules);
        when(vehiculeMapper.toDtoPersonnelList(tousLesVehicules)).thenReturn(tousLesDtos);

        // Act
        List<VehiculeDTO> resultat = vehiculePersonnelService.findAll();

        // Assert
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        assertEquals("Peugeot", resultat.get(0).marque());
        assertEquals("Opel", resultat.get(1).marque());
        assertEquals(1L, resultat.get(0).utilisateurId());
        assertEquals(2L, resultat.get(1).utilisateurId());

        verify(vehiculePersonnelRepository, times(1)).findAll();
        verify(vehiculeMapper, times(1)).toDtoPersonnelList(tousLesVehicules);
    }

    @Test
    @DisplayName("Devrait retourner une liste vide lorsqu'aucun véhicule personnel n'existe dans la base de données")
    void findAll_ShouldReturnEmptyListWhenNoVehiculesExist() {
        // Arrange
        when(vehiculePersonnelRepository.findAll()).thenReturn(new ArrayList<>());
        when(vehiculeMapper.toDtoPersonnelList(any())).thenReturn(new ArrayList<>());

        // Act
        List<VehiculeDTO> resultat = vehiculePersonnelService.findAll();

        // Assert
        assertNotNull(resultat);
        assertTrue(resultat.isEmpty());
        assertEquals(0, resultat.size());

        verify(vehiculePersonnelRepository, times(1)).findAll();
        verify(vehiculeMapper, times(1)).toDtoPersonnelList(any());
    }

    // ========== Tests pour delete ==========

    @Test
    @DisplayName("Devrait supprimer le véhicule personnel avec succès lorsque l'ID existe dans la base de données")
    void delete_ShouldDeleteVehiculePersonnelSuccessfullyWhenIdExists() {
        // Arrange
        when(vehiculePersonnelRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vehiculePersonnelRepository).deleteById(1L);

        // Act
        vehiculePersonnelService.delete(1L);

        // Assert
        verify(vehiculePersonnelRepository, times(1)).existsById(1L);
        verify(vehiculePersonnelRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Devrait lever une NotFoundException lors de la tentative de suppression d'un véhicule personnel inexistant")
    void delete_ShouldThrowNotFoundExceptionWhenVehiculeDoesNotExist() {
        // Arrange
        when(vehiculePersonnelRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            vehiculePersonnelService.delete(999L);
        });

        assertTrue(exception.getMessage().contains("Véhicule personnel introuvable"));
        assertTrue(exception.getMessage().contains("999"));

        verify(vehiculePersonnelRepository, times(1)).existsById(999L);
        verify(vehiculePersonnelRepository, never()).deleteById(anyLong());
    }
    // ========== Tests pour deleteByUtilisateurId ==========

    @Test
    @DisplayName("Devrait supprimer le véhicule personnel associé à l'utilisateur spécifié lorsque celui-ci existe")
    void deleteByUtilisateurId_ShouldDeleteVehiculePersonnelWhenUtilisateurIdExists() {
        // Arrange
        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculePersonnelTest));
        doNothing().when(vehiculePersonnelRepository).deleteById(1L);

        // Act
        vehiculePersonnelService.deleteByUtilisateurId(1L);

        // Assert
        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
        verify(vehiculePersonnelRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Devrait lever une NotFoundException lorsque l'utilisateur spécifié n'a aucun véhicule personnel")
    void deleteByUtilisateurId_ShouldThrowNotFoundExceptionWhenUtilisateurHasNoVehicule() {
        // Arrange
        when(vehiculePersonnelRepository.findFirstByUtilisateurId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            vehiculePersonnelService.deleteByUtilisateurId(999L);
        });

        assertTrue(exception.getMessage().contains("Véhicule personnel introuvable"));
        assertTrue(exception.getMessage().contains("999"));

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(999L);
        verify(vehiculePersonnelRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Devrait supprimer le premier véhicule trouvé lorsque l'utilisateur possède plusieurs véhicules personnels")
    void deleteByUtilisateurId_ShouldDeleteFirstVehiculeWhenUtilisateurHasMultipleVehicules() {
        // Arrange
        VehiculePersonnel premierVehicule = new VehiculePersonnel(
                5L,
                "AA-111-BB",
                5,
                "Golf",
                130,
                "http://example.com/golf.jpg",
                "Volkswagen",
                Motorisation.THERMIQUE,
                Categorie.COMPACTE,
                utilisateurTest
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(premierVehicule));
        doNothing().when(vehiculePersonnelRepository).deleteById(5L);

        // Act
        vehiculePersonnelService.deleteByUtilisateurId(1L);

        // Assert
        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
        verify(vehiculePersonnelRepository, times(1)).deleteById(5L);
    }

    // ========== Tests pour findById ==========

    @Test
    @DisplayName("Devrait retourner le véhicule personnel correspondant lorsqu'un véhicule avec l'ID spécifié existe")
    void findById_ShouldReturnVehiculePersonnelWhenIdExists() {
        // Arrange
        when(vehiculePersonnelRepository.findById(1L)).thenReturn(Optional.of(vehiculePersonnelTest));
        when(vehiculeMapper.toDto(vehiculePersonnelTest)).thenReturn(vehiculeDtoTest);

        // Act
        VehiculeDTO resultat = vehiculePersonnelService.findById(1L);

        // Assert
        assertNotNull(resultat);
        assertEquals(1L, resultat.id());
        assertEquals("AB-123-CD", resultat.immatriculation());
        assertEquals("Peugeot", resultat.marque());
        assertEquals("308", resultat.modele());
        assertEquals(5, resultat.nbPlaces());
        assertEquals(Motorisation.HYBRIDE, resultat.motorisation());
        assertEquals(120, resultat.co2ParKm());
        assertEquals(Categorie.COMPACTE, resultat.categorie());
        assertEquals(1L, resultat.utilisateurId());

        verify(vehiculePersonnelRepository, times(1)).findById(1L);
        verify(vehiculeMapper, times(1)).toDto(vehiculePersonnelTest);
    }

    @Test
    @DisplayName("Devrait lever une NotFoundException lorsqu'aucun véhicule ne correspond à l'ID spécifié")
    void findById_ShouldThrowNotFoundExceptionWhenIdDoesNotExist() {
        // Arrange
        when(vehiculePersonnelRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            vehiculePersonnelService.findById(999L);
        });

        assertTrue(exception.getMessage().contains("Véhicule personnel introuvable"));
        assertTrue(exception.getMessage().contains("999"));

        verify(vehiculePersonnelRepository, times(1)).findById(999L);
        verify(vehiculeMapper, never()).toDto(any(VehiculePersonnel.class));
    }

    @Test
    @DisplayName("Devrait lever une NotFoundException lorsque l'ID fourni est null")
    void findById_ShouldThrowNotFoundExceptionWhenIdIsNull() {
        // Arrange
        when(vehiculePersonnelRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            vehiculePersonnelService.findById(null);
        });

        assertTrue(exception.getMessage().contains("Véhicule personnel introuvable"));

        verify(vehiculePersonnelRepository, times(1)).findById(null);
        verify(vehiculeMapper, never()).toDto(any(VehiculePersonnel.class));
    }

// ========== Tests pour create ==========

    @Test
    @DisplayName("Devrait créer un nouveau véhicule personnel avec succès lorsque toutes les données sont valides et l'utilisateur n'a pas encore de véhicule")
    void create_ShouldCreateVehiculePersonnelSuccessfullyWhenAllDataIsValid() {
        // Arrange
        VehiculeDTO nouveauVehiculeDto = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                "Toyota",
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        VehiculePersonnel vehiculeACreer = new VehiculePersonnel(
                null,
                "XY-999-ZZ",
                5,
                "Yaris",
                95,
                "http://example.com/yaris.jpg",
                "Toyota",
                Motorisation.HYBRIDE,
                Categorie.COMPACTE,
                null
        );

        VehiculePersonnel vehiculeSauvegarde = new VehiculePersonnel(
                10L,
                "XY-999-ZZ",
                5,
                "Yaris",
                95,
                "http://example.com/yaris.jpg",
                "Toyota",
                Motorisation.HYBRIDE,
                Categorie.COMPACTE,
                utilisateurTest
        );

        VehiculeDTO vehiculeDtoSauvegarde = new VehiculeDTO(
                10L,
                "XY-999-ZZ",
                "Toyota",
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                1L
        );

        when(vehiculePersonnelRepository.existsByUtilisateurId(1L)).thenReturn(false);
        when(vehiculeMapper.toPersonnelEntity(nouveauVehiculeDto)).thenReturn(vehiculeACreer);
        when(utilisateurRepository.getReferenceById(1L)).thenReturn(utilisateurTest);
        when(vehiculePersonnelRepository.saveAndFlush(any(VehiculePersonnel.class))).thenReturn(vehiculeSauvegarde);
        when(vehiculeMapper.toDto(vehiculeSauvegarde)).thenReturn(vehiculeDtoSauvegarde);

        // Act
        VehiculeDTO resultat = vehiculePersonnelService.create(1L, nouveauVehiculeDto);

        // Assert
        assertNotNull(resultat);
        assertEquals(10L, resultat.id());
        assertEquals("XY-999-ZZ", resultat.immatriculation());
        assertEquals("Toyota", resultat.marque());
        assertEquals("Yaris", resultat.modele());
        assertEquals(5, resultat.nbPlaces());
        assertEquals(1L, resultat.utilisateurId());

        verify(vehiculePersonnelRepository, times(1)).existsByUtilisateurId(1L);
        verify(vehiculeMapper, times(1)).toPersonnelEntity(nouveauVehiculeDto);
        verify(utilisateurRepository, times(1)).getReferenceById(1L);
        verify(vehiculePersonnelRepository, times(1)).saveAndFlush(any(VehiculePersonnel.class));
        verify(vehiculeMapper, times(1)).toDto(vehiculeSauvegarde);
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque la marque est null lors de la création")
    void create_ShouldThrowBadRequestExceptionWhenMarqueIsNull() {
        // Arrange
        VehiculeDTO vehiculeAvecMarqueNull = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                null,
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.create(1L, vehiculeAvecMarqueNull);
        });

        assertTrue(exception.getMessage().contains("La marque est obligatoire"));

        verify(vehiculePersonnelRepository, never()).existsByUtilisateurId(anyLong());
        verify(vehiculePersonnelRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque la marque est vide lors de la création")
    void create_ShouldThrowBadRequestExceptionWhenMarqueIsBlank() {
        // Arrange
        VehiculeDTO vehiculeAvecMarqueVide = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                "   ",
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.create(1L, vehiculeAvecMarqueVide);
        });

        assertTrue(exception.getMessage().contains("La marque est obligatoire"));

        verify(vehiculePersonnelRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque le modèle est null lors de la création")
    void create_ShouldThrowBadRequestExceptionWhenModeleIsNull() {
        // Arrange
        VehiculeDTO vehiculeAvecModeleNull = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                "Toyota",
                null,
                5,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.create(1L, vehiculeAvecModeleNull);
        });

        assertTrue(exception.getMessage().contains("Le modele est obligatoire"));

        verify(vehiculePersonnelRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque le modèle est vide lors de la création")
    void create_ShouldThrowBadRequestExceptionWhenModeleIsBlank() {
        // Arrange
        VehiculeDTO vehiculeAvecModeleVide = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                "Toyota",
                "  ",
                5,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.create(1L, vehiculeAvecModeleVide);
        });

        assertTrue(exception.getMessage().contains("Le modele est obligatoire"));

        verify(vehiculePersonnelRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque l'immatriculation est null lors de la création")
    void create_ShouldThrowBadRequestExceptionWhenImmatriculationIsNull() {
        // Arrange
        VehiculeDTO vehiculeAvecImmatNull = new VehiculeDTO(
                null,
                null,
                "Toyota",
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.create(1L, vehiculeAvecImmatNull);
        });

        assertTrue(exception.getMessage().contains("L'immatriculation est obligatoire"));

        verify(vehiculePersonnelRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque l'immatriculation est vide lors de la création")
    void create_ShouldThrowBadRequestExceptionWhenImmatriculationIsBlank() {
        // Arrange
        VehiculeDTO vehiculeAvecImmatVide = new VehiculeDTO(
                null,
                "   ",
                "Toyota",
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.create(1L, vehiculeAvecImmatVide);
        });

        assertTrue(exception.getMessage().contains("L'immatriculation est obligatoire"));

        verify(vehiculePersonnelRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque le nombre de places est null lors de la création")
    void create_ShouldThrowBadRequestExceptionWhenNbPlacesIsNull() {
        // Arrange
        VehiculeDTO vehiculeAvecNbPlacesNull = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                "Toyota",
                "Yaris",
                null,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.create(1L, vehiculeAvecNbPlacesNull);
        });

        assertTrue(exception.getMessage().contains("nombrePlaces doit être >= 1"));

        verify(vehiculePersonnelRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque le nombre de places est inférieur à 1 lors de la création")
    void create_ShouldThrowBadRequestExceptionWhenNbPlacesIsLessThanOne() {
        // Arrange
        VehiculeDTO vehiculeAvecNbPlacesInvalide = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                "Toyota",
                "Yaris",
                0,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.create(1L, vehiculeAvecNbPlacesInvalide);
        });

        assertTrue(exception.getMessage().contains("nombrePlaces doit être >= 1"));

        verify(vehiculePersonnelRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque le CO2 par km est négatif lors de la création")
    void create_ShouldThrowBadRequestExceptionWhenCo2ParKmIsNegative() {
        // Arrange
        VehiculeDTO vehiculeAvecCo2Negatif = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                "Toyota",
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                -10,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.create(1L, vehiculeAvecCo2Negatif);
        });

        assertTrue(exception.getMessage().contains("co2ParKm doit être >= 0"));

        verify(vehiculePersonnelRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Devrait créer un véhicule avec succès lorsque le CO2 par km est null (valeur optionnelle)")
    void create_ShouldCreateVehiculeSuccessfullyWhenCo2ParKmIsNull() {
        // Arrange
        VehiculeDTO vehiculeAvecCo2Null = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                "Toyota",
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                null,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        VehiculePersonnel vehiculeACreer = new VehiculePersonnel(
                null,
                "XY-999-ZZ",
                5,
                "Yaris",
                null,
                "http://example.com/yaris.jpg",
                "Toyota",
                Motorisation.HYBRIDE,
                Categorie.COMPACTE,
                null
        );

        VehiculePersonnel vehiculeSauvegarde = new VehiculePersonnel(
                10L,
                "XY-999-ZZ",
                5,
                "Yaris",
                null,
                "http://example.com/yaris.jpg",
                "Toyota",
                Motorisation.HYBRIDE,
                Categorie.COMPACTE,
                utilisateurTest
        );

        VehiculeDTO vehiculeDtoSauvegarde = new VehiculeDTO(
                10L,
                "XY-999-ZZ",
                "Toyota",
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                null,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                1L
        );

        when(vehiculePersonnelRepository.existsByUtilisateurId(1L)).thenReturn(false);
        when(vehiculeMapper.toPersonnelEntity(vehiculeAvecCo2Null)).thenReturn(vehiculeACreer);
        when(utilisateurRepository.getReferenceById(1L)).thenReturn(utilisateurTest);
        when(vehiculePersonnelRepository.saveAndFlush(any(VehiculePersonnel.class))).thenReturn(vehiculeSauvegarde);
        when(vehiculeMapper.toDto(vehiculeSauvegarde)).thenReturn(vehiculeDtoSauvegarde);

        // Act
        VehiculeDTO resultat = vehiculePersonnelService.create(1L, vehiculeAvecCo2Null);

        // Assert
        assertNotNull(resultat);
        assertNull(resultat.co2ParKm());

        verify(vehiculePersonnelRepository, times(1)).saveAndFlush(any(VehiculePersonnel.class));
    }

    @Test
    @DisplayName("Devrait lever une ConflictException lorsque l'utilisateur possède déjà un véhicule personnel")
    void create_ShouldThrowConflictExceptionWhenUtilisateurAlreadyHasVehicule() {
        // Arrange
        VehiculeDTO nouveauVehiculeDto = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                "Toyota",
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        VehiculePersonnel vehiculeExistant = new VehiculePersonnel(
                5L,
                "AB-123-CD",
                5,
                "308",
                120,
                "http://example.com/photo.jpg",
                "Peugeot",
                Motorisation.HYBRIDE,
                Categorie.COMPACTE,
                utilisateurTest
        );

        when(vehiculePersonnelRepository.existsByUtilisateurId(1L)).thenReturn(true);
        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculeExistant));

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            vehiculePersonnelService.create(1L, nouveauVehiculeDto);
        });

        assertTrue(exception.getMessage().contains("L'utilisateur 1"));
        assertTrue(exception.getMessage().contains("possède déjà un véhicule personnel"));
        assertTrue(exception.getMessage().contains("id=5"));

        verify(vehiculePersonnelRepository, times(1)).existsByUtilisateurId(1L);
        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
        verify(vehiculePersonnelRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Devrait créer un véhicule avec succès lorsque le CO2 par km est zéro (valeur limite valide)")
    void create_ShouldCreateVehiculeSuccessfullyWhenCo2ParKmIsZero() {
        // Arrange
        VehiculeDTO vehiculeAvecCo2Zero = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                "Tesla",
                "Model 3",
                5,
                Motorisation.ELECTRIQUE,
                0,
                "http://example.com/tesla.jpg",
                Categorie.BERLINE_M,
                null,
                null
        );

        VehiculePersonnel vehiculeACreer = new VehiculePersonnel(
                null,
                "XY-999-ZZ",
                5,
                "Model 3",
                0,
                "http://example.com/tesla.jpg",
                "Tesla",
                Motorisation.ELECTRIQUE,
                Categorie.BERLINE_M,
                null
        );

        VehiculePersonnel vehiculeSauvegarde = new VehiculePersonnel(
                10L,
                "XY-999-ZZ",
                5,
                "Model 3",
                0,
                "http://example.com/tesla.jpg",
                "Tesla",
                Motorisation.ELECTRIQUE,
                Categorie.BERLINE_M,
                utilisateurTest
        );

        VehiculeDTO vehiculeDtoSauvegarde = new VehiculeDTO(
                10L,
                "XY-999-ZZ",
                "Tesla",
                "Model 3",
                5,
                Motorisation.ELECTRIQUE,
                0,
                "http://example.com/tesla.jpg",
                Categorie.BERLINE_M,
                null,
                1L
        );

        when(vehiculePersonnelRepository.existsByUtilisateurId(1L)).thenReturn(false);
        when(vehiculeMapper.toPersonnelEntity(vehiculeAvecCo2Zero)).thenReturn(vehiculeACreer);
        when(utilisateurRepository.getReferenceById(1L)).thenReturn(utilisateurTest);
        when(vehiculePersonnelRepository.saveAndFlush(any(VehiculePersonnel.class))).thenReturn(vehiculeSauvegarde);
        when(vehiculeMapper.toDto(vehiculeSauvegarde)).thenReturn(vehiculeDtoSauvegarde);

        // Act
        VehiculeDTO resultat = vehiculePersonnelService.create(1L, vehiculeAvecCo2Zero);

        // Assert
        assertNotNull(resultat);
        assertEquals(0, resultat.co2ParKm());

        verify(vehiculePersonnelRepository, times(1)).saveAndFlush(any(VehiculePersonnel.class));
    }

    @Test
    @DisplayName("Devrait lever une ResponseStatusException lorsqu'une violation de contrainte de validation JPA se produit lors de la sauvegarde")
    void create_ShouldThrowResponseStatusExceptionWhenConstraintViolationOccurs() {
        // Arrange
        VehiculeDTO nouveauVehiculeDto = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                "Toyota",
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        VehiculePersonnel vehiculeACreer = new VehiculePersonnel(
                null,
                "XY-999-ZZ",
                5,
                "Yaris",
                95,
                "http://example.com/yaris.jpg",
                "Toyota",
                Motorisation.HYBRIDE,
                Categorie.COMPACTE,
                null
        );

        when(vehiculePersonnelRepository.existsByUtilisateurId(1L)).thenReturn(false);
        when(vehiculeMapper.toPersonnelEntity(nouveauVehiculeDto)).thenReturn(vehiculeACreer);
        when(utilisateurRepository.getReferenceById(1L)).thenReturn(utilisateurTest);

        javax.validation.ConstraintViolationException constraintException =
                mock(javax.validation.ConstraintViolationException.class);
        when(constraintException.getMessage()).thenReturn("Validation failed");
        when(constraintException.getConstraintViolations()).thenReturn(java.util.Collections.emptySet());

        when(vehiculePersonnelRepository.saveAndFlush(any(VehiculePersonnel.class)))
                .thenThrow(constraintException);

        // Act & Assert
        org.springframework.web.server.ResponseStatusException exception =
                assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
                    vehiculePersonnelService.create(1L, nouveauVehiculeDto);
                });

        assertTrue(exception.getMessage().contains("Validation error"));
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(vehiculePersonnelRepository, times(1)).saveAndFlush(any(VehiculePersonnel.class));
    }

    @Test
    @DisplayName("Devrait lever une ResponseStatusException lorsqu'une violation d'intégrité de données se produit (exemple: immatriculation en double)")
    void create_ShouldThrowResponseStatusExceptionWhenDataIntegrityViolationOccurs() {
        // Arrange
        VehiculeDTO nouveauVehiculeDto = new VehiculeDTO(
                null,
                "AB-123-CD",
                "Toyota",
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        VehiculePersonnel vehiculeACreer = new VehiculePersonnel(
                null,
                "AB-123-CD",
                5,
                "Yaris",
                95,
                "http://example.com/yaris.jpg",
                "Toyota",
                Motorisation.HYBRIDE,
                Categorie.COMPACTE,
                null
        );

        when(vehiculePersonnelRepository.existsByUtilisateurId(1L)).thenReturn(false);
        when(vehiculeMapper.toPersonnelEntity(nouveauVehiculeDto)).thenReturn(vehiculeACreer);
        when(utilisateurRepository.getReferenceById(1L)).thenReturn(utilisateurTest);

        org.springframework.dao.DataIntegrityViolationException dataException =
                new org.springframework.dao.DataIntegrityViolationException("Duplicate entry 'AB-123-CD' for key 'immatriculation'");

        when(vehiculePersonnelRepository.saveAndFlush(any(VehiculePersonnel.class)))
                .thenThrow(dataException);

        // Act & Assert
        org.springframework.web.server.ResponseStatusException exception =
                assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
                    vehiculePersonnelService.create(1L, nouveauVehiculeDto);
                });

        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(vehiculePersonnelRepository, times(1)).saveAndFlush(any(VehiculePersonnel.class));
    }

    // ========== Tests pour update ==========

    @Test
    @DisplayName("Devrait mettre à jour toutes les propriétés du véhicule personnel avec succès lorsque toutes les données sont valides")
    void update_ShouldUpdateAllPropertiesSuccessfullyWhenAllDataIsValid() {
        // Arrange
        VehiculeDTO vehiculeModifieDto = new VehiculeDTO(
                null,
                "ZZ-999-YY",
                "Honda",
                "Civic",
                4,
                Motorisation.THERMIQUE,
                110,
                "http://example.com/civic.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        VehiculeDTO vehiculeDtoMisAJour = new VehiculeDTO(
                1L,
                "ZZ-999-YY",
                "Honda",
                "Civic",
                4,
                Motorisation.THERMIQUE,
                110,
                "http://example.com/civic.jpg",
                Categorie.COMPACTE,
                null,
                1L
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculePersonnelTest));
        when(vehiculeMapper.toDto(vehiculePersonnelTest)).thenReturn(vehiculeDtoMisAJour);

        // Act
        VehiculeDTO resultat = vehiculePersonnelService.update(1L, vehiculeModifieDto);

        // Assert
        assertNotNull(resultat);
        assertEquals("ZZ-999-YY", resultat.immatriculation());
        assertEquals("Honda", resultat.marque());
        assertEquals("Civic", resultat.modele());
        assertEquals(4, resultat.nbPlaces());
        assertEquals(Motorisation.THERMIQUE, resultat.motorisation());
        assertEquals(110, resultat.co2ParKm());
        assertEquals("http://example.com/civic.jpg", resultat.photo());
        assertEquals(Categorie.COMPACTE, resultat.categorie());

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
        verify(vehiculeMapper, times(1)).toDto(vehiculePersonnelTest);
    }

    @Test
    @DisplayName("Devrait lever une NotFoundException lorsque le véhicule personnel à mettre à jour n'existe pas pour l'utilisateur spécifié")
    void update_ShouldThrowNotFoundExceptionWhenVehiculeDoesNotExist() {
        // Arrange
        VehiculeDTO vehiculeModifieDto = new VehiculeDTO(
                null,
                "ZZ-999-YY",
                "Honda",
                "Civic",
                4,
                Motorisation.THERMIQUE,
                110,
                "http://example.com/civic.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            vehiculePersonnelService.update(999L, vehiculeModifieDto);
        });

        assertTrue(exception.getMessage().contains("Véhicule personnel introuvable"));
        assertTrue(exception.getMessage().contains("999"));

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(999L);
        verify(vehiculeMapper, never()).toDto(any(VehiculePersonnel.class));
    }

    @Test
    @DisplayName("Devrait mettre à jour uniquement les champs fournis et conserver les valeurs existantes pour les champs null")
    void update_ShouldUpdateOnlyProvidedFieldsAndKeepExistingValuesForNullFields() {
        // Arrange - DTO avec seulement quelques champs renseignés
        VehiculeDTO vehiculePartielDto = new VehiculeDTO(
                null,
                "NEW-IMMAT",
                null,  // marque null - ne doit pas être modifiée
                null,  // modele null - ne doit pas être modifié
                null,  // nbPlaces null - ne doit pas être modifié
                null,  // motorisation null - ne doit pas être modifiée
                null,  // co2ParKm null - ne doit pas être modifié
                "http://example.com/new-photo.jpg",
                null,  // categorie null - ne doit pas être modifiée
                null,
                null
        );

        VehiculeDTO vehiculeDtoResultat = new VehiculeDTO(
                1L,
                "NEW-IMMAT",
                "Peugeot",
                "308",
                5,
                Motorisation.HYBRIDE,
                120,
                "http://example.com/new-photo.jpg",
                Categorie.COMPACTE,
                null,
                1L
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculePersonnelTest));
        when(vehiculeMapper.toDto(vehiculePersonnelTest)).thenReturn(vehiculeDtoResultat);

        // Act
        VehiculeDTO resultat = vehiculePersonnelService.update(1L, vehiculePartielDto);

        // Assert
        assertNotNull(resultat);
        assertEquals("NEW-IMMAT", resultat.immatriculation());
        assertEquals("Peugeot", resultat.marque()); // Valeur originale conservée
        assertEquals("308", resultat.modele()); // Valeur originale conservée
        assertEquals(5, resultat.nbPlaces()); // Valeur originale conservée
        assertEquals("http://example.com/new-photo.jpg", resultat.photo());

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque la marque fournie est vide (non null mais blank)")
    void update_ShouldThrowBadRequestExceptionWhenMarqueIsBlank() {
        // Arrange
        VehiculeDTO vehiculeAvecMarqueVide = new VehiculeDTO(
                null,
                "ZZ-999-YY",
                "   ",  // marque vide
                "Civic",
                4,
                Motorisation.THERMIQUE,
                110,
                "http://example.com/civic.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculePersonnelTest));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.update(1L, vehiculeAvecMarqueVide);
        });

        assertTrue(exception.getMessage().contains("La marque est obligatoire"));

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
        verify(vehiculeMapper, never()).toDto(any(VehiculePersonnel.class));
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque le modèle fourni est vide (non null mais blank)")
    void update_ShouldThrowBadRequestExceptionWhenModeleIsBlank() {
        // Arrange
        VehiculeDTO vehiculeAvecModeleVide = new VehiculeDTO(
                null,
                "ZZ-999-YY",
                "Honda",
                "  ",  // modele vide
                4,
                Motorisation.THERMIQUE,
                110,
                "http://example.com/civic.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculePersonnelTest));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.update(1L, vehiculeAvecModeleVide);
        });

        assertTrue(exception.getMessage().contains("Le modele est obligatoire"));

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque l'immatriculation fournie est vide (non null mais blank)")
    void update_ShouldThrowBadRequestExceptionWhenImmatriculationIsBlank() {
        // Arrange
        VehiculeDTO vehiculeAvecImmatVide = new VehiculeDTO(
                null,
                "   ",  // immatriculation vide
                "Honda",
                "Civic",
                4,
                Motorisation.THERMIQUE,
                110,
                "http://example.com/civic.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculePersonnelTest));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.update(1L, vehiculeAvecImmatVide);
        });

        assertTrue(exception.getMessage().contains("L'immatriculation est obligatoire"));

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque le nombre de places fourni est inférieur à 1")
    void update_ShouldThrowBadRequestExceptionWhenNbPlacesIsLessThanOne() {
        // Arrange
        VehiculeDTO vehiculeAvecNbPlacesInvalide = new VehiculeDTO(
                null,
                "ZZ-999-YY",
                "Honda",
                "Civic",
                0,  // nbPlaces invalide
                Motorisation.THERMIQUE,
                110,
                "http://example.com/civic.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculePersonnelTest));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.update(1L, vehiculeAvecNbPlacesInvalide);
        });

        assertTrue(exception.getMessage().contains("nombrePlaces doit être >= 1"));

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
    }

    @Test
    @DisplayName("Devrait lever une BadRequestException lorsque le CO2 par km fourni est négatif")
    void update_ShouldThrowBadRequestExceptionWhenCo2ParKmIsNegative() {
        // Arrange
        VehiculeDTO vehiculeAvecCo2Negatif = new VehiculeDTO(
                null,
                "ZZ-999-YY",
                "Honda",
                "Civic",
                4,
                Motorisation.THERMIQUE,
                -50,  // co2ParKm négatif
                "http://example.com/civic.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculePersonnelTest));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            vehiculePersonnelService.update(1L, vehiculeAvecCo2Negatif);
        });

        assertTrue(exception.getMessage().contains("co2ParKm doit être >= 0"));

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
    }

    @Test
    @DisplayName("Devrait mettre à jour avec succès lorsque la marque est null (permet de ne pas modifier ce champ)")
    void update_ShouldUpdateSuccessfullyWhenMarqueIsNull() {
        // Arrange
        VehiculeDTO vehiculeAvecMarqueNull = new VehiculeDTO(
                null,
                "ZZ-999-YY",
                null,  // marque null - OK pour update partiel
                "Civic",
                4,
                Motorisation.THERMIQUE,
                110,
                "http://example.com/civic.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        VehiculeDTO vehiculeDtoResultat = new VehiculeDTO(
                1L,
                "ZZ-999-YY",
                "Peugeot",  // marque originale conservée
                "Civic",
                4,
                Motorisation.THERMIQUE,
                110,
                "http://example.com/civic.jpg",
                Categorie.COMPACTE,
                null,
                1L
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculePersonnelTest));
        when(vehiculeMapper.toDto(vehiculePersonnelTest)).thenReturn(vehiculeDtoResultat);

        // Act
        VehiculeDTO resultat = vehiculePersonnelService.update(1L, vehiculeAvecMarqueNull);

        // Assert
        assertNotNull(resultat);
        assertEquals("Peugeot", resultat.marque()); // Marque originale conservée

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
    }

    @Test
    @DisplayName("Devrait mettre à jour avec succès en permettant de définir le CO2 par km à zéro (valeur limite valide)")
    void update_ShouldUpdateSuccessfullyWhenCo2ParKmIsZero() {
        // Arrange
        VehiculeDTO vehiculeAvecCo2Zero = new VehiculeDTO(
                null,
                "ELECTRIC-01",
                "Tesla",
                "Model 3",
                5,
                Motorisation.ELECTRIQUE,
                0,  // co2ParKm zéro - valide pour véhicule électrique
                "http://example.com/tesla.jpg",
                Categorie.BERLINE_M,
                null,
                null
        );

        VehiculeDTO vehiculeDtoResultat = new VehiculeDTO(
                1L,
                "ELECTRIC-01",
                "Tesla",
                "Model 3",
                5,
                Motorisation.ELECTRIQUE,
                0,
                "http://example.com/tesla.jpg",
                Categorie.BERLINE_M,
                null,
                1L
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculePersonnelTest));
        when(vehiculeMapper.toDto(vehiculePersonnelTest)).thenReturn(vehiculeDtoResultat);

        // Act
        VehiculeDTO resultat = vehiculePersonnelService.update(1L, vehiculeAvecCo2Zero);

        // Assert
        assertNotNull(resultat);
        assertEquals(0, resultat.co2ParKm());

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
    }

    @Test
    @DisplayName("Devrait mettre à jour toutes les propriétés optionnelles avec succès (motorisation, catégorie, photo)")
    void update_ShouldUpdateAllOptionalPropertiesSuccessfully() {
        // Arrange
        VehiculeDTO vehiculeAvecProprietesOptionelles = new VehiculeDTO(
                null,
                null,  // immatriculation non modifiée
                null,  // marque non modifiée
                null,  // modele non modifié
                null,  // nbPlaces non modifié
                Motorisation.ELECTRIQUE,  // motorisation modifiée
                null,  // co2ParKm non modifié
                "http://example.com/nouvelle-photo.jpg",  // photo modifiée
                Categorie.BERLINE_L,  // categorie modifiée
                null,
                null
        );

        VehiculeDTO vehiculeDtoResultat = new VehiculeDTO(
                1L,
                "AB-123-CD",
                "Peugeot",
                "308",
                5,
                Motorisation.ELECTRIQUE,
                120,
                "http://example.com/nouvelle-photo.jpg",
                Categorie.BERLINE_L,
                null,
                1L
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculePersonnelTest));
        when(vehiculeMapper.toDto(vehiculePersonnelTest)).thenReturn(vehiculeDtoResultat);

        // Act
        VehiculeDTO resultat = vehiculePersonnelService.update(1L, vehiculeAvecProprietesOptionelles);

        // Assert
        assertNotNull(resultat);
        assertEquals(Motorisation.ELECTRIQUE, resultat.motorisation());
        assertEquals("http://example.com/nouvelle-photo.jpg", resultat.photo());
        assertEquals(Categorie.BERLINE_L, resultat.categorie());

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
    }

    @Test
    @DisplayName("Devrait lever une ResponseStatusException et logger les violations lorsqu'une violation de contrainte de validation JPA se produit avec des violations détaillées")
    void create_ShouldThrowResponseStatusExceptionAndLogViolationsWhenConstraintViolationOccursWithViolations() {
        // Arrange
        VehiculeDTO nouveauVehiculeDto = new VehiculeDTO(
                null,
                "XY-999-ZZ",
                "Toyota",
                "Yaris",
                5,
                Motorisation.HYBRIDE,
                95,
                "http://example.com/yaris.jpg",
                Categorie.COMPACTE,
                null,
                null
        );

        VehiculePersonnel vehiculeACreer = new VehiculePersonnel(
                null,
                "XY-999-ZZ",
                5,
                "Yaris",
                95,
                "http://example.com/yaris.jpg",
                "Toyota",
                Motorisation.HYBRIDE,
                Categorie.COMPACTE,
                null
        );

        when(vehiculePersonnelRepository.existsByUtilisateurId(1L)).thenReturn(false);
        when(vehiculeMapper.toPersonnelEntity(nouveauVehiculeDto)).thenReturn(vehiculeACreer);
        when(utilisateurRepository.getReferenceById(1L)).thenReturn(utilisateurTest);

        // Créer un mock de ConstraintViolation
        javax.validation.ConstraintViolation<VehiculePersonnel> violation = mock(javax.validation.ConstraintViolation.class);
        when(violation.getRootBeanClass()).thenReturn((Class) VehiculePersonnel.class);
        when(violation.getPropertyPath()).thenReturn(mock(javax.validation.Path.class));
        when(violation.getMessage()).thenReturn("Validation message");

        // Créer un ensemble contenant la violation
        java.util.Set<javax.validation.ConstraintViolation<?>> violations = new java.util.HashSet<>();
        violations.add((javax.validation.ConstraintViolation<?>) violation);

        javax.validation.ConstraintViolationException constraintException =
                mock(javax.validation.ConstraintViolationException.class);
        when(constraintException.getMessage()).thenReturn("Validation failed");
        when(constraintException.getConstraintViolations()).thenReturn(violations);

        when(vehiculePersonnelRepository.saveAndFlush(any(VehiculePersonnel.class)))
                .thenThrow(constraintException);

        // Act & Assert
        org.springframework.web.server.ResponseStatusException exception =
                assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
                    vehiculePersonnelService.create(1L, nouveauVehiculeDto);
                });

        assertTrue(exception.getMessage().contains("Validation error"));
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(vehiculePersonnelRepository, times(1)).saveAndFlush(any(VehiculePersonnel.class));
        verify(constraintException, times(1)).getConstraintViolations();
    }

    @Test
    @DisplayName("Devrait conserver la photo existante lorsque le champ photo du DTO est null lors de la mise à jour")
    void update_ShouldKeepExistingPhotoWhenDtoPhotoIsNull() {
        // Arrange
        VehiculeDTO vehiculeAvecPhotoNull = new VehiculeDTO(
                null,
                "ZZ-999-YY",
                "Honda",
                "Civic",
                4,
                Motorisation.THERMIQUE,
                110,
                null,  // photo null - ne doit pas être modifiée
                Categorie.COMPACTE,
                null,
                null
        );

        VehiculeDTO vehiculeDtoResultat = new VehiculeDTO(
                1L,
                "ZZ-999-YY",
                "Honda",
                "Civic",
                4,
                Motorisation.THERMIQUE,
                110,
                "http://example.com/photo.jpg",  // Photo originale conservée
                Categorie.COMPACTE,
                null,
                1L
        );

        when(vehiculePersonnelRepository.findFirstByUtilisateurId(1L)).thenReturn(Optional.of(vehiculePersonnelTest));
        when(vehiculeMapper.toDto(vehiculePersonnelTest)).thenReturn(vehiculeDtoResultat);

        // Act
        VehiculeDTO resultat = vehiculePersonnelService.update(1L, vehiculeAvecPhotoNull);

        // Assert
        assertNotNull(resultat);
        assertEquals("http://example.com/photo.jpg", resultat.photo());  // Photo originale conservée

        verify(vehiculePersonnelRepository, times(1)).findFirstByUtilisateurId(1L);
    }

}
