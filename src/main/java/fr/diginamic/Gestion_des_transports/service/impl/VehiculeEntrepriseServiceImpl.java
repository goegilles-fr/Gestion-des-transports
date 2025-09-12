package fr.diginamic.Gestion_des_transports.service.impl;

import fr.diginamic.Gestion_des_transports.dto.VehiculeDTO;
import fr.diginamic.Gestion_des_transports.mapper.VehiculeMapper;
import fr.diginamic.Gestion_des_transports.entites.VehiculeEntreprise;
import fr.diginamic.Gestion_des_transports.repositories.VehiculeEntrepriseRepository;
import fr.diginamic.Gestion_des_transports.service.VehiculeEntrepriseService;
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
        VehiculeEntreprise entity = vehiculeMapper.toEntrepriseEntity(dto);
        VehiculeEntreprise saved = repo.save(entity);
        return vehiculeMapper.toDto(saved);
    }

    @Override
    public VehiculeDTO update(Long id, VehiculeDTO dto) {
        VehiculeEntreprise entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Véhicule d'entreprise introuvable: " + id));

        // Mise à jour des champs simples (on réutilise la logique de conversion du mapper pour le statut)
        entity.setImmatriculation(dto.immatriculation());
        entity.setMarque(dto.marque());
        entity.setModele(dto.modele());
        entity.setNbPlaces(dto.nombrePlaces());
        entity.setStatut(dto.statut());
        entity.setMotorisation(dto.motorisation());
        entity.setCo2ParKm(dto.co2ParKm());
        entity.setPhoto(dto.photoUrl());
        entity.setCategorie(dto.categorie());
        // Les réservations sont gérées via un service dédié, pas ici.

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
        return vehiculeMapper.toDtoEntrepriseList(repo.findByStatut(statut));
    }
}
