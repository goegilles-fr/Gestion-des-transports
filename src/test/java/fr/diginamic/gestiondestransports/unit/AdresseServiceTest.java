package fr.diginamic.gestiondestransports.unit;
import fr.diginamic.gestiondestransports.entites.Adresse;
import fr.diginamic.gestiondestransports.repositories.AdresseRepository;
import fr.diginamic.gestiondestransports.services.impl.AdresseServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class AdresseServiceTest {

    @Mock
    private AdresseRepository adresseRepository;

    @InjectMocks
    private AdresseServiceImpl adresseService;

    private Adresse adresseTest;

    @BeforeAll
    static void beforeAll() {
        System.out.println("Début de la campagne de tests AdresseServiceTest");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Fin de la campagne de tests AdresseServiceTest");
    }

    @BeforeEach
    void BeforeEach() {
        // Préparer une adresse de test réutilisable
        adresseTest = new Adresse(42, "Rue de la République", "34000", "Montpellier");
        adresseTest.setId(1L);
    }


    @Test
    @DisplayName("Devrait créer une nouvelle adresse à partir d'un objet Adresse et retourner l'adresse sauvegardée avec son ID généré")
    void creerAdresse_ShouldCreateAdresseAndReturnSaved() {
        // Arrange - Préparer les données
        Adresse nouvelleAdresse = new Adresse(15, "Avenue des Champs", "75008", "Paris");
        Adresse adresseSauvegardee = new Adresse(15, "Avenue des Champs", "75008", "Paris");
        adresseSauvegardee.setId(100L);

        when(adresseRepository.save(any(Adresse.class))).thenReturn(adresseSauvegardee);

        // Act - Exécuter la méthode
        Adresse resultat = adresseService.creerAdresse(nouvelleAdresse);

        // Assert - Vérifier les résultats
        assertNotNull(resultat);
        assertEquals(100L, resultat.getId());
        assertEquals(15, resultat.getNumero());
        assertEquals("Avenue des Champs", resultat.getLibelle());
        assertEquals("75008", resultat.getCodePostal());
        assertEquals("Paris", resultat.getVille());

        verify(adresseRepository, times(1)).save(nouvelleAdresse);
    }


    @Test
    @DisplayName("Devrait lever une exception IllegalArgumentException lors de la tentative de création d'une adresse avec un objet null")
    void creerAdresse_ShouldThrowIllegalArgumentExceptionWhenAdresseObjectIsNull() {

        // Arrange
        when(adresseRepository.save(null)).thenThrow(new IllegalArgumentException("L'adresse ne peut pas être null"));

        // Act Assert
        assertThrows(IllegalArgumentException.class, () -> {
            adresseService.creerAdresse(null);
        });

        verify(adresseRepository, times(1)).save(null);
    }



    @Test
    @DisplayName("Devrait créer une nouvelle adresse à partir des paramètres individuels et retourner l'adresse sauvegardée avec son ID")
    void creerAdresse_ShouldCreateAdresseFromIndividualParametersAndReturnSavedAdresse() {
        // Arrange
        Adresse adresseSauvegardee = new Adresse(8, "Boulevard Haussmann", "75009", "Paris");
        adresseSauvegardee.setId(50L);

        when(adresseRepository.save(any(Adresse.class))).thenReturn(adresseSauvegardee);

        // Act
        Adresse resultat = adresseService.creerAdresse(8, "Boulevard Haussmann", "75009", "Paris");

        // Assert
        assertNotNull(resultat);
        assertEquals(50L, resultat.getId());
        assertEquals(8, resultat.getNumero());
        assertEquals("Boulevard Haussmann", resultat.getLibelle());
        assertEquals("75009", resultat.getCodePostal());
        assertEquals("Paris", resultat.getVille());

        verify(adresseRepository, times(1)).save(any(Adresse.class));
    }

    @Test
    @DisplayName("Devrait créer une adresse valide même lorsque le numéro de rue est null, permettant les adresses sans numéro")
    void creerAdresse_ShouldCreateValidAdresseWhenNumeroIsNull(){
        // Arrange
        Adresse adresseSauvegardee = new Adresse(null, "Place de la Comédie", "34000", "Montpellier");
        adresseSauvegardee.setId(25L);

        when(adresseRepository.save(any(Adresse.class))).thenReturn(adresseSauvegardee);

        // Act
        Adresse resultat = adresseService.creerAdresse(null, "Place de la Comédie", "34000", "Montpellier");

        // Assert
        assertNotNull(resultat);
        assertEquals(25L, resultat.getId());
        assertNull(resultat.getNumero());
        assertEquals("Place de la Comédie", resultat.getLibelle());
        assertEquals("34000", resultat.getCodePostal());
        assertEquals("Montpellier", resultat.getVille());

        verify(adresseRepository, times(1)).save(any(Adresse.class));
    }

    // ========== Tests pour trouverParId ==========

    @Test
    @DisplayName("Devrait retourner l'adresse correspondante lorsqu'une adresse avec l'ID spécifié existe dans la base de données")
    void trouverParId_ShouldReturnAdresseWhenAdresseExistsWithGivenId() {
        // Arrange
        when(adresseRepository.findById(1L)).thenReturn(Optional.of(adresseTest));

        // Act
        Adresse resultat = adresseService.trouverParId(1L);

        // Assert
        assertNotNull(resultat);
        assertEquals(1L, resultat.getId());
        assertEquals(42, resultat.getNumero());
        assertEquals("Rue de la République", resultat.getLibelle());
        assertEquals("34000", resultat.getCodePostal());
        assertEquals("Montpellier", resultat.getVille());

        verify(adresseRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Devrait retourner null lorsqu'aucune adresse ne correspond à l'ID spécifié dans la base de données")
    void trouverParId_ShouldReturnNullWhenAdresseDoesNotExistWithGivenId() {
        // Arrange
        when(adresseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Adresse resultat = adresseService.trouverParId(999L);

        // Assert
        assertNull(resultat);

        verify(adresseRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Devrait retourner null lorsque l'ID fourni est null, évitant ainsi les erreurs de recherche")
    void trouverParId_ShouldReturnNullWhenIdIsNull() {
        // Arrange
        when(adresseRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Adresse resultat = adresseService.trouverParId(null);

        // Assert
        assertNull(resultat);

        verify(adresseRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("Devrait retourner la liste complète de toutes les adresses présentes dans la base de données")
    void obtenirToutesLesAdresses_ShouldReturnCompleteListOfAllAdresses() {
        // Arrange
        Adresse adresse1 = new Adresse(10, "Rue Victor Hugo", "34000", "Montpellier");
        adresse1.setId(1L);

        Adresse adresse2 = new Adresse(25, "Avenue Foch", "34000", "Montpellier");
        adresse2.setId(2L);

        Adresse adresse3 = new Adresse(5, "Boulevard Gambetta", "75020", "Paris");
        adresse3.setId(3L);

        List<Adresse> toutesLesAdresses = Arrays.asList(adresse1, adresse2, adresse3);

        when(adresseRepository.findAll()).thenReturn(toutesLesAdresses);

        // Act
        List<Adresse> resultat = adresseService.obtenirToutesLesAdresses();

        // Assert
        assertNotNull(resultat);
        assertEquals(3, resultat.size());
        assertEquals("Rue Victor Hugo", resultat.get(0).getLibelle());
        assertEquals("Avenue Foch", resultat.get(1).getLibelle());
        assertEquals("Boulevard Gambetta", resultat.get(2).getLibelle());

        verify(adresseRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Devrait retourner une liste vide lorsqu'aucune adresse n'existe dans la base de données")
    void obtenirToutesLesAdresses_ShouldReturnEmptyListWhenNoAdressesExist() {
        // Arrange
        when(adresseRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Adresse> resultat = adresseService.obtenirToutesLesAdresses();

        // Assert
        assertNotNull(resultat);
        assertTrue(resultat.isEmpty());
        assertEquals(0, resultat.size());

        verify(adresseRepository, times(1)).findAll();
    }
    @Test
    @DisplayName("Devrait supprimer l'adresse avec succès et retourner true lorsque l'adresse existe dans la base de données")
    void supprimerAdresse_ShouldDeleteAdresseAndReturnTrueWhenAdresseExists() {
        // Arrange
        when(adresseRepository.existsById(1L)).thenReturn(true);
        doNothing().when(adresseRepository).deleteById(1L);

        // Act
        boolean resultat = adresseService.supprimerAdresse(1L);

        // Assert
        assertTrue(resultat);

        verify(adresseRepository, times(1)).existsById(1L);
        verify(adresseRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Devrait retourner false sans effectuer de suppression lorsque l'adresse n'existe pas dans la base de données")
    void supprimerAdresse_ShouldReturnFalseWhenAdresseDoesNotExist() {
        // Arrange
        when(adresseRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean resultat = adresseService.supprimerAdresse(999L);

        // Assert
        assertFalse(resultat);

        verify(adresseRepository, times(1)).existsById(999L);
        verify(adresseRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Devrait retourner false lorsque l'ID fourni est null, sans tenter de suppression")
    void supprimerAdresse_ShouldReturnFalseWhenIdIsNull() {
        // Arrange
        when(adresseRepository.existsById(null)).thenReturn(false);

        // Act
        boolean resultat = adresseService.supprimerAdresse(null);

        // Assert
        assertFalse(resultat);

        verify(adresseRepository, times(1)).existsById(null);
        verify(adresseRepository, never()).deleteById(any());
    }
    @Test
    @DisplayName("Devrait mettre à jour toutes les propriétés de l'adresse et retourner l'adresse modifiée lorsque l'ID existe")
    void mettreAJourAdresse_ShouldUpdateAllPropertiesAndReturnUpdatedAdresseWhenIdExists() {
        // Arrange
        Adresse adresseExistante = new Adresse(10, "Ancienne Rue", "34000", "Montpellier");
        adresseExistante.setId(1L);

        Adresse adresseModifiee = new Adresse(20, "Nouvelle Rue", "75001", "Paris");

        Adresse adresseMiseAJour = new Adresse(20, "Nouvelle Rue", "75001", "Paris");
        adresseMiseAJour.setId(1L);

        when(adresseRepository.findById(1L)).thenReturn(Optional.of(adresseExistante));
        when(adresseRepository.save(any(Adresse.class))).thenReturn(adresseMiseAJour);

        // Act
        Adresse resultat = adresseService.mettreAJourAdresse(1L, adresseModifiee);

        // Assert
        assertNotNull(resultat);
        assertEquals(1L, resultat.getId());
        assertEquals(20, resultat.getNumero());
        assertEquals("Nouvelle Rue", resultat.getLibelle());
        assertEquals("75001", resultat.getCodePostal());
        assertEquals("Paris", resultat.getVille());

        verify(adresseRepository, times(1)).findById(1L);
        verify(adresseRepository, times(1)).save(any(Adresse.class));
    }

    @Test
    @DisplayName("Devrait retourner null sans effectuer de mise à jour lorsque l'adresse avec l'ID spécifié n'existe pas")
    void mettreAJourAdresse_ShouldReturnNullWhenAdresseDoesNotExist() {
        // Arrange
        Adresse adresseModifiee = new Adresse(20, "Nouvelle Rue", "75001", "Paris");

        when(adresseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Adresse resultat = adresseService.mettreAJourAdresse(999L, adresseModifiee);

        // Assert
        assertNull(resultat);

        verify(adresseRepository, times(1)).findById(999L);
        verify(adresseRepository, never()).save(any(Adresse.class));
    }

    @Test
    @DisplayName("Devrait permettre de mettre à jour le numéro à null pour les adresses sans numéro de rue")
    void mettreAJourAdresse_ShouldAllowUpdatingNumeroToNull() {
        // Arrange
        Adresse adresseExistante = new Adresse(10, "Place de la Comédie", "34000", "Montpellier");
        adresseExistante.setId(1L);

        Adresse adresseModifiee = new Adresse(null, "Place de la Comédie", "34000", "Montpellier");

        Adresse adresseMiseAJour = new Adresse(null, "Place de la Comédie", "34000", "Montpellier");
        adresseMiseAJour.setId(1L);

        when(adresseRepository.findById(1L)).thenReturn(Optional.of(adresseExistante));
        when(adresseRepository.save(any(Adresse.class))).thenReturn(adresseMiseAJour);

        // Act
        Adresse resultat = adresseService.mettreAJourAdresse(1L, adresseModifiee);

        // Assert
        assertNotNull(resultat);
        assertEquals(1L, resultat.getId());
        assertNull(resultat.getNumero());
        assertEquals("Place de la Comédie", resultat.getLibelle());

        verify(adresseRepository, times(1)).findById(1L);
        verify(adresseRepository, times(1)).save(any(Adresse.class));
    }

}