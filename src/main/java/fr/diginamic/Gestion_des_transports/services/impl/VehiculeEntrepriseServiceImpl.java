package fr.diginamic.Gestion_des_transports.services.impl;

import fr.diginamic.Gestion_des_transports.dto.VehiculeDTO;
import fr.diginamic.Gestion_des_transports.entites.VehiculePersonnel;
import fr.diginamic.Gestion_des_transports.enums.StatutVehicule;
import fr.diginamic.Gestion_des_transports.mapper.VehiculeMapper;
import fr.diginamic.Gestion_des_transports.entites.VehiculeEntreprise;
import fr.diginamic.Gestion_des_transports.repositories.VehiculeEntrepriseRepository;
import fr.diginamic.Gestion_des_transports.services.VehiculeEntrepriseService;
import fr.diginamic.Gestion_des_transports.shared.BadRequestException;
import fr.diginamic.Gestion_des_transports.shared.ConflictException;
import fr.diginamic.Gestion_des_transports.shared.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class VehiculeEntrepriseServiceImpl implements VehiculeEntrepriseService {

    private final VehiculeEntrepriseRepository repo;
    private final VehiculeMapper vehiculeMapper;

    public VehiculeEntrepriseServiceImpl(VehiculeEntrepriseRepository repo,
                                         VehiculeMapper vehiculeMapper) {
        this.repo = repo;
        this.vehiculeMapper = vehiculeMapper;
    }

    @Override
    public List<VehiculeDTO> findAll() {
        return vehiculeMapper.toDtoEntrepriseList(repo.findAll());
    }

    @Override
    public VehiculeDTO findById(Long id) {
        VehiculeEntreprise ve = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Véhicule d'entreprise introuvable: " + id));
        return vehiculeMapper.toDto(ve);
    }

    @Override
    public VehiculeDTO create(VehiculeDTO dto) {
        if (dto.type() == null || dto.type() != VehiculeDTO.VehiculeType.ENTREPRISE) {
            throw new BadRequestException("Type de véhicule invalide: attendu ENTREPRISE.");
        }
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
