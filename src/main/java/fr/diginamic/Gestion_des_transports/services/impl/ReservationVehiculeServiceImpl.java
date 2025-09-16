package fr.diginamic.Gestion_des_transports.services.impl;

import fr.diginamic.Gestion_des_transports.dto.ReservationVehiculeDTO;
import fr.diginamic.Gestion_des_transports.mapper.ReservationVehiculeMapper;
import fr.diginamic.Gestion_des_transports.entites.ReservationVehicule;
import fr.diginamic.Gestion_des_transports.repositories.ReservationVehiculeRepository;
import fr.diginamic.Gestion_des_transports.repositories.UtilisateurRepository;
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
    private final UtilisateurRepository utilisateurRepo;
    private final VehiculeEntrepriseRepository vehiculeEntrepriseRepo;
    private final ReservationVehiculeMapper reservationMapper;

    public ReservationVehiculeServiceImpl(ReservationVehiculeRepository repo,
                                          UtilisateurRepository utilisateurRepo,
                                          VehiculeEntrepriseRepository vehiculeEntrepriseRepo,
                                          ReservationVehiculeMapper reservationMapper) {
        this.repo = repo;
        this.utilisateurRepo = utilisateurRepo;
        this.vehiculeEntrepriseRepo = vehiculeEntrepriseRepo;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public List<ReservationVehiculeDTO> findAll() {
        return reservationMapper.toDtoList(repo.findAll());
    }

    @Override
    public ReservationVehiculeDTO findById(Long id) {
        ReservationVehicule r = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable: " + id));
        return reservationMapper.toDto(r);
    }

    @Override
    public ReservationVehiculeDTO create(ReservationVehiculeDTO dto) {
        if (dto.utilisateurId() == null) {
            throw new BadRequestException("L'utilisateurId est obligatoire.");
        }
        if (dto.vehiculeId() == null) {
            throw new BadRequestException("Le vehiculeId est obligatoire.");
        }
        if (dto.dateDebut() == null || dto.dateFin() == null) {
            throw new BadRequestException("La dateDebut et la dateFin sont obligatoires.");
        }
        validateDates(dto.dateDebut(), dto.dateFin());

        ReservationVehicule entity = reservationMapper.toEntity(dto);

        // Lier les associations via références JPA (évite un SELECT)
        entity.setUtilisateur(utilisateurRepo.getReferenceById(dto.utilisateurId()));
        entity.setVehiculeEntreprise(vehiculeEntrepriseRepo.getReferenceById(dto.vehiculeId()));

        ReservationVehicule saved = repo.save(entity);
        return reservationMapper.toDto(saved);
    }

    @Override
    public ReservationVehiculeDTO update(Long id, ReservationVehiculeDTO dto) {
        ReservationVehicule entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable: " + id));

        // On calcule les nouvelles valeurs effectives (permet l’update partiel)
        LocalDateTime newDebut = dto.dateDebut() != null ? dto.dateDebut() : entity.getDateDebut();
        LocalDateTime newFin   = dto.dateFin()   != null ? dto.dateFin()   : entity.getDateFin();

        // Validations temporelles sur le résultat effectif
        validateDates(newDebut, newFin);

        // Mise à jour des champs
        entity.setDateDebut(dto.dateDebut());
        entity.setDateFin(dto.dateFin());
        entity.setUtilisateur(utilisateurRepo.getReferenceById(dto.utilisateurId()));
        entity.setVehiculeEntreprise(vehiculeEntrepriseRepo.getReferenceById(dto.vehiculeId()));

        return reservationMapper.toDto(entity);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Réservation introuvable: " + id);
        }
        repo.deleteById(id);
    }

    @Override
    public List<ReservationVehiculeDTO> findByUtilisateurId(Long utilisateurId) {
        return reservationMapper.toDtoList(repo.findByUtilisateurId(Math.toIntExact(utilisateurId)));
    }

    @Override
    public List<ReservationVehiculeDTO> findByVehiculeId(Long vehiculeId) {
        return reservationMapper.toDtoList(repo.findByVehiculeEntrepriseId(Math.toIntExact(vehiculeId)));
    }

    private void validateDates(LocalDateTime debut, LocalDateTime fin) {
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
    }
}
