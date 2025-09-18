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
import fr.diginamic.Gestion_des_transports.shared.BadRequestException;
import fr.diginamic.Gestion_des_transports.shared.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        validateReservation(dto.vehiculeId(), dto.dateDebut(), dto.dateFin());

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

        // On calcule les nouvelles valeurs effectives (permet l’update partiel)
        LocalDateTime newDebut = dto.dateDebut() != null ? dto.dateDebut() : entity.getDateDebut();
        LocalDateTime newFin   = dto.dateFin()   != null ? dto.dateFin()   : entity.getDateFin();

        // Validations temporelles sur le résultat effectif
        validateReservation(dto.vehiculeId(), newDebut, newFin);

        // Mise à jour des champs
        entity.setDateDebut(dto.dateDebut());
        entity.setDateFin(dto.dateFin());
        entity.setVehiculeEntreprise(vehiculeEntrepriseRepo.getReferenceById(dto.vehiculeId()));

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
        return reservationMapper.toDtoList(repo.findByUtilisateurId(Math.toIntExact(user.getId())));
    }

    @Override
    public List<ReservationVehiculeDTO> findByVehiculeId(Long vehiculeId) {
        return reservationMapper.toDtoList(repo.findByVehiculeEntrepriseId(Math.toIntExact(vehiculeId)));
    }

    private void validateReservation(Long vehiculeId, LocalDateTime debut, LocalDateTime fin) {
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

        // Verification que le vehicule est disponible pour ces dates
        List<ReservationVehicule> reservationVehicules = repo.findByVehiculeEntrepriseId(Math.toIntExact(vehiculeId));
        reservationVehicules.forEach(reservation -> {
           if((debut.isAfter(reservation.getDateDebut()) && debut.isBefore(reservation.getDateFin())) || (fin.isAfter(reservation.getDateDebut()) && fin.isBefore(reservation.getDateFin()))) {
               throw new BadRequestException("Le vehicule n'est pas disponible pour ces dates");
           }
        });
        VehiculeEntreprise vehicule = vehiculeEntrepriseRepo.getReferenceById(vehiculeId);
        if (vehicule.getStatut() != StatutVehicule.EN_SERVICE) {
            throw new BadRequestException("Le vehicule n'est pas en service.");
        }
    }
}
