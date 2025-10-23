package fr.diginamic.gestiondestransports.services.impl;

import fr.diginamic.gestiondestransports.dto.VehiculeDTO;
import fr.diginamic.gestiondestransports.entites.ReservationVehicule;
import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import fr.diginamic.gestiondestransports.mapper.VehiculeMapper;
import fr.diginamic.gestiondestransports.entites.VehiculeEntreprise;
import fr.diginamic.gestiondestransports.repositories.ReservationVehiculeRepository;
import fr.diginamic.gestiondestransports.repositories.VehiculeEntrepriseRepository;
import fr.diginamic.gestiondestransports.services.VehiculeEntrepriseService;
import fr.diginamic.gestiondestransports.shared.BadRequestException;
import fr.diginamic.gestiondestransports.shared.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class VehiculeEntrepriseServiceImpl implements VehiculeEntrepriseService {

    private final VehiculeEntrepriseRepository repo;
    private final ReservationVehiculeRepository repoReservations;
    private final VehiculeMapper vehiculeMapper;

    public VehiculeEntrepriseServiceImpl(VehiculeEntrepriseRepository repo,
                                         ReservationVehiculeRepository repoReservations,
                                         VehiculeMapper vehiculeMapper) {
        this.repo = repo;
        this.vehiculeMapper = vehiculeMapper;
        this.repoReservations = repoReservations;

    }

    @Override
    public List<VehiculeDTO> findAll() {
        return vehiculeMapper.toDtoEntrepriseList(repo.findAll());
    }

    public List<VehiculeDTO> findByAvailability(LocalDateTime dateDebut, LocalDateTime dateFin) {
        // Check if dateFin is after dateDebut
        if (dateFin.isBefore(dateDebut) || dateFin.isEqual(dateDebut)) {
            throw new BadRequestException("DATES INCORRECTES : La date de début doit être antérieure à la date de fin");
        }

        LocalDateTime now = LocalDateTime.now();


        if (dateDebut.isBefore(now)) {
            throw new BadRequestException("DATES INCORRECTES : La date de début doit être dans le futur");
        }
        if (dateFin.isBefore(now)) {
            throw new BadRequestException("DATES INCORRECTES : La date de fin doit être dans le futur");
        }
        // Get all vehicles with status EN_SERVICE
        List<VehiculeEntreprise> vehiculesEnService = repo.findByStatut(StatutVehicule.EN_SERVICE);

        if (vehiculesEnService.isEmpty()) {
            throw new NotFoundException("Il n'y a pas de voitures en service");
        }
        List<VehiculeEntreprise> vehiculesDisponibles = new ArrayList<>();

        // Loop through all company cars
        for (VehiculeEntreprise vehicule : vehiculesEnService) {
            List<ReservationVehicule> reservations = repoReservations.findByVehiculeEntrepriseId(vehicule.getId());



            boolean isAvailable = true;

            // Check if the selected period intersects with any existing reservations
            for (ReservationVehicule reservation : reservations) {
                LocalDateTime reservationDebut = reservation.getDateDebut();
                LocalDateTime reservationFin = reservation.getDateFin();


                // Check for real overlap (excluding boundary touches)
                if (((!dateDebut.isBefore(reservationDebut) && !dateDebut.isAfter(reservationFin)) ||
                        (!dateFin.isBefore(reservationDebut) && !dateFin.isAfter(reservationFin)) ||
                        (!dateDebut.isAfter(reservationDebut) && !dateFin.isBefore(reservationFin))) &&
                        !(dateFin.isEqual(reservationDebut) || dateDebut.isEqual(reservationFin))) {

                    isAvailable = false;
                    break;
                }
            }

            // If no conflicts found, add the vehicle to available list
            if (isAvailable) {

                vehiculesDisponibles.add(vehicule);
            }
        }

        if (vehiculesDisponibles.isEmpty()) {
            throw new  NotFoundException("Aucune voiture disponible pour les dates sélectionnées");
        }


        return vehiculeMapper.toDtoEntrepriseList(vehiculesDisponibles);
    }

    @Override
    public VehiculeDTO findById(Long id) {
        VehiculeEntreprise ve = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Véhicule d'entreprise introuvable: " + id));
        return vehiculeMapper.toDto(ve);
    }

    @Override
    public VehiculeDTO create(VehiculeDTO dto) {
        if (dto.marque() == null || dto.marque().isBlank()){
            throw new BadRequestException("La marque est obligatoire.");
        }
        if (dto.modele() == null || dto.modele().isBlank()){
            throw new BadRequestException("Le modele est obligatoire.");
        }
        if (dto.immatriculation() == null || dto.immatriculation().isBlank()) {
            throw new BadRequestException("L'immatriculation est obligatoire.");
        }
        if (dto.statut() == null) {
            throw new BadRequestException("Le statut est obligatoire pour enregistrer un véhicule d'entreprise.");
        }
        if (dto.nbPlaces() == null || dto.nbPlaces() < 1) {
            throw new BadRequestException("nombrePlaces doit être >= 1.");
        }
        if (dto.co2ParKm() != null && dto.co2ParKm() < 0) {
            throw new BadRequestException("co2ParKm doit être >= 0.");
        }
        if (dto.utilisateurId() != null) {
            throw new BadRequestException("L'utilisateur ne doit pas etre renseigné pour un Vehicule d'entreprise.");
        }

        VehiculeEntreprise entity = vehiculeMapper.toEntrepriseEntity(dto);
        VehiculeEntreprise saved = repo.save(entity);
        return vehiculeMapper.toDto(saved);
    }

    @Override
    public VehiculeDTO update(Long id, VehiculeDTO dto) {
        VehiculeEntreprise entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Véhicule d'entreprise introuvable: " + id));
        if (dto.marque() != null && dto.marque().isBlank()){
            throw new BadRequestException("La marque est obligatoire.");
        }
        if (dto.modele() != null && dto.modele().isBlank()){
            throw new BadRequestException("Le modele est obligatoire.");
        }
        if (dto.immatriculation() != null && dto.immatriculation().isBlank()) {
            throw new BadRequestException("L'immatriculation est obligatoire.");
        }
        if (dto.nbPlaces() != null && dto.nbPlaces() < 1) {
            throw new BadRequestException("nombrePlaces doit être >= 1.");
        }
        if (dto.co2ParKm() != null && dto.co2ParKm() < 0) {
            throw new BadRequestException("co2ParKm doit être >= 0.");
        }
        if (dto.utilisateurId() != null) {
            throw new BadRequestException("L'utilisateur ne doit pas etre renseigné pour un Vehicule d'entreprise.");
        }

        if(dto.immatriculation() != null) entity.setImmatriculation(dto.immatriculation());
        if(dto.marque() != null) entity.setMarque(dto.marque());
        if(dto.modele() != null) entity.setModele(dto.modele());
        if(dto.nbPlaces() != null) entity.setNbPlaces(dto.nbPlaces());
        if(dto.statut() != null) entity.setStatut(dto.statut());
        if(dto.co2ParKm() != null) entity.setCo2ParKm(dto.co2ParKm());
        if(dto.photo() != null) entity.setPhoto(dto.photo());
        if(dto.categorie() != null) entity.setCategorie(dto.categorie());
        if(dto.motorisation() != null) entity.setMotorisation(dto.motorisation());

        return vehiculeMapper.toDto(entity);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Véhicule d'entreprise introuvable: " + id);
        }
        repo.deleteById(id);
    }

    @Override
    public List<VehiculeDTO> findByStatut(String statut) {
        return vehiculeMapper.toDtoEntrepriseList(repo.findByStatut(StatutVehicule.valueOf(statut)));
    }
}
