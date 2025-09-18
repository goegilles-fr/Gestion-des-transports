package fr.diginamic.Gestion_des_transports.services.impl;

import fr.diginamic.Gestion_des_transports.dto.VehiculeDTO;
import fr.diginamic.Gestion_des_transports.mapper.VehiculeMapper;
import fr.diginamic.Gestion_des_transports.entites.VehiculePersonnel;
import fr.diginamic.Gestion_des_transports.repositories.UtilisateurRepository;
import fr.diginamic.Gestion_des_transports.repositories.VehiculePersonnelRepository;
import fr.diginamic.Gestion_des_transports.services.VehiculePersonnelService;
import fr.diginamic.Gestion_des_transports.shared.NotFoundException;
import fr.diginamic.Gestion_des_transports.shared.BadRequestException;
import fr.diginamic.Gestion_des_transports.shared.ConflictException;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class VehiculePersonnelServiceImpl implements VehiculePersonnelService {

    private static final Logger log = LogManager.getLogger(VehiculePersonnelServiceImpl.class);
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
    public VehiculeDTO create(Long utilisateurId, VehiculeDTO dto) {
        if (dto.marque() == null || dto.marque().isBlank()){
            throw new BadRequestException("La marque est obligatoire.");
        }
        if (dto.modele() == null || dto.modele().isBlank()){
            throw new BadRequestException("Le modele est obligatoire.");
        }
        if (dto.immatriculation() == null || dto.immatriculation().isBlank()) {
            throw new BadRequestException("L'immatriculation est obligatoire.");
        }
        if (dto.nbPlaces() == null || dto.nbPlaces() < 1) {
            throw new BadRequestException("nombrePlaces doit être >= 1.");
        }
        if (dto.co2ParKm() != null && dto.co2ParKm() < 0) {
            throw new BadRequestException("co2ParKm doit être >= 0.");
        }
        if (repo.existsByUtilisateurId(utilisateurId)) {
            Long existingId = repo.findFirstByUtilisateurId(utilisateurId).map(VehiculePersonnel::getId).orElse(null);
            throw new ConflictException("L'utilisateur " + utilisateurId +
                    " possède déjà un véhicule personnel (id=" + existingId + ").");
        }

        var entity = vehiculeMapper.toPersonnelEntity(dto);
        entity.setUtilisateur(utilisateurRepo.getReferenceById(utilisateurId));
        try {
            var saved = repo.saveAndFlush(entity);
            return vehiculeMapper.toDto(saved);
        } catch (javax.validation.ConstraintViolationException e) {
            e.getConstraintViolations().forEach(v ->
                    log.error("Validation {}.{} : {}",
                            v.getRootBeanClass().getSimpleName(),
                            v.getPropertyPath(),
                            v.getMessage())
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error: " + e.getMessage(), e);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // ex: unique constraint immatriculation
            var root = org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage(e);
            log.error("Data integrity violation: {}", root, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, root, e);
        }
    }

    @Override
    public VehiculeDTO update(Long id, VehiculeDTO dto) {
        VehiculePersonnel entity = repo.findFirstByUtilisateurId(id)
                .orElseThrow(() -> new NotFoundException("Véhicule personnel introuvable: " + id));
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

        if(dto.immatriculation() != null) entity.setImmatriculation(dto.immatriculation());
        if(dto.marque() != null) entity.setMarque(dto.marque());
        if(dto.modele() != null) entity.setModele(dto.modele());
        if(dto.nbPlaces() != null) entity.setNbPlaces(dto.nbPlaces());
        if(dto.co2ParKm() != null) entity.setCo2ParKm(dto.co2ParKm());
        if(dto.photo() != null) entity.setPhoto(dto.photo());
        if(dto.categorie() != null) entity.setCategorie(dto.categorie());
        if(dto.motorisation() != null) entity.setMotorisation(dto.motorisation());

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
    public void deleteByUtilisateurId(Long utilisateurId) {
        VehiculePersonnel entity = repo.findFirstByUtilisateurId(utilisateurId).orElse(null);
        if(entity == null){
            throw new NotFoundException("Véhicule personnel introuvable: " + utilisateurId);
        }

        repo.deleteById(entity.getId());
    }

    @Override
    public List<VehiculeDTO> findByUtilisateurId(Long utilisateurId) {
        return vehiculeMapper.toDtoPersonnelList(repo.findByUtilisateurId(utilisateurId));
    }
}
