package fr.diginamic.Gestion_des_transports.service.impl;

import fr.diginamic.Gestion_des_transports.dto.ReservationVehiculeDTO;
import fr.diginamic.Gestion_des_transports.mapper.ReservationVehiculeMapper;
import fr.diginamic.Gestion_des_transports.entites.ReservationVehicule;
import fr.diginamic.Gestion_des_transports.repositories.ReservationVehiculeRepository;
import fr.diginamic.Gestion_des_transports.repositories.UtilisateurRepository;
import fr.diginamic.Gestion_des_transports.repositories.VehiculeEntrepriseRepository;
import fr.diginamic.Gestion_des_transports.service.ReservationVehiculeService;
import fr.diginamic.Gestion_des_transports.shared.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
        // MapStruct crée l'entité (sans liaisons)
        ReservationVehicule entity = reservationMapper.toEntity(dto);

        // Lier les associations via références JPA (évite un SELECT)
        entity.setUtilisateur(utilisateurRepo.getReferenceById(dto.utilisateurId()));
        entity.setVehiculeEntreprise(vehiculeEntrepriseRepo.getReferenceById(dto.vehiculeId()));

        // TODO: règles métier (disponibilité, statut EN_SERVICE, mail si lié à covoiturage, etc.)

        ReservationVehicule saved = repo.save(entity);
        return reservationMapper.toDto(saved);
    }

    @Override
    public ReservationVehiculeDTO update(Long id, ReservationVehiculeDTO dto) {
        ReservationVehicule entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable: " + id));

        // Mise à jour des champs
        entity.setDateDebut(dto.dateDebut());
        entity.setDateFin(dto.dateFin());
        entity.setUtilisateur(utilisateurRepo.getReferenceById(dto.utilisateurId()));
        entity.setVehiculeEntreprise(vehiculeEntrepriseRepo.getReferenceById(dto.vehiculeId()));

        // TODO: revalider règles métier (chevauchement, statut véhicule, notifications)

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
}
