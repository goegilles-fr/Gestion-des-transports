package fr.diginamic.Gestion_des_transports.services.impl;

import fr.diginamic.Gestion_des_transports.dto.ReservationVehiculeDTO;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;
import fr.diginamic.Gestion_des_transports.entites.VehiculeEntreprise;
import fr.diginamic.Gestion_des_transports.enums.StatutVehicule;
import fr.diginamic.Gestion_des_transports.mapper.ReservationVehiculeMapper;
import fr.diginamic.Gestion_des_transports.entites.ReservationVehicule;
import fr.diginamic.Gestion_des_transports.repositories.ReservationVehiculeRepository;
import fr.diginamic.Gestion_des_transports.repositories.VehiculeEntrepriseRepository;
import fr.diginamic.Gestion_des_transports.services.ReservationVehiculeService;
import fr.diginamic.Gestion_des_transports.services.VehiculeEntrepriseService;
import fr.diginamic.Gestion_des_transports.shared.BadRequestException;
import fr.diginamic.Gestion_des_transports.shared.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationVehiculeServiceImpl implements ReservationVehiculeService {

    private final ReservationVehiculeRepository repo;
    private final VehiculeEntrepriseRepository vehiculeEntrepriseRepo;
    private final ReservationVehiculeMapper reservationMapper;

    public ReservationVehiculeServiceImpl(ReservationVehiculeRepository repo,
                                          VehiculeEntrepriseRepository vehiculeEntrepriseRepo,
                                          ReservationVehiculeMapper reservationMapper) {
        this.repo = repo;
        this.vehiculeEntrepriseRepo = vehiculeEntrepriseRepo;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public List<ReservationVehiculeDTO> findAll() {
        return reservationMapper.toDtoList(repo.findAll());
    }

    @Override
    public ReservationVehiculeDTO findById(Utilisateur user, Long id) {
        ReservationVehicule entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable: " + id));
        if (!entity.getUtilisateur().getId().equals(user.getId())) {
            throw new BadRequestException("L'utilisateur ne correspond pas.");
        }
        return reservationMapper.toDto(entity);
    }

    @Override
    public ReservationVehiculeDTO create(Utilisateur user, ReservationVehiculeDTO dto) {
        if (dto.vehiculeId() == null) {
            throw new BadRequestException("Le vehiculeId est obligatoire.");
        }
        if (dto.dateDebut() == null || dto.dateFin() == null) {
            throw new BadRequestException("La dateDebut et la dateFin sont obligatoires.");
        }
        validateReservation(dto.vehiculeId(), dto.dateDebut(), dto.dateFin(), null);
        validateUser(dto.dateDebut(), dto.dateFin(), user.getId(), null);
        ReservationVehicule entity = reservationMapper.toEntity(dto);

        entity.setUtilisateur(user);
        entity.setVehiculeEntreprise(vehiculeEntrepriseRepo.getReferenceById(dto.vehiculeId()));

        ReservationVehicule saved = repo.save(entity);
        return reservationMapper.toDto(saved);
    }

    @Override
    public ReservationVehiculeDTO update(Utilisateur user, Long id, ReservationVehiculeDTO dto) {
        ReservationVehicule entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable: " + id));
        if (!entity.getUtilisateur().getId().equals(user.getId())) {
            throw new BadRequestException("L'utilisateur ne correspond pas.");
        }

        // On calcule les nouvelles valeurs effectives (permet l'update partiel)
        LocalDateTime newDebut = dto.dateDebut() != null ? dto.dateDebut() : entity.getDateDebut();
        LocalDateTime newFin = dto.dateFin() != null ? dto.dateFin() : entity.getDateFin();
        Long newVehiculeId = dto.vehiculeId() != null ? dto.vehiculeId() : entity.getVehiculeEntreprise().getId();

        // Validations temporelles sur le résultat effectif
        validateReservation(newVehiculeId, newDebut, newFin, id);
        validateUser(newDebut, newFin, user.getId(), id);
        // Mise à jour des champs SEULEMENT si fournis
        if (dto.dateDebut() != null) {
            entity.setDateDebut(dto.dateDebut());
        }
        if (dto.dateFin() != null) {
            entity.setDateFin(dto.dateFin());
        }
        if (dto.vehiculeId() != null) {
            entity.setVehiculeEntreprise(vehiculeEntrepriseRepo.getReferenceById(dto.vehiculeId()));
        }

        return reservationMapper.toDto(entity);
    }




    @Override
    public void delete(Utilisateur user, Long id) {
        ReservationVehicule entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable: " + id));
        if (!entity.getUtilisateur().getId().equals(user.getId())) {
            throw new BadRequestException("L'utilisateur ne correspond pas.");
        }
        repo.deleteById(id);
    }

    @Override
    public List<ReservationVehiculeDTO> findByUtilisateurId(Utilisateur user) {
        return reservationMapper.toDtoList(repo.findByUtilisateurId(user.getId()));
    }

    @Override
    public List<ReservationVehiculeDTO> findByVehiculeId(Long vehiculeId) {
        return reservationMapper.toDtoList(repo.findByVehiculeEntrepriseId(vehiculeId));
    }

    private void validateReservation(Long vehiculeId, LocalDateTime debut, LocalDateTime fin, Long reservationIdAExclure) {
        LocalDateTime now = LocalDateTime.now(); // horloge système
        if (!debut.isAfter(now)) {
            throw new BadRequestException("dateDebut doit être strictement postérieure à la date actuelle.");
        }
        if (!fin.isAfter(now)) {
            throw new BadRequestException("dateFin doit être strictement postérieure à la date actuelle.");
        }
        if (!debut.isBefore(fin)) {
            throw new BadRequestException("dateDebut doit être strictement antérieure à dateFin.");
        }

        VehiculeEntreprise vehicule = vehiculeEntrepriseRepo.findById(vehiculeId)
                .orElseThrow(() -> new NotFoundException("Véhicule introuvable: " + vehiculeId));

        if (vehicule.getStatut() != StatutVehicule.EN_SERVICE) {
            throw new BadRequestException("Le véhicule n'est pas en service. Statut actuel: " + vehicule.getStatut());
        }


        List<ReservationVehicule> reservationsExistantes = repo.findByVehiculeEntrepriseId(vehiculeId);
        // Verification que le vehicule est disponible pour ces dates
        for (ReservationVehicule reservationExistante : reservationsExistantes) {
            LocalDateTime debutExistant = reservationExistante.getDateDebut();
            LocalDateTime finExistante = reservationExistante.getDateFin();

            if (reservationExistante.getId().equals(reservationIdAExclure)) {
                continue;
            }

            // Vérification de chevauchement : deux périodes se chevauchent si :
            // - la nouvelle période commence avant la fin de l'existante ET
            // - la nouvelle période finit après le début de l'existante
            if (debut.isBefore(finExistante) && fin.isAfter(debutExistant)) {
                throw new BadRequestException("Le véhicule n'est pas disponible pour ces dates. " +
                        "Conflit avec une réservation existante du " + debutExistant + " au " + finExistante);
            }
        }
    }

    private void validateUser(LocalDateTime debut, LocalDateTime fin, Long userId, Long reservationIdAExclure) {
        // Vérification que l'utilisateur n'a pas d'autre réservation en même temps
        List<ReservationVehicule> reservationsUtilisateur = repo.findByUtilisateurId(userId);
        for (ReservationVehicule reservationUtilisateur : reservationsUtilisateur) {
            // Exclure la réservation courante si c'est une mise à jour
            if (reservationIdAExclure != null && reservationUtilisateur.getId().equals(reservationIdAExclure)) {
                continue;
            }

            LocalDateTime debutUtilisateur = reservationUtilisateur.getDateDebut();
            LocalDateTime finUtilisateur = reservationUtilisateur.getDateFin();

            // Vérification de chevauchement avec les autres réservations de l'utilisateur
            if (debut.isBefore(finUtilisateur) && fin.isAfter(debutUtilisateur)) {
                throw new BadRequestException("Vous avez déjà une réservation de véhicule pour cette période. " +
                        "Conflit avec votre réservation du " + debutUtilisateur + " au " + finUtilisateur);
            }
        }
    }




}