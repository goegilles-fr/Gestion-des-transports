package fr.diginamic.Gestion_des_transports.service.impl;

import fr.diginamic.Gestion_des_transports.dto.VehiculeDTO;
import fr.diginamic.Gestion_des_transports.mapper.VehiculeMapper;
import fr.diginamic.Gestion_des_transports.entites.VehiculePersonnel;
import fr.diginamic.Gestion_des_transports.repositories.UtilisateurRepository;
import fr.diginamic.Gestion_des_transports.repositories.VehiculePersonnelRepository;
import fr.diginamic.Gestion_des_transports.service.VehiculePersonnelService;
import fr.diginamic.Gestion_des_transports.shared.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class VehiculePersonnelServiceImpl implements VehiculePersonnelService {

    private final VehiculePersonnelRepository repo;
    private final UtilisateurRepository utilisateurRepo;
    private final VehiculeMapper vehiculeMapper;

    public VehiculePersonnelServiceImpl(VehiculePersonnelRepository repo,
                                        UtilisateurRepository utilisateurRepo,
                                        VehiculeMapper vehiculeMapper) {
        this.repo = repo;
        this.utilisateurRepo = utilisateurRepo;
        this.vehiculeMapper = vehiculeMapper;
    }

    @Override
    public List<VehiculeDTO> findAll() {
        return vehiculeMapper.toDtoPersonnelList(repo.findAll());
    }

    @Override
    public VehiculeDTO findById(Long id) {
        VehiculePersonnel vp = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Véhicule personnel introuvable: " + id));
        return vehiculeMapper.toDto(vp);
    }

    @Override
    public VehiculeDTO create(VehiculeDTO dto) {
        VehiculePersonnel entity = vehiculeMapper.toPersonnelEntity(dto);
        // Lien vers l'utilisateur (association)
        if (dto.utilisateurId() != null) {
            entity.setUtilisateur(utilisateurRepo.getReferenceById(dto.utilisateurId()));
        }
        VehiculePersonnel saved = repo.save(entity);
        return vehiculeMapper.toDto(saved);
    }

    @Override
    public VehiculeDTO update(Long id, VehiculeDTO dto) {
        VehiculePersonnel entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Véhicule personnel introuvable: " + id));

        // Mise à jour des champs simples (MapStruct n'a pas de méthode @MappingTarget ici)
        entity.setImmatriculation(dto.immatriculation());
        entity.setMarque(dto.marque());
        entity.setModele(dto.modele());
        entity.setNbPlaces(dto.nombrePlaces());

        if (dto.utilisateurId() != null) {
            entity.setUtilisateur(utilisateurRepo.getReferenceById(dto.utilisateurId()));
        }

        return vehiculeMapper.toDto(entity);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Véhicule personnel introuvable: " + id);
        }
        repo.deleteById(id);
    }

    @Override
    public List<VehiculeDTO> findByUtilisateurId(Long utilisateurId) {
        return vehiculeMapper.toDtoPersonnelList(repo.findByUtilisateurId(utilisateurId));
    }
}
