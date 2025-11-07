package fr.diginamic.gestiondestransports.unit;

import fr.diginamic.gestiondestransports.dto.VehiculeDTO;
import fr.diginamic.gestiondestransports.entites.ReservationVehicule;
import fr.diginamic.gestiondestransports.entites.VehiculeEntreprise;
import fr.diginamic.gestiondestransports.enums.Categorie;
import fr.diginamic.gestiondestransports.enums.Motorisation;
import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import fr.diginamic.gestiondestransports.mapper.VehiculeMapper;
import fr.diginamic.gestiondestransports.repositories.ReservationVehiculeRepository;
import fr.diginamic.gestiondestransports.repositories.VehiculeEntrepriseRepository;
import fr.diginamic.gestiondestransports.services.impl.VehiculeEntrepriseServiceImpl;
import fr.diginamic.gestiondestransports.shared.BadRequestException;
import fr.diginamic.gestiondestransports.shared.NotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehiculeEntrepriseServiceTest {

    @Mock
    private VehiculeEntrepriseRepository vehiculeEntrepriseRepository;

    @Mock
    private ReservationVehiculeRepository reservationVehiculeRepository;

    @Mock
    private VehiculeMapper vehiculeMapper;

    @InjectMocks
    private VehiculeEntrepriseServiceImpl vehiculeEntrepriseService;

    private VehiculeEntreprise vehiculeFactice;
    private VehiculeDTO vehiculeDtoFactice;

    @BeforeAll
    static void beforeAll() {
        System.out.println("Début de la campagne de tests VehiculeEntrepriseService");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Fin de la campagne de tests VehiculeEntrepriseService");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("Préparation des données de test");

        // Créer un véhicule d'entreprise factice
        vehiculeFactice = new VehiculeEntreprise();
        vehiculeFactice.setId(1L);
        vehiculeFactice.setImmatriculation("AB-123-CD");
        vehiculeFactice.setMarque("Renault");
        vehiculeFactice.setModele("Clio");
        vehiculeFactice.setNbPlaces(5);
        vehiculeFactice.setStatut(StatutVehicule.EN_SERVICE);
        vehiculeFactice.setMotorisation(Motorisation.ELECTRIQUE);
        vehiculeFactice.setCo2ParKm(0);
        vehiculeFactice.setCategorie(Categorie.COMPACTE);
        vehiculeFactice.setPhoto("photo.jpg");

        // Créer un DTO factice
        vehiculeDtoFactice = new VehiculeDTO(
                1L,
                "AB-123-CD",
                "Renault",
                "Clio",
                5,
                Motorisation.ELECTRIQUE,
                0,
                "photo.jpg",
                Categorie.COMPACTE,
                StatutVehicule.EN_SERVICE,
                null
        );
    }

    @AfterEach
    void afterEach() {
        System.out.println("Nettoyage après le test");
    }

    // ============================================
    // Tests pour findAll
    // ============================================

    @Test
    void findAll_ShouldReturnAllVehicles() {
        // Arrange
        List<VehiculeEntreprise> vehicules = Arrays.asList(vehiculeFactice);
        List<VehiculeDTO> vehiculeDtos = Arrays.asList(vehiculeDtoFactice);

        when(vehiculeEntrepriseRepository.findAll()).thenReturn(vehicules);
        when(vehiculeMapper.toDtoEntrepriseList(vehicules)).thenReturn(vehiculeDtos);

        // Act
        List<VehiculeDTO> resultat = vehiculeEntrepriseService.findAll();

        // Assert
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals("AB-123-CD", resultat.getFirst().immatriculation());
        verify(vehiculeEntrepriseRepository, times(1)).findAll();
        verify(vehiculeMapper, times(1)).toDtoEntrepriseList(vehicules);
    }

    @Test
    void findAll_ShouldReturnEmptyListWhenNoVehicles() {
        // Arrange
        when(vehiculeEntrepriseRepository.findAll()).thenReturn(Collections.emptyList());
        when(vehiculeMapper.toDtoEntrepriseList(anyList())).thenReturn(Collections.emptyList());

        // Act
        List<VehiculeDTO> resultat = vehiculeEntrepriseService.findAll();

        // Assert
        assertNotNull(resultat);
        assertTrue(resultat.isEmpty());
        verify(vehiculeEntrepriseRepository, times(1)).findAll();
    }

    // ============================================
    // Tests pour findById
    // ============================================

    @Test
    void findById_ShouldReturnVehicleById() {
        // Arrange
        when(vehiculeEntrepriseRepository.findById(1L)).thenReturn(Optional.of(vehiculeFactice));
        when(vehiculeMapper.toDto(vehiculeFactice)).thenReturn(vehiculeDtoFactice);

        // Act
        VehiculeDTO resultat = vehiculeEntrepriseService.findById(1L);

        // Assert
        assertNotNull(resultat);
        assertEquals(1L, resultat.id());
        assertEquals("AB-123-CD", resultat.immatriculation());
        verify(vehiculeEntrepriseRepository, times(1)).findById(1L);
        verify(vehiculeMapper, times(1)).toDto(vehiculeFactice);
    }

    @Test
    void findById_ShouldThrowNotFoundExceptionWhenVehicleNotFound() {
        // Arrange
        when(vehiculeEntrepriseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> vehiculeEntrepriseService.findById(999L));

        assertEquals("Véhicule d'entreprise introuvable: 999", exception.getMessage());
        verify(vehiculeEntrepriseRepository, times(1)).findById(999L);
        verify(vehiculeMapper, never()).toDto(any(VehiculeEntreprise.class));
    }

    // ============================================
    // Tests pour create
    // ============================================

    @Test
    void create_ShouldCreateVehicleSuccessfully() {
        // Arrange
        when(vehiculeMapper.toEntrepriseEntity(vehiculeDtoFactice)).thenReturn(vehiculeFactice);
        when(vehiculeEntrepriseRepository.save(vehiculeFactice)).thenReturn(vehiculeFactice);
        when(vehiculeMapper.toDto((VehiculeEntreprise) vehiculeFactice)).thenReturn(vehiculeDtoFactice);

        // Act
        VehiculeDTO resultat = vehiculeEntrepriseService.create(vehiculeDtoFactice);

        // Assert
        assertNotNull(resultat);
        assertEquals("AB-123-CD", resultat.immatriculation());
        verify(vehiculeMapper, times(1)).toEntrepriseEntity(vehiculeDtoFactice);
        verify(vehiculeEntrepriseRepository, times(1)).save(vehiculeFactice);
        verify(vehiculeMapper, times(1)).toDto(vehiculeFactice);
    }

    @Test
    void create_ShouldThrowBadRequestExceptionWhenMarqueIsNull() {
        // Arrange
        VehiculeDTO dtoSansMarque = new VehiculeDTO(null, "AB-123-CD", null, "Clio", 5,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.create(dtoSansMarque));

        assertEquals("La marque est obligatoire.", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestExceptionWhenMarqueIsBlank() {
        // Arrange
        VehiculeDTO dtoMarqueVide = new VehiculeDTO(null, "AB-123-CD", "   ", "Clio", 5,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.create(dtoMarqueVide));

        assertEquals("La marque est obligatoire.", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestExceptionWhenModeleIsNull() {
        // Arrange
        VehiculeDTO dtoSansModele = new VehiculeDTO(null, "AB-123-CD", "Renault", null, 5,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.create(dtoSansModele));

        assertEquals("Le modele est obligatoire.", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestExceptionWhenModeleIsBlank() {
        // Arrange
        VehiculeDTO dtoModeleVide = new VehiculeDTO(null, "AB-123-CD", "Renault", "   ", 5,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.create(dtoModeleVide));

        assertEquals("Le modele est obligatoire.", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestExceptionWhenImmatriculationIsNull() {
        // Arrange
        VehiculeDTO dtoSansImmat = new VehiculeDTO(null, null, "Renault", "Clio", 5,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.create(dtoSansImmat));

        assertEquals("L'immatriculation est obligatoire.", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestExceptionWhenImmatriculationIsBlank() {
        // Arrange
        VehiculeDTO dtoImmatVide = new VehiculeDTO(null, "   ", "Renault", "Clio", 5,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.create(dtoImmatVide));

        assertEquals("L'immatriculation est obligatoire.", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestExceptionWhenStatutIsNull() {
        // Arrange
        VehiculeDTO dtoSansStatut = new VehiculeDTO(null, "AB-123-CD", "Renault", "Clio", 5,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, null, null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.create(dtoSansStatut));

        assertEquals("Le statut est obligatoire pour enregistrer un véhicule d'entreprise.", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestExceptionWhenNbPlacesIsNull() {
        // Arrange
        VehiculeDTO dtoSansNbPlaces = new VehiculeDTO(null, "AB-123-CD", "Renault", "Clio", null,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.create(dtoSansNbPlaces));

        assertEquals("nombrePlaces doit être >= 1.", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestExceptionWhenNbPlacesIsLessThanOne() {
        // Arrange
        VehiculeDTO dtoNbPlacesInvalide = new VehiculeDTO(null, "AB-123-CD", "Renault", "Clio", 0,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.create(dtoNbPlacesInvalide));

        assertEquals("nombrePlaces doit être >= 1.", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestExceptionWhenCo2ParKmIsNegative() {
        // Arrange
        VehiculeDTO dtoCo2Negatif = new VehiculeDTO(null, "AB-123-CD", "Renault", "Clio", 5,
                Motorisation.ELECTRIQUE, -10, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.create(dtoCo2Negatif));

        assertEquals("co2ParKm doit être >= 0.", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestExceptionWhenUtilisateurIdIsNotNull() {
        // Arrange
        VehiculeDTO dtoAvecUtilisateur = new VehiculeDTO(null, "AB-123-CD", "Renault", "Clio", 5,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, 1L);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.create(dtoAvecUtilisateur));

        assertEquals("L'utilisateur ne doit pas etre renseigné pour un Vehicule d'entreprise.", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).save(any());
    }

    // ============================================
    // Tests pour update
    // ============================================

    @Test
    void update_ShouldUpdateVehicleSuccessfully() {
        // Arrange
        VehiculeDTO dtoMiseAJour = new VehiculeDTO(1L, "XY-456-ZZ", "Peugeot", "208", 4,
                Motorisation.HYBRIDE, 50, "newphoto.jpg", Categorie.MINI_CITADINE, StatutVehicule.EN_REPARATION, null);

        when(vehiculeEntrepriseRepository.findById(1L)).thenReturn(Optional.of(vehiculeFactice));
        when(vehiculeMapper.toDto((VehiculeEntreprise) vehiculeFactice)).thenReturn(dtoMiseAJour);

        // Act
        VehiculeDTO resultat = vehiculeEntrepriseService.update(1L, dtoMiseAJour);

        // Assert
        assertNotNull(resultat);
        verify(vehiculeEntrepriseRepository, times(1)).findById(1L);
        verify(vehiculeMapper, times(1)).toDto(vehiculeFactice);
    }

    @Test
    void update_ShouldThrowNotFoundExceptionWhenVehicleNotFound() {
        // Arrange
        when(vehiculeEntrepriseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> vehiculeEntrepriseService.update(999L, vehiculeDtoFactice));

        assertEquals("Véhicule d'entreprise introuvable: 999", exception.getMessage());
        verify(vehiculeEntrepriseRepository, times(1)).findById(999L);
    }

    @Test
    void update_ShouldThrowBadRequestExceptionWhenMarqueIsBlank() {
        // Arrange
        VehiculeDTO dtoMarqueVide = new VehiculeDTO(1L, "AB-123-CD", "   ", "Clio", 5,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        when(vehiculeEntrepriseRepository.findById(1L)).thenReturn(Optional.of(vehiculeFactice));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.update(1L, dtoMarqueVide));

        assertEquals("La marque est obligatoire.", exception.getMessage());
    }

    @Test
    void update_ShouldThrowBadRequestExceptionWhenModeleIsBlank() {
        // Arrange
        VehiculeDTO dtoModeleVide = new VehiculeDTO(1L, "AB-123-CD", "Renault", "   ", 5,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        when(vehiculeEntrepriseRepository.findById(1L)).thenReturn(Optional.of(vehiculeFactice));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.update(1L, dtoModeleVide));

        assertEquals("Le modele est obligatoire.", exception.getMessage());
    }

    @Test
    void update_ShouldThrowBadRequestExceptionWhenImmatriculationIsBlank() {
        // Arrange
        VehiculeDTO dtoImmatVide = new VehiculeDTO(1L, "   ", "Renault", "Clio", 5,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        when(vehiculeEntrepriseRepository.findById(1L)).thenReturn(Optional.of(vehiculeFactice));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.update(1L, dtoImmatVide));

        assertEquals("L'immatriculation est obligatoire.", exception.getMessage());
    }

    @Test
    void update_ShouldThrowBadRequestExceptionWhenNbPlacesIsLessThanOne() {
        // Arrange
        VehiculeDTO dtoNbPlacesInvalide = new VehiculeDTO(1L, "AB-123-CD", "Renault", "Clio", 0,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        when(vehiculeEntrepriseRepository.findById(1L)).thenReturn(Optional.of(vehiculeFactice));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.update(1L, dtoNbPlacesInvalide));

        assertEquals("nombrePlaces doit être >= 1.", exception.getMessage());
    }

    @Test
    void update_ShouldThrowBadRequestExceptionWhenCo2ParKmIsNegative() {
        // Arrange
        VehiculeDTO dtoCo2Negatif = new VehiculeDTO(1L, "AB-123-CD", "Renault", "Clio", 5,
                Motorisation.ELECTRIQUE, -10, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, null);

        when(vehiculeEntrepriseRepository.findById(1L)).thenReturn(Optional.of(vehiculeFactice));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.update(1L, dtoCo2Negatif));

        assertEquals("co2ParKm doit être >= 0.", exception.getMessage());
    }

    @Test
    void update_ShouldThrowBadRequestExceptionWhenUtilisateurIdIsNotNull() {
        // Arrange
        VehiculeDTO dtoAvecUtilisateur = new VehiculeDTO(1L, "AB-123-CD", "Renault", "Clio", 5,
                Motorisation.ELECTRIQUE, 0, "photo.jpg", Categorie.COMPACTE, StatutVehicule.EN_SERVICE, 1L);

        when(vehiculeEntrepriseRepository.findById(1L)).thenReturn(Optional.of(vehiculeFactice));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.update(1L, dtoAvecUtilisateur));

        assertEquals("L'utilisateur ne doit pas etre renseigné pour un Vehicule d'entreprise.", exception.getMessage());
    }

    // ============================================
    // Tests pour delete
    // ============================================

    @Test
    void delete_ShouldDeleteVehicleSuccessfully() {
        // Arrange
        when(vehiculeEntrepriseRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vehiculeEntrepriseRepository).deleteById(1L);

        // Act
        vehiculeEntrepriseService.delete(1L);

        // Assert
        verify(vehiculeEntrepriseRepository, times(1)).existsById(1L);
        verify(vehiculeEntrepriseRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowNotFoundExceptionWhenVehicleNotFound() {
        // Arrange
        when(vehiculeEntrepriseRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> vehiculeEntrepriseService.delete(999L));

        assertEquals("Véhicule d'entreprise introuvable: 999", exception.getMessage());
        verify(vehiculeEntrepriseRepository, times(1)).existsById(999L);
        verify(vehiculeEntrepriseRepository, never()).deleteById(anyLong());
    }

    // ============================================
    // Tests pour findByStatut
    // ============================================

    @Test
    void findByStatut_ShouldReturnVehiclesByStatut() {
        // Arrange
        List<VehiculeEntreprise> vehicules = Arrays.asList(vehiculeFactice);
        List<VehiculeDTO> vehiculeDtos = Arrays.asList(vehiculeDtoFactice);

        when(vehiculeEntrepriseRepository.findByStatut(StatutVehicule.EN_SERVICE)).thenReturn(vehicules);
        when(vehiculeMapper.toDtoEntrepriseList(vehicules)).thenReturn(vehiculeDtos);

        // Act
        List<VehiculeDTO> resultat = vehiculeEntrepriseService.findByStatut("EN_SERVICE");

        // Assert
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals(StatutVehicule.EN_SERVICE, resultat.getFirst().statut());
        verify(vehiculeEntrepriseRepository, times(1)).findByStatut(StatutVehicule.EN_SERVICE);
        verify(vehiculeMapper, times(1)).toDtoEntrepriseList(vehicules);
    }

    @Test
    void findByStatut_ShouldReturnEmptyListWhenNoVehiclesWithStatut() {
        // Arrange
        when(vehiculeEntrepriseRepository.findByStatut(StatutVehicule.HORS_SERVICE)).thenReturn(Collections.emptyList());
        when(vehiculeMapper.toDtoEntrepriseList(anyList())).thenReturn(Collections.emptyList());

        // Act
        List<VehiculeDTO> resultat = vehiculeEntrepriseService.findByStatut("HORS_SERVICE");

        // Assert
        assertNotNull(resultat);
        assertTrue(resultat.isEmpty());
        verify(vehiculeEntrepriseRepository, times(1)).findByStatut(StatutVehicule.HORS_SERVICE);
    }

// ============================================
// Tests pour findByAvailability
// ============================================

    @Test
    void findByAvailability_ShouldReturnAvailableVehicles() {
        // Arrange
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(1);
        LocalDateTime dateFin = LocalDateTime.now().plusDays(3);

        List<VehiculeEntreprise> vehiculesEnService = Arrays.asList(vehiculeFactice);
        List<VehiculeDTO> vehiculeDtos = Arrays.asList(vehiculeDtoFactice);

        when(vehiculeEntrepriseRepository.findByStatut(StatutVehicule.EN_SERVICE)).thenReturn(vehiculesEnService);
        when(reservationVehiculeRepository.findByVehiculeEntrepriseId(1L)).thenReturn(Collections.emptyList());
        when(vehiculeMapper.toDtoEntrepriseList(anyList())).thenReturn(vehiculeDtos);

        // Act
        List<VehiculeDTO> resultat = vehiculeEntrepriseService.findByAvailability(dateDebut, dateFin);

        // Assert
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        verify(vehiculeEntrepriseRepository, times(1)).findByStatut(StatutVehicule.EN_SERVICE);
        verify(reservationVehiculeRepository, times(1)).findByVehiculeEntrepriseId(1L);
    }

    @Test
    void findByAvailability_ShouldThrowBadRequestExceptionWhenDateFinBeforeDateDebut() {
        // Arrange
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(3);
        LocalDateTime dateFin = LocalDateTime.now().plusDays(1);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.findByAvailability(dateDebut, dateFin));

        assertEquals("DATES INCORRECTES : La date de début doit être antérieure à la date de fin", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).findByStatut(any());
    }

    @Test
    void findByAvailability_ShouldThrowBadRequestExceptionWhenDateFinEqualsDateDebut() {
        // Arrange
        LocalDateTime date = LocalDateTime.now().plusDays(1);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.findByAvailability(date, date));

        assertEquals("DATES INCORRECTES : La date de début doit être antérieure à la date de fin", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).findByStatut(any());
    }

    @Test
    void findByAvailability_ShouldThrowBadRequestExceptionWhenDateDebutIsInPast() {
        // Arrange
        LocalDateTime dateDebut = LocalDateTime.now().minusDays(1);
        LocalDateTime dateFin = LocalDateTime.now().plusDays(1);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.findByAvailability(dateDebut, dateFin));

        assertEquals("DATES INCORRECTES : La date de début doit être dans le futur", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).findByStatut(any());
    }

    @Test
    void findByAvailability_ShouldThrowBadRequestExceptionWhenDateFinIsInPast() {
        // Arrange
        LocalDateTime dateDebut = LocalDateTime.now().plusHours(1);
        LocalDateTime dateFin = LocalDateTime.now().minusDays(1);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> vehiculeEntrepriseService.findByAvailability(dateDebut, dateFin));

        assertEquals("DATES INCORRECTES : La date de début doit être antérieure à la date de fin", exception.getMessage());
        verify(vehiculeEntrepriseRepository, never()).findByStatut(any());
    }

    @Test
    void findByAvailability_ShouldThrowNotFoundExceptionWhenNoVehiclesInService() {
        // Arrange
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(1);
        LocalDateTime dateFin = LocalDateTime.now().plusDays(3);

        when(vehiculeEntrepriseRepository.findByStatut(StatutVehicule.EN_SERVICE)).thenReturn(Collections.emptyList());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> vehiculeEntrepriseService.findByAvailability(dateDebut, dateFin));

        assertEquals("Il n'y a pas de voitures en service", exception.getMessage());
        verify(vehiculeEntrepriseRepository, times(1)).findByStatut(StatutVehicule.EN_SERVICE);
    }

    @Test
    void findByAvailability_ShouldThrowNotFoundExceptionWhenNoAvailableVehicles() {
        // Arrange
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(1);
        LocalDateTime dateFin = LocalDateTime.now().plusDays(3);

        List<VehiculeEntreprise> vehiculesEnService = Arrays.asList(vehiculeFactice);

        // Créer une réservation qui chevauche complètement la période demandée
        ReservationVehicule reservation = new ReservationVehicule();
        reservation.setDateDebut(dateDebut);
        reservation.setDateFin(dateFin);
        List<ReservationVehicule> reservations = Arrays.asList(reservation);

        when(vehiculeEntrepriseRepository.findByStatut(StatutVehicule.EN_SERVICE)).thenReturn(vehiculesEnService);
        when(reservationVehiculeRepository.findByVehiculeEntrepriseId(1L)).thenReturn(reservations);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> vehiculeEntrepriseService.findByAvailability(dateDebut, dateFin));

        assertEquals("Aucune voiture disponible pour les dates sélectionnées", exception.getMessage());
        verify(vehiculeEntrepriseRepository, times(1)).findByStatut(StatutVehicule.EN_SERVICE);
        verify(reservationVehiculeRepository, times(1)).findByVehiculeEntrepriseId(1L);
    }

    @Test
    void findByAvailability_ShouldDetectOverlapWhenReservationStartsDuringPeriod() {
        // Arrange - Réservation commence pendant la période demandée
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(1);
        LocalDateTime dateFin = LocalDateTime.now().plusDays(5);

        List<VehiculeEntreprise> vehiculesEnService = Arrays.asList(vehiculeFactice);

        ReservationVehicule reservation = new ReservationVehicule();
        reservation.setDateDebut(LocalDateTime.now().plusDays(3)); // Commence pendant
        reservation.setDateFin(LocalDateTime.now().plusDays(7));   // Finit après
        List<ReservationVehicule> reservations = Arrays.asList(reservation);

        when(vehiculeEntrepriseRepository.findByStatut(StatutVehicule.EN_SERVICE)).thenReturn(vehiculesEnService);
        when(reservationVehiculeRepository.findByVehiculeEntrepriseId(1L)).thenReturn(reservations);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> vehiculeEntrepriseService.findByAvailability(dateDebut, dateFin));

        assertEquals("Aucune voiture disponible pour les dates sélectionnées", exception.getMessage());
    }

    @Test
    void findByAvailability_ShouldDetectOverlapWhenReservationEndsDuringPeriod() {
        // Arrange - Réservation finit pendant la période demandée
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(5);
        LocalDateTime dateFin = LocalDateTime.now().plusDays(10);

        List<VehiculeEntreprise> vehiculesEnService = Arrays.asList(vehiculeFactice);

        ReservationVehicule reservation = new ReservationVehicule();
        reservation.setDateDebut(LocalDateTime.now().plusDays(1)); // Commence avant
        reservation.setDateFin(LocalDateTime.now().plusDays(7));   // Finit pendant
        List<ReservationVehicule> reservations = Arrays.asList(reservation);

        when(vehiculeEntrepriseRepository.findByStatut(StatutVehicule.EN_SERVICE)).thenReturn(vehiculesEnService);
        when(reservationVehiculeRepository.findByVehiculeEntrepriseId(1L)).thenReturn(reservations);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> vehiculeEntrepriseService.findByAvailability(dateDebut, dateFin));

        assertEquals("Aucune voiture disponible pour les dates sélectionnées", exception.getMessage());
    }

    @Test
    void findByAvailability_ShouldDetectOverlapWhenReservationContainsPeriod() {
        // Arrange - Réservation englobe toute la période demandée
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(5);
        LocalDateTime dateFin = LocalDateTime.now().plusDays(7);

        List<VehiculeEntreprise> vehiculesEnService = Arrays.asList(vehiculeFactice);

        ReservationVehicule reservation = new ReservationVehicule();
        reservation.setDateDebut(LocalDateTime.now().plusDays(1));  // Commence avant
        reservation.setDateFin(LocalDateTime.now().plusDays(10));   // Finit après
        List<ReservationVehicule> reservations = Arrays.asList(reservation);

        when(vehiculeEntrepriseRepository.findByStatut(StatutVehicule.EN_SERVICE)).thenReturn(vehiculesEnService);
        when(reservationVehiculeRepository.findByVehiculeEntrepriseId(1L)).thenReturn(reservations);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> vehiculeEntrepriseService.findByAvailability(dateDebut, dateFin));

        assertEquals("Aucune voiture disponible pour les dates sélectionnées", exception.getMessage());
    }

    @Test
    void findByAvailability_ShouldAllowAdjacentReservationsWhenDateFinEqualsReservationDebut() {
        // Arrange - La période demandée finit exactement quand une réservation commence (OK)
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(1);
        LocalDateTime dateFin = LocalDateTime.now().plusDays(5);

        List<VehiculeEntreprise> vehiculesEnService = Arrays.asList(vehiculeFactice);
        List<VehiculeDTO> vehiculeDtos = Arrays.asList(vehiculeDtoFactice);

        ReservationVehicule reservation = new ReservationVehicule();
        reservation.setDateDebut(dateFin);                          // Commence exactement quand on finit
        reservation.setDateFin(LocalDateTime.now().plusDays(10));
        List<ReservationVehicule> reservations = Arrays.asList(reservation);

        when(vehiculeEntrepriseRepository.findByStatut(StatutVehicule.EN_SERVICE)).thenReturn(vehiculesEnService);
        when(reservationVehiculeRepository.findByVehiculeEntrepriseId(1L)).thenReturn(reservations);
        when(vehiculeMapper.toDtoEntrepriseList(anyList())).thenReturn(vehiculeDtos);

        // Act
        List<VehiculeDTO> resultat = vehiculeEntrepriseService.findByAvailability(dateDebut, dateFin);

        // Assert
        assertNotNull(resultat);
        assertEquals(1, resultat.size()); // Le véhicule est disponible car pas de chevauchement réel
    }

    @Test
    void findByAvailability_ShouldAllowAdjacentReservationsWhenDateDebutEqualsReservationFin() {
        // Arrange - La période demandée commence exactement quand une réservation finit (OK)
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(5);
        LocalDateTime dateFin = LocalDateTime.now().plusDays(10);

        List<VehiculeEntreprise> vehiculesEnService = Arrays.asList(vehiculeFactice);
        List<VehiculeDTO> vehiculeDtos = Arrays.asList(vehiculeDtoFactice);

        ReservationVehicule reservation = new ReservationVehicule();
        reservation.setDateDebut(LocalDateTime.now().plusDays(1));
        reservation.setDateFin(dateDebut);                          // Finit exactement quand on commence
        List<ReservationVehicule> reservations = Arrays.asList(reservation);

        when(vehiculeEntrepriseRepository.findByStatut(StatutVehicule.EN_SERVICE)).thenReturn(vehiculesEnService);
        when(reservationVehiculeRepository.findByVehiculeEntrepriseId(1L)).thenReturn(reservations);
        when(vehiculeMapper.toDtoEntrepriseList(anyList())).thenReturn(vehiculeDtos);

        // Act
        List<VehiculeDTO> resultat = vehiculeEntrepriseService.findByAvailability(dateDebut, dateFin);

        // Assert
        assertNotNull(resultat);
        assertEquals(1, resultat.size()); // Le véhicule est disponible car pas de chevauchement réel
    }
}