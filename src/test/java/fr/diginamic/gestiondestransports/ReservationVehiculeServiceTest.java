package fr.diginamic.gestiondestransports;

import fr.diginamic.gestiondestransports.dto.ReservationVehiculeDTO;
import fr.diginamic.gestiondestransports.entites.AnnonceCovoiturage;
import fr.diginamic.gestiondestransports.entites.ReservationVehicule;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.entites.VehiculeEntreprise;
import fr.diginamic.gestiondestransports.enums.Categorie;
import fr.diginamic.gestiondestransports.enums.Motorisation;
import fr.diginamic.gestiondestransports.enums.RoleEnum;
import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import fr.diginamic.gestiondestransports.mapper.ReservationVehiculeMapper;
import fr.diginamic.gestiondestransports.mapper.VehiculeMapper;
import fr.diginamic.gestiondestransports.repositories.AnnonceCovoiturageRepository;
import fr.diginamic.gestiondestransports.repositories.ReservationVehiculeRepository;
import fr.diginamic.gestiondestransports.repositories.UtilisateurRepository;
import fr.diginamic.gestiondestransports.repositories.VehiculeEntrepriseRepository;
import fr.diginamic.gestiondestransports.services.impl.ReservationVehiculeServiceImpl;
import fr.diginamic.gestiondestransports.shared.BadRequestException;
import fr.diginamic.gestiondestransports.shared.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationVehiculeServiceTest {
    @Mock
    ReservationVehiculeRepository reservationRepository;
    @Mock
    VehiculeEntrepriseRepository vehiculeEntrepriseRepo;
    @Mock
    AnnonceCovoiturageRepository annonceCovoiturageRepo;
    @Mock
    UtilisateurRepository userRepo;
    @Mock
    ReservationVehiculeMapper reservationMapper;
    @Mock
    VehiculeMapper vehiculeMapper;

    @InjectMocks
    ReservationVehiculeServiceImpl reservationService;

    private Long idReservation = 123L;
    private Utilisateur user;
    private VehiculeEntreprise vehicule;
    private LocalDateTime debut;
    private LocalDateTime fin;
    private ReservationVehiculeDTO reservationDto;

    @BeforeEach
    void setUp() {
        user = new Utilisateur("Dupont", "Dupont", "dupont@mail.com", RoleEnum.ROLE_USER);
        user.setId(5L);
        vehicule = new VehiculeEntreprise(idReservation, "FF-666-FF", 4, "Megane", 142,
                null, "Renault", Motorisation.THERMIQUE, Categorie.BERLINE_M, StatutVehicule.EN_SERVICE);
        vehicule.setId(5L);
        debut = LocalDateTime.of(2025, 11, 1, 9, 0, 0);
        fin = debut.plusHours(8);
        reservationDto = new ReservationVehiculeDTO(idReservation, user.getId(), vehicule.getId(), debut, fin);
    }

    @Test
    @DisplayName("findAll() -> renvoie la liste de DTO produite par le mapper")
    void findAll_shouldReturnDtoList() {
        // Reservation & DTO
        List<ReservationVehicule> reservations = List.of(new ReservationVehicule(user, vehicule, debut, fin));
        reservations.getFirst().setId(idReservation);

        List<ReservationVehiculeDTO> dtos = List.of(reservationDto);

        // When
        when(reservationRepository.findAll()).thenReturn(reservations);
        when(reservationMapper.toDtoList(reservations)).thenReturn(dtos);

        // Act
        List<ReservationVehiculeDTO> result = reservationService.findAll();

        // Assert
        assertEquals(dtos, result);
        verify(reservationRepository).findAll();
        verify(reservationMapper).toDtoList(reservations);
        verifyNoMoreInteractions(reservationRepository, reservationMapper);
    }

    @Test
    @DisplayName("findById → retourne le DTO si la réservation existe et appartient à l’utilisateur")
    void findById_shouldReturnReservationDto() {
        // Reservation & DTO
        ReservationVehicule entity = new ReservationVehicule(user, vehicule, debut, fin);
        entity.setId(idReservation);

        // When
        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));
        when(reservationMapper.toDto(entity)).thenReturn(reservationDto);

        // Act
        ReservationVehiculeDTO out = reservationService.findById(user, idReservation);

        // Assert
        assertEquals(reservationDto, out);
        verify(reservationRepository).findById(idReservation);
        verify(reservationMapper).toDto(entity);
        verifyNoMoreInteractions(reservationRepository, reservationMapper);
    }

    @Test
    @DisplayName("findById → NotFoundException si la réservation n’existe pas")
    void findById_notFound() {
        // When
        when(reservationRepository.findById(idReservation)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> reservationService.findById(user, idReservation));

        assertTrue(ex.getMessage().contains("Réservation introuvable"));
        verify(reservationRepository).findById(idReservation);
        verifyNoMoreInteractions(reservationRepository, reservationMapper);
    }

    @Test
    @DisplayName("findById → BadRequestException si l’utilisateur n’est pas le propriétaire")
    void findById_userMismatch() {
        // Utilisateur
        Utilisateur caller = new Utilisateur("Martin", "Alice", "alice@mail.com", RoleEnum.ROLE_USER);
        caller.setId(77L);

        // Reservation
        ReservationVehicule entity = new ReservationVehicule();
        entity.setId(idReservation);
        entity.setUtilisateur(user);

        // When
        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.findById(caller, idReservation));

        assertTrue(ex.getMessage().toLowerCase().contains("utilisateur"));
        verify(reservationRepository).findById(idReservation);
        verifyNoInteractions(reservationMapper);
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    @DisplayName("create(user,dto) → succès : set utilisateur & véhicule, save, retourne DTO")
    void create_ok() {
        // Le mapper renvoie une entité "vierge" (sans utilisateur ni vehiculeEntreprise)
        ReservationVehicule mapped = new ReservationVehicule();

        when(reservationMapper.toEntity(reservationDto)).thenReturn(mapped);

        // Le repo de véhicule renvoie une référence
        when(vehiculeEntrepriseRepo.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(vehiculeEntrepriseRepo.getReferenceById(vehicule.getId())).thenReturn(vehicule);

        // Le save renvoie une entité persistée
        ReservationVehicule persisted = new ReservationVehicule(user, vehicule, debut, fin);
        persisted.setId(5L);
        when(reservationRepository.save(any(ReservationVehicule.class))).thenReturn(persisted);

        // Le mapper toDto renvoie le DTO final
        when(reservationMapper.toDto(persisted)).thenReturn(reservationDto);

        // ACT
        ReservationVehiculeDTO result = reservationService.create(user, reservationDto);

        // ASSERT état
        assertEquals(reservationDto, result);

        // ASSERT interactions + contenu de l'entité passée à save()
        verify(reservationMapper).toEntity(reservationDto);
        verify(vehiculeEntrepriseRepo).getReferenceById(vehicule.getId());
        verify(reservationMapper).toDto(persisted);
    }

    @Test
    @DisplayName("create → KO si vehiculeId est null")
    void create_ko_vehiculeId_null() {
        var input = new ReservationVehiculeDTO(null, user.getId(), null, debut, fin);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reservationService.create(user, input));
        assertTrue(ex.getMessage().toLowerCase().contains("vehiculeid"));
        // rien d’autre ne doit être appelé quand les entrées sont invalides
        verifyNoInteractions(reservationMapper, reservationRepository);
    }

    @Test
    @DisplayName("create → KO si dateDebut est null")
    void create_ko_dateDebut_null() {
        var input = new ReservationVehiculeDTO(null, user.getId(), vehicule.getId(), null, fin);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reservationService.create(user, input));
        assertTrue(ex.getMessage().toLowerCase().contains("datedebut"));
        verifyNoInteractions(reservationMapper, reservationRepository);
    }

    @Test
    @DisplayName("create → KO si dateFin est null")
    void create_ko_dateFin_null() {
        var input = new ReservationVehiculeDTO(null, user.getId(), vehicule.getId(), debut, null);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reservationService.create(user, input));
        assertTrue(ex.getMessage().toLowerCase().contains("datefin"));
        verifyNoInteractions(reservationMapper, reservationRepository);
    }

    @Test
    @DisplayName("create → KO si chevauchement avec une réservation existante du même véhicule")
    void create_ko_chevauchement_vehicule() {
        // dates bien futures pour éviter les checks "now"
        LocalDateTime debut = LocalDateTime.of(2099, 10, 2, 10, 0, 0);
        LocalDateTime fin   = LocalDateTime.of(2099, 10, 2, 12, 0, 0);
        var input = new ReservationVehiculeDTO(null, user.getId(), vehicule.getId(), debut, fin);

        // véhicule EXISTANT et EN_SERVICE (sinon on tomberait sur un autre test/erreur)
        when(vehiculeEntrepriseRepo.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));

        // réservation EXISTANTE sur ce véhicule : 09:00–11:00 => chevauchement avec 10:00–12:00
        ReservationVehicule exist = new ReservationVehicule();
        exist.setId(100L);
        exist.setDateDebut(LocalDateTime.of(2099, 10, 2, 9, 0, 0));
        exist.setDateFin  (LocalDateTime.of(2099, 10, 2,11, 0, 0));
        when(reservationRepository.findByVehiculeEntrepriseId(vehicule.getId()))
                .thenReturn(List.of(exist));

        // WHEN / THEN
        BadRequestException ex = assertThrows(BadRequestException.class, () -> reservationService.create(user, input));
        assertTrue(ex.getMessage().toLowerCase().contains("pas disponible")
                || ex.getMessage().toLowerCase().contains("conflit"), ex.getMessage());

        // interactions pertinentes
        verify(vehiculeEntrepriseRepo).findById(vehicule.getId());
        verify(reservationRepository).findByVehiculeEntrepriseId(vehicule.getId());
        // rien d'autre (pas de mapping ni save si la validation échoue)
        verifyNoInteractions(reservationMapper);
        // getReferenceById ne doit PAS être appelé car on plante avant
        verify(vehiculeEntrepriseRepo, never()).getReferenceById(anyLong());
        verifyNoMoreInteractions(vehiculeEntrepriseRepo, reservationRepository);
    }

    @Test
    @DisplayName("create → KO si l'utilisateur a déjà une réservation chevauchante")
    void create_ko_chevauchement_utilisateur() {
        LocalDateTime debut = LocalDateTime.of(2099, 10, 3, 10, 0, 0);
        LocalDateTime fin   = LocalDateTime.of(2099, 10, 3, 12, 0, 0);
        var input = new ReservationVehiculeDTO(
                null, user.getId(), vehicule.getId(), debut, fin
        );

        when(vehiculeEntrepriseRepo.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(reservationRepository.findByVehiculeEntrepriseId(vehicule.getId()))
                .thenReturn(Collections.emptyList());

        // MAIS l'utilisateur a déjà une résa 09:00–11:00 le même jour => chevauche 10:00–12:00
        ReservationVehicule existUser = new ReservationVehicule();
        existUser.setId(999L);
        existUser.setDateDebut(LocalDateTime.of(2099, 10, 3, 9, 0, 0));
        existUser.setDateFin  (LocalDateTime.of(2099, 10, 3,11, 0, 0));
        when(reservationRepository.findByUtilisateurId(user.getId()))
                .thenReturn(List.of(existUser));

        // WHEN / THEN
        BadRequestException ex = assertThrows(BadRequestException.class, () -> reservationService.create(user, input));
        assertTrue(ex.getMessage().toLowerCase().contains("réservation")
                || ex.getMessage().toLowerCase().contains("conflit"), ex.getMessage());

        // Vérifie les lectures attendues
        verify(vehiculeEntrepriseRepo).findById(vehicule.getId());
        verify(reservationRepository).findByVehiculeEntrepriseId(vehicule.getId());
        verify(reservationRepository).findByUtilisateurId(user.getId());

        // Pas de mapping / save si la validation échoue
        verifyNoInteractions(reservationMapper);
        verify(vehiculeEntrepriseRepo, never()).getReferenceById(anyLong());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("update → OK : change dates et véhicule, passe validations, mappe vers DTO sans save")
    void update_ok_change_dates_and_vehicle() {
        ReservationVehicule entity = new ReservationVehicule(user, vehicule, debut, fin);
        entity.setId(idReservation);

        // nouveau véhicule
        VehiculeEntreprise v2 = new VehiculeEntreprise(idReservation, "NN-999-NN", 4, "Fiesta", 142,
                null, "Ford", Motorisation.THERMIQUE, Categorie.BERLINE_M, StatutVehicule.EN_SERVICE);
        v2.setId(5L);

        // nouvelles dates
        LocalDateTime dNew1 = LocalDateTime.of(2099, 10, 11, 9, 0);
        LocalDateTime dNew2 = LocalDateTime.of(2099, 10, 11, 18, 0);

        ReservationVehiculeDTO input = new ReservationVehiculeDTO(idReservation, user.getId(), v2.getId(), dNew1, dNew2);

        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));
        //validate reservation
        when(vehiculeEntrepriseRepo.findById(v2.getId())).thenReturn(Optional.of(v2));
        when(reservationRepository.findByVehiculeEntrepriseId(v2.getId())).thenReturn(Collections.emptyList());
        // Validate user
        when(reservationRepository.findByUtilisateurId(user.getId())).thenReturn(Collections.emptyList());
        when(vehiculeEntrepriseRepo.getReferenceById(v2.getId())).thenReturn(v2);
        // mapping sortie
        when(reservationMapper.toDto(entity)).thenReturn(input);

        // ACT
        ReservationVehiculeDTO out = reservationService.update(user, idReservation, input);

        // ASSERT
        assertEquals(input, out);
        assertEquals(dNew1, entity.getDateDebut());
        assertEquals(dNew2, entity.getDateFin());
        assertEquals(v2.getId(), entity.getVehiculeEntreprise().getId());

        verify(reservationRepository).findById(idReservation);
        verify(vehiculeEntrepriseRepo).findById(v2.getId());
        verify(reservationMapper).toDto(entity);

        // pas de save dans update
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("update → OK partiel : change uniquement la dateFin, aucun getReferenceById si vehiculeId null")
    void update_ok_partial_change_only_end_date() {
        ReservationVehicule entity = new ReservationVehicule(user, vehicule, debut, fin);
        entity.setId(idReservation);

        LocalDateTime dNew2 = LocalDateTime.of(2099, 10, 12, 19, 0);
        ReservationVehiculeDTO input = new ReservationVehiculeDTO(idReservation, user.getId(), null,  null, dNew2);

        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));
        when(vehiculeEntrepriseRepo.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(reservationRepository.findByVehiculeEntrepriseId(vehicule.getId())).thenReturn(Collections.emptyList());
        when(reservationRepository.findByUtilisateurId(user.getId())).thenReturn(Collections.emptyList());

        ReservationVehiculeDTO outDto = new ReservationVehiculeDTO(idReservation, user.getId(), vehicule.getId(), debut, dNew2);
        when(reservationMapper.toDto(entity)).thenReturn(outDto);

        ReservationVehiculeDTO out = reservationService.update(user, idReservation, input);

        assertEquals(outDto, out);
        assertEquals(debut, entity.getDateDebut());
        assertEquals(dNew2, entity.getDateFin());
        assertEquals(vehicule.getId(), entity.getVehiculeEntreprise().getId());

        verify(reservationRepository).findById(idReservation);
        verify(vehiculeEntrepriseRepo).findById(vehicule.getId());
        verify(reservationRepository).findByVehiculeEntrepriseId(vehicule.getId());
        verify(reservationRepository).findByUtilisateurId(user.getId());
        verify(vehiculeEntrepriseRepo, never()).getReferenceById(anyLong());
        verify(reservationMapper).toDto(entity);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("update → NotFound si la réservation n'existe pas")
    void update_not_found() {
        long resId = 9999L;
        when(reservationRepository.findById(resId)).thenReturn(Optional.empty());

        var input = new ReservationVehiculeDTO(resId, user.getId(), vehicule.getId(), fin.plusDays(1), fin.plusDays(1).plusHours(2));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> reservationService.update(user, resId, input));
        assertTrue(ex.getMessage().contains("introuvable"));

        verify(reservationRepository).findById(resId);
        verifyNoMoreInteractions(reservationRepository);
        verifyNoInteractions(vehiculeEntrepriseRepo, reservationMapper);
    }

    @Test
    @DisplayName("update → BadRequest si l'utilisateur n'est pas le propriétaire de la réservation")
    void update_user_mismatch() {
        Utilisateur other = new Utilisateur("Martin", "Alice", "alice@mail.com", RoleEnum.ROLE_USER);
        other.setId(42L);

        ReservationVehicule entity = new ReservationVehicule(other, vehicule, debut, fin);

        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));

        var input = new ReservationVehiculeDTO(idReservation, user.getId(), vehicule.getId(), fin.plusDays(2), fin.plusDays(2).plusHours(2));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.update(user, idReservation, input));
        assertTrue(ex.getMessage().toLowerCase().contains("utilisateur"));

        verify(reservationRepository).findById(idReservation);
        verifyNoMoreInteractions(reservationRepository);
        verifyNoInteractions(vehiculeEntrepriseRepo, reservationMapper);
    }

    @Test
    @DisplayName("update → KO si chevauchement avec une autre réservation du véhicule (exclusion de l'id courant respectée)")
    void update_ko_vehicle_overlap() {
        // réservation existante à mettre à jour
        LocalDateTime dOld1 = LocalDateTime.of(2099, 10, 14, 9, 0);
        LocalDateTime dOld2 = LocalDateTime.of(2099, 10, 14, 17, 0);
        ReservationVehicule entity = new ReservationVehicule(user, vehicule, dOld1, dOld2);
        entity.setId(idReservation);

        // on veut bouger sur dNew1–dNew2 qui chevauche une autre résa
        LocalDateTime dNew1 = LocalDateTime.of(2099, 10, 15, 9, 0);
        LocalDateTime dNew2 = LocalDateTime.of(2099, 10, 15, 17, 0);
        var input = new ReservationVehiculeDTO(idReservation, user.getId(), null, dNew1, dNew2);

        // le véhicule courant est en service et existe
        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));
        when(vehiculeEntrepriseRepo.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));

        // une autre réservation sur le même véhicule, avec un autre id, qui chevauche
        ReservationVehicule other = new ReservationVehicule();
        other.setId(888L); // ≠ idReservation
        other.setDateDebut(LocalDateTime.of(2099,10,15,9,0));
        other.setDateFin  (LocalDateTime.of(2099,10,15,10,0));
        when(reservationRepository.findByVehiculeEntrepriseId(vehicule.getId()))
                .thenReturn(List.of(other));

        // WHEN / THEN
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.update(user, idReservation, input));
        assertTrue(ex.getMessage().toLowerCase().contains("pas disponible")
                || ex.getMessage().toLowerCase().contains("conflit"));

        verify(reservationRepository).findById(idReservation);
        verify(vehiculeEntrepriseRepo).findById(vehicule.getId());
        verify(reservationRepository).findByVehiculeEntrepriseId(vehicule.getId());
        verify(reservationRepository, never()).save(any());
        verifyNoInteractions(reservationMapper);
    }

    @Test
    @DisplayName("update → KO si l'utilisateur a une autre réservation chevauchante (exclusion de l'id courant respectée)")
    void update_ko_user_overlap() {
        ReservationVehicule entity = new ReservationVehicule(user, vehicule, debut, fin);

        // On ne change pas de véhicule, on change la plage pour chevaucher une autre résa du même user
        LocalDateTime dNew1 = LocalDateTime.of(2099,10,15,10,0);
        LocalDateTime dNew2 = LocalDateTime.of(2099,10,15,12,0);
        var input = new ReservationVehiculeDTO(idReservation, user.getId(),  null, dNew1, dNew2);

        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));
        when(vehiculeEntrepriseRepo.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(reservationRepository.findByVehiculeEntrepriseId(vehicule.getId()))
                .thenReturn(Collections.emptyList());

        // autre réservation du même user, id différent, qui chevauche
        ReservationVehicule other = new ReservationVehicule();
        other.setId(777L); // ≠ idReservation
        other.setDateDebut(LocalDateTime.of(2099,10,15,9,0));
        other.setDateFin  (LocalDateTime.of(2099,10,15,11,0));
        when(reservationRepository.findByUtilisateurId(user.getId()))
                .thenReturn(List.of(other));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.update(user, idReservation, input));
        assertTrue(ex.getMessage().toLowerCase().contains("déjà une réservation")
                || ex.getMessage().toLowerCase().contains("conflit"));

        verify(reservationRepository).findById(idReservation);
        verify(vehiculeEntrepriseRepo).findById(vehicule.getId());
        verify(reservationRepository).findByVehiculeEntrepriseId(vehicule.getId());
        verify(reservationRepository).findByUtilisateurId(user.getId());
        verifyNoInteractions(reservationMapper);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("update → KO si le véhicule demandé n'est pas EN_SERVICE")
    void update_ko_target_vehicle_not_in_service() {
        ReservationVehicule entity = new ReservationVehicule(user, vehicule, debut, fin);

        VehiculeEntreprise v2 = new VehiculeEntreprise();
        v2.setId(199L);
        v2.setStatut(StatutVehicule.HORS_SERVICE);

        LocalDateTime dNew1 = LocalDateTime.of(2099,10,16,10,0);
        LocalDateTime dNew2 = LocalDateTime.of(2099,10,16,12,0);
        var input = new ReservationVehiculeDTO(idReservation, user.getId(), v2.getId(), dNew1, dNew2);

        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));
        when(vehiculeEntrepriseRepo.findById(v2.getId())).thenReturn(Optional.of(v2));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.update(user, idReservation, input));
        assertTrue(ex.getMessage().toLowerCase().contains("pas en service"));

        verify(reservationRepository).findById(idReservation);
        verify(vehiculeEntrepriseRepo).findById(v2.getId());
        verify(reservationRepository, never()).save(any());
        verifyNoInteractions(reservationMapper);
    }

    @Test
    @DisplayName("delete → OK si aucune annonce de covoiturage ne chevauche la réservation")
    void delete_ok_no_carpool_conflict() {
        ReservationVehicule entity = new ReservationVehicule(user, vehicule, debut, fin);
        entity.setId(idReservation);

        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));
        when(annonceCovoiturageRepo.findByVehiculeServiceIdBetweenDates(vehicule.getId(), debut, fin))
                .thenReturn(Collections.emptyList());

        reservationService.delete(user, idReservation);

        verify(reservationRepository).findById(idReservation);
        verify(annonceCovoiturageRepo).findByVehiculeServiceIdBetweenDates(vehicule.getId(), debut, fin);
        verify(reservationRepository).deleteById(idReservation);
    }

    @Test
    @DisplayName("delete → OK si la réservation n'a pas de véhicule (aucune vérif d'annonces), puis suppression")
    void delete_ok_null_vehicle() {
        ReservationVehicule entity = new ReservationVehicule(user, null, debut, fin);
        entity.setId(idReservation);

        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));

        reservationService.delete(user, idReservation);

        verify(reservationRepository).findById(idReservation);
        verifyNoInteractions(annonceCovoiturageRepo);
        verify(reservationRepository).deleteById(idReservation);
    }

    @Test
    @DisplayName("delete → NotFound si la réservation n'existe pas")
    void delete_not_found() {
        long resId = 999L;
        when(reservationRepository.findById(resId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> reservationService.delete(user, resId));
        assertTrue(ex.getMessage().contains("introuvable"));

        verify(reservationRepository).findById(resId);
        verify(reservationRepository, never()).deleteById(anyLong());
        verifyNoInteractions(annonceCovoiturageRepo);
    }

    @Test
    @DisplayName("delete → BadRequest si l'utilisateur n'est pas le propriétaire")
    void delete_user_mismatch() {
        Utilisateur other = new Utilisateur("Martin", "Alice", "alice@mail.com", RoleEnum.ROLE_USER);
        other.setId(42L);

        ReservationVehicule entity = new ReservationVehicule(other, vehicule, debut, fin);
        entity.setId(idReservation);

        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.delete(user, idReservation));
        assertTrue(ex.getMessage().toLowerCase().contains("utilisateur"));

        verify(reservationRepository).findById(idReservation);
        verifyNoInteractions(annonceCovoiturageRepo);
        verify(reservationRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete → BadRequest si une annonce de covoiturage chevauche la réservation")
    void delete_ko_conflict_with_carpool() {
        LocalDateTime d1 = LocalDateTime.of(2099, 10, 4, 9, 0);  // réservation
        LocalDateTime d2 = LocalDateTime.of(2099, 10, 4, 12, 0);

        ReservationVehicule entity = new ReservationVehicule(user, vehicule, d1, d2);
        entity.setId(idReservation);

        // annonce départ 10:00, durée 90 min → 11:30 : CHEVAUCHE 09:00–12:00
        AnnonceCovoiturage annonce = new AnnonceCovoiturage();
        annonce.setId(555L);
        annonce.setHeureDepart(LocalDateTime.of(2099, 10, 4, 10, 0));
        annonce.setDureeTrajet(90);

        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));
        when(annonceCovoiturageRepo.findByVehiculeServiceIdBetweenDates(vehicule.getId(), d1, d2))
                .thenReturn(List.of(annonce));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.delete(user, idReservation));
        assertTrue(ex.getMessage().toLowerCase().contains("impossible de supprimer"));

        verify(reservationRepository).findById(idReservation);
        verify(annonceCovoiturageRepo).findByVehiculeServiceIdBetweenDates(vehicule.getId(), d1, d2);
        verify(reservationRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete → OK si des annonces existent mais ne chevauchent PAS la réservation")
    void delete_ok_annonces_non_overlapping() {
        ReservationVehicule entity = new ReservationVehicule(user, vehicule, debut, fin);
        entity.setId(idReservation);

        // Annonce avant (06:00–07:00) → pas de chevauchement
        AnnonceCovoiturage a1 = new AnnonceCovoiturage();
        a1.setId(1L);
        a1.setHeureDepart(LocalDateTime.of(2099, 10, 5, 6, 0));
        a1.setDureeTrajet(60);

        // Annonce après (13:00–13:30) → pas de chevauchement
        AnnonceCovoiturage a2 = new AnnonceCovoiturage();
        a2.setId(2L);
        a2.setHeureDepart(LocalDateTime.of(2099, 10, 5, 13, 0));
        a2.setDureeTrajet(30);

        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));
        when(annonceCovoiturageRepo.findByVehiculeServiceIdBetweenDates(vehicule.getId(), debut, fin))
                .thenReturn(List.of(a1, a2));

        reservationService.delete(user, idReservation);

        verify(reservationRepository).findById(idReservation);
        verify(annonceCovoiturageRepo).findByVehiculeServiceIdBetweenDates(vehicule.getId(), debut, fin);
        verify(reservationRepository).deleteById(idReservation);
    }

    @Test
    @DisplayName("findByUtilisateurAndPeriode → OK : renvoie le DTO couvrant la période")
    void findByUtilisateurAndPeriode_ok() {
        LocalDateTime debut = LocalDateTime.of(2099, 10, 20, 9, 0);
        int duree = 90; // minutes
        LocalDateTime finRecherche = debut.plusMinutes(duree);

        ReservationVehicule entity = new ReservationVehicule(user, vehicule, debut, finRecherche);
        entity.setId(idReservation);

        when(reservationRepository.findByUtilisateurIdAndPeriodeCouvrante(
                eq(user.getId()), eq(debut), eq(finRecherche)
        )).thenReturn(Optional.of(entity));

        ReservationVehiculeDTO dto = new ReservationVehiculeDTO(idReservation, user.getId(), vehicule.getId(), debut, debut.plusMinutes(duree));
        when(reservationMapper.toDto(entity)).thenReturn(dto);

        ReservationVehiculeDTO out = reservationService.findByUtilisateurAndPeriode(user, debut, duree);

        assertEquals(dto, out);
        verify(reservationRepository).findByUtilisateurIdAndPeriodeCouvrante(user.getId(), debut, finRecherche);
        verify(reservationMapper).toDto(entity);
        verifyNoMoreInteractions(reservationRepository, reservationMapper);
    }

    @Test
    @DisplayName("findByUtilisateurAndPeriode → KO si dateDebut est null")
    void findByUtilisateurAndPeriode_ko_dateDebut_null() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.findByUtilisateurAndPeriode(user, null, 10));
        assertTrue(ex.getMessage().toLowerCase().contains("datedebut"));
        verifyNoInteractions(reservationRepository, reservationMapper);
    }

    @Test
    @DisplayName("findByUtilisateurAndPeriode → KO si dureeMinutes est null")
    void findByUtilisateurAndPeriode_ko_duree_null() {
        LocalDateTime debut = LocalDateTime.of(2099, 10, 20, 9, 0);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.findByUtilisateurAndPeriode(user, debut, null));
        assertTrue(ex.getMessage().toLowerCase().contains("duree"));
        verifyNoInteractions(reservationRepository, reservationMapper);
    }

    @Test
    @DisplayName("findByUtilisateurAndPeriode → KO si dureeMinutes <= 0")
    void findByUtilisateurAndPeriode_ko_duree_non_positive() {
        LocalDateTime debut = LocalDateTime.of(2099, 10, 20, 9, 0);

        BadRequestException ex1 = assertThrows(BadRequestException.class,
                () -> reservationService.findByUtilisateurAndPeriode(user, debut, 0));
        assertTrue(ex1.getMessage().contains("strictement positive"));

        BadRequestException ex2 = assertThrows(BadRequestException.class,
                () -> reservationService.findByUtilisateurAndPeriode(user, debut, -15));
        assertTrue(ex2.getMessage().contains("strictement positive"));

        verifyNoInteractions(reservationRepository, reservationMapper);
    }

    @Test
    @DisplayName("findByUtilisateurAndPeriode → NotFound si aucune réservation ne couvre la période")
    void findByUtilisateurAndPeriode_not_found() {
        LocalDateTime debut = LocalDateTime.of(2099, 10, 20, 9, 0);
        int duree = 45;
        LocalDateTime finRecherche = debut.plusMinutes(duree);

        when(reservationRepository.findByUtilisateurIdAndPeriodeCouvrante(
                eq(user.getId()), eq(debut), eq(finRecherche)
        )).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> reservationService.findByUtilisateurAndPeriode(user, debut, duree));
        assertTrue(ex.getMessage().toLowerCase().contains("aucune réservation"));

        verify(reservationRepository).findByUtilisateurIdAndPeriodeCouvrante(user.getId(), debut, finRecherche);
        verifyNoInteractions(reservationMapper);
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    @DisplayName("findByUtilisateurId → renvoie la liste de DTO produite par le mapper")
    void findByUtilisateurId_shouldReturnDtoList() {
        ReservationVehicule enitity = new ReservationVehicule(user, vehicule, debut, fin);
        enitity.setId(idReservation);

        List<ReservationVehicule> entities = List.of(enitity);

        ReservationVehiculeDTO dto = new ReservationVehiculeDTO(idReservation, user.getId(), vehicule.getId(), debut, fin);
        List<ReservationVehiculeDTO> dtos = List.of(dto);

        when(reservationRepository.findByUtilisateurId(user.getId())).thenReturn(entities);
        when(reservationMapper.toDtoList(entities)).thenReturn(dtos);

        // WHEN
        List<ReservationVehiculeDTO> out = reservationService.findByUtilisateurId(user);

        // THEN
        assertEquals(dtos, out);
        verify(reservationRepository).findByUtilisateurId(user.getId());
        verify(reservationMapper).toDtoList(entities);
        verifyNoMoreInteractions(reservationRepository, reservationMapper);
    }

    @Test
    @DisplayName("findByUtilisateurId → liste vide")
    void findByUtilisateurId_empty_shouldReturnEmptyList() {
        List<ReservationVehicule> emptyEntities = List.of();
        List<ReservationVehiculeDTO> emptyDtos = List.of();

        when(reservationRepository.findByUtilisateurId(user.getId())).thenReturn(emptyEntities);
        when(reservationMapper.toDtoList(emptyEntities)).thenReturn(emptyDtos);

        List<ReservationVehiculeDTO> out = reservationService.findByUtilisateurId(user);

        assertTrue(out.isEmpty());
        verify(reservationRepository).findByUtilisateurId(user.getId());
        verify(reservationMapper).toDtoList(emptyEntities);
        verifyNoMoreInteractions(reservationRepository, reservationMapper);
    }

    @Test
    @DisplayName("findByVehiculeId → renvoie la liste de DTO produite par le mapper")
    void findByVehiculeId_shouldReturnDtoList() {
        ReservationVehicule entity = new ReservationVehicule(user, vehicule, debut, fin);
        entity.setId(idReservation);

        List<ReservationVehicule> entities = List.of(entity);

        ReservationVehiculeDTO dto = new ReservationVehiculeDTO(idReservation, user.getId(), vehicule.getId(), debut, fin);
        List<ReservationVehiculeDTO> dtos = List.of(dto);

        when(reservationRepository.findByVehiculeEntrepriseId(vehicule.getId())).thenReturn(entities);
        when(reservationMapper.toDtoList(entities)).thenReturn(dtos);

        List<ReservationVehiculeDTO> out = reservationService.findByVehiculeId(vehicule.getId());

        assertEquals(dtos, out);
        verify(reservationRepository).findByVehiculeEntrepriseId(vehicule.getId());
        verify(reservationMapper).toDtoList(entities);
        verifyNoMoreInteractions(reservationRepository, reservationMapper);
    }

    @Test
    @DisplayName("findByVehiculeId → liste vide")
    void findByVehiculeId_empty_shouldReturnEmptyList() {
        Long vehiculeId = 9L;

        List<ReservationVehicule> emptyEntities = List.of();
        List<ReservationVehiculeDTO> emptyDtos = List.of();

        when(reservationRepository.findByVehiculeEntrepriseId(vehiculeId)).thenReturn(emptyEntities);
        when(reservationMapper.toDtoList(emptyEntities)).thenReturn(emptyDtos);

        List<ReservationVehiculeDTO> out = reservationService.findByVehiculeId(vehiculeId);

        assertTrue(out.isEmpty());
        verify(reservationRepository).findByVehiculeEntrepriseId(9L);
        verify(reservationMapper).toDtoList(emptyEntities);
        verifyNoMoreInteractions(reservationRepository, reservationMapper);
    }

    @Test
    @DisplayName("validateReservation → KO si dateDebut <= now (via create)")
    void validateReservation_ko_dateDebut_notAfterNow() {
        // dateDebut passée
        LocalDateTime debut = LocalDateTime.now().minusMinutes(1);
        LocalDateTime fin   = LocalDateTime.now().plusHours(1);

        ReservationVehiculeDTO dto = new ReservationVehiculeDTO(idReservation, user.getId(), vehicule.getId(), debut, fin);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.create(user, dto));
        assertTrue(ex.getMessage().toLowerCase().contains("datedebut"));

        verifyNoInteractions(reservationMapper);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("validateReservation → KO si dateFin <= now (via create)")
    void validateReservation_ko_dateFin_notAfterNow() {
        LocalDateTime debut = LocalDateTime.now().plusMinutes(10);
        LocalDateTime fin   = LocalDateTime.now().minusMinutes(1);

        ReservationVehiculeDTO dto = new ReservationVehiculeDTO(idReservation, user.getId(), vehicule.getId(), debut, fin);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.create(user, dto));
        assertTrue(ex.getMessage().toLowerCase().contains("datefin"));

        verifyNoInteractions(reservationMapper);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("validateReservation → KO si dateDebut >= dateFin (via create)")
    void validateReservation_ko_debut_notBefore_fin() {
        LocalDateTime debut = LocalDateTime.of(2099,10,1,12,0);
        LocalDateTime fin   = debut.minusMinutes(10);

        ReservationVehiculeDTO dto = new ReservationVehiculeDTO(idReservation, user.getId(), vehicule.getId(), debut, fin);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.create(user, dto));
        assertTrue(ex.getMessage().toLowerCase().contains("antérieure"));

        verifyNoInteractions(reservationMapper);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("validateReservation → KO si le véhicule est introuvable (via create)")
    void validateReservation_ko_vehicle_not_found() {
        when(vehiculeEntrepriseRepo.findById(vehicule.getId())).thenReturn(Optional.empty());

        ReservationVehiculeDTO dto = new ReservationVehiculeDTO(idReservation, user.getId(), vehicule.getId(), debut, fin);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> reservationService.create(user, dto));
        assertTrue(ex.getMessage().toLowerCase().contains("véhicule introuvable"));

        verifyNoInteractions(reservationMapper);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("validateReservation → KO si le véhicule n'est pas EN_SERVICE (via create)")
    void validateReservation_ko_vehicle_not_in_service() {
        Long vehiculeId = 5L;
        VehiculeEntreprise v = new VehiculeEntreprise();
        v.setId(vehiculeId);
        v.setStatut(StatutVehicule.HORS_SERVICE);

        when(vehiculeEntrepriseRepo.findById(vehiculeId)).thenReturn(Optional.of(v));

        ReservationVehiculeDTO dto = new ReservationVehiculeDTO(idReservation, user.getId(), vehiculeId, debut, fin);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.create(user, dto));
        assertTrue(ex.getMessage().toLowerCase().contains("pas en service"));

        verifyNoInteractions(reservationMapper);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("validateReservation → KO si chevauchement avec une réservation existante du véhicule (via create)")
    void validateReservation_ko_overlap_vehicle() {
        LocalDateTime d1 = LocalDateTime.of(2099,10,4,10,0);
        LocalDateTime d2 = LocalDateTime.of(2099,10,4,12,0);

        when(vehiculeEntrepriseRepo.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));

        // Réservation existante 09:00–11:00 → chevauche 10:00–12:00
        ReservationVehicule exist = new ReservationVehicule();
        exist.setId(111L);
        exist.setDateDebut(LocalDateTime.of(2099,10,4,9,0));
        exist.setDateFin  (LocalDateTime.of(2099,10,4,11,0));
        when(reservationRepository.findByVehiculeEntrepriseId(vehicule.getId()))
                .thenReturn(List.of(exist));

        ReservationVehiculeDTO dto = new ReservationVehiculeDTO(idReservation, user.getId(), vehicule.getId(), d1, d2);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reservationService.create(user, dto));
        assertTrue(ex.getMessage().toLowerCase().contains("pas disponible")
                || ex.getMessage().toLowerCase().contains("conflit"));

        verifyNoInteractions(reservationMapper);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("validateReservation → OK en update : chevauchement avec soi-même ignoré grâce à reservationIdAExclure")
    void validateReservation_ok_self_overlap_excluded_in_update() {
        LocalDateTime oldStart = LocalDateTime.of(2099,10,5,9,0);
        LocalDateTime oldEnd   = LocalDateTime.of(2099,10,5,17,0);

        ReservationVehicule entity = new ReservationVehicule(user, vehicule, oldStart, oldEnd);
        entity.setId(idReservation);

        when(reservationRepository.findById(idReservation)).thenReturn(Optional.of(entity));
        when(vehiculeEntrepriseRepo.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));

        // Le repo renvoie la liste contenant la "même" résa -> doit être ignorée par l'exclusion
        when(reservationRepository.findByVehiculeEntrepriseId(vehicule.getId()))
                .thenReturn(List.of(entity));

        // On ne change que légèrement la plage mais qui chevaucherait "elle-même"
        LocalDateTime newStart = LocalDateTime.of(2099,10,5,10,0);
        LocalDateTime newEnd   = LocalDateTime.of(2099,10,5,12,0);
        ReservationVehiculeDTO patch = new ReservationVehiculeDTO(idReservation, user.getId(), null, newStart, newEnd);

        // le mapper n'est pas nécessaire si tu fais juste l'update sans save ; on stub le toDto pour terminer proprement
        when(reservationMapper.toDto(entity)).thenReturn(new ReservationVehiculeDTO(idReservation, user.getId(), vehicule.getId(), newStart, newEnd));

        ReservationVehiculeDTO out = reservationService.update(user, idReservation, patch);

        assertEquals(newStart, entity.getDateDebut());
        assertEquals(newEnd,   entity.getDateFin());
        assertEquals(vehicule.getId(), entity.getVehiculeEntreprise().getId());
        assertEquals(idReservation, out.id());

        verify(reservationRepository).findById(idReservation);
        verify(vehiculeEntrepriseRepo).findById(vehicule.getId());
        verify(reservationRepository).findByVehiculeEntrepriseId(vehicule.getId());
        verify(reservationMapper).toDto(entity);
        verify(reservationRepository, never()).save(any());
    }

}
