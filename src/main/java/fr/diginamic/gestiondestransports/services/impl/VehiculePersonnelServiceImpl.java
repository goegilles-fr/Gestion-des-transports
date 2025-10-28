package fr.diginamic.gestiondestransports.services.impl;

import fr.diginamic.gestiondestransports.dto.VehiculeDTO;
import fr.diginamic.gestiondestransports.mapper.VehiculeMapper;
import fr.diginamic.gestiondestransports.entites.VehiculePersonnel;
import fr.diginamic.gestiondestransports.repositories.UtilisateurRepository;
import fr.diginamic.gestiondestransports.repositories.VehiculePersonnelRepository;
import fr.diginamic.gestiondestransports.services.VehiculePersonnelService;
import fr.diginamic.gestiondestransports.shared.NotFoundException;
import fr.diginamic.gestiondestransports.shared.BadRequestException;
import fr.diginamic.gestiondestransports.shared.ConflictException;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
/**
 * Implémentation du service de gestion des véhicules personnels des collaborateurs.
 * Gère la logique métier complète des véhicules personnels :
 * - CRUD des véhicules (création, lecture, modification, suppression)
 * - Validation des données (marque, modèle, immatriculation, places, CO2)
 * - Application de la règle métier : un seul véhicule personnel par utilisateur
 * - Gestion des contraintes d'unicité (immatriculation unique dans le système)
 * Les véhicules personnels peuvent être utilisés pour créer des annonces de covoiturage.
 * Applique les règles métier du cahier des charges concernant les véhicules personnels.
 * Toutes les opérations sont transactionnelles pour garantir la cohérence des données.
 */
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
    /**
     * Récupère tous les véhicules personnels du système.
     * Convertit les entités en DTOs pour la couche présentation.
     * Utile pour les administrateurs pour consulter l'ensemble du parc.
     *
     * @return liste de tous les véhicules personnels sous forme de DTOs
     */
    @Override
    public List<VehiculeDTO> findAll() {
        return vehiculeMapper.toDtoPersonnelList(repo.findAll());
    }
    /**
     * Récupère un véhicule personnel par son identifiant.
     * Convertit l'entité en DTO pour la couche présentation.
     *
     * @param id l'identifiant unique du véhicule personnel
     * @return le DTO du véhicule
     * @throws NotFoundException si le véhicule n'existe pas
     */
    @Override
    public VehiculeDTO findById(Long id) {
        VehiculePersonnel vp = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Véhicule personnel introuvable: " + id));
        return vehiculeMapper.toDto(vp);
    }
    /**
     * Crée un nouveau véhicule personnel pour un utilisateur.
     * Applique les règles métier :
     * - Un utilisateur ne peut avoir qu'un seul véhicule personnel
     * - L'immatriculation doit être unique dans tout le système
     * Valide les données obligatoires :
     * - Marque, modèle, immatriculation non vides
     * - Nombre de places >= 1
     * - CO2/km >= 0 si fourni
     * Gère les violations de contraintes d'intégrité (unicité immatriculation).
     *
     * @param utilisateurId l'identifiant de l'utilisateur propriétaire
     * @param dto les données du véhicule à créer
     * @return le DTO du véhicule créé avec son ID
     * @throws BadRequestException si les données sont invalides
     * @throws ConflictException si l'utilisateur possède déjà un véhicule
     * @throws ResponseStatusException si violation de contrainte d'intégrité (immatriculation en doublon)
     */
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
    /**
     * Modifie le véhicule personnel d'un utilisateur.
     * Recherche le véhicule par l'identifiant de l'utilisateur (un seul véhicule par utilisateur).
     * Supporte la mise à jour partielle (seuls les champs fournis sont modifiés).
     * Applique les mêmes validations que la création sur les champs fournis.
     * Le statut n'existe pas pour les véhicules personnels (toujours disponibles).
     *
     * @param id l'identifiant de l'utilisateur propriétaire
     * @param dto les nouvelles données (champs optionnels pour update partiel)
     * @return le DTO du véhicule modifié
     * @throws NotFoundException si l'utilisateur n'a pas de véhicule
     * @throws BadRequestException si les données fournies sont invalides
     */
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
    /**
     * Supprime un véhicule personnel par son identifiant.
     * La suppression doit être refusée si le véhicule est utilisé dans des annonces
     * de covoiturage actives ou futures (validation effectuée au niveau contrôleur).
     *
     * @param id l'identifiant du véhicule à supprimer
     * @throws NotFoundException si le véhicule n'existe pas
     */
    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Véhicule personnel introuvable: " + id);
        }
        repo.deleteById(id);
    }
    /**
     * Supprime le véhicule personnel d'un utilisateur.
     * Recherche d'abord le véhicule de l'utilisateur puis le supprime.
     * Alternative à delete() utilisant l'identifiant utilisateur au lieu de l'ID véhicule.
     *
     * @param utilisateurId l'identifiant de l'utilisateur dont on veut supprimer le véhicule
     * @throws NotFoundException si l'utilisateur n'a pas de véhicule
     */
    @Override
    public void deleteByUtilisateurId(Long utilisateurId) {
        VehiculePersonnel entity = repo.findFirstByUtilisateurId(utilisateurId).orElse(null);
        if(entity == null){
            throw new NotFoundException("Véhicule personnel introuvable: " + utilisateurId);
        }

        repo.deleteById(entity.getId());
    }
    /**
     * Récupère les véhicules personnels d'un utilisateur (maximum 1 selon règle métier).
     * Retourne une liste avec 0 ou 1 élément.
     * Convertit les entités en DTOs pour la couche présentation.
     *
     * @param utilisateurId l'identifiant de l'utilisateur
     * @return liste contenant le véhicule de l'utilisateur ou liste vide
     */
    @Override
    public List<VehiculeDTO> findByUtilisateurId(Long utilisateurId) {
        return vehiculeMapper.toDtoPersonnelList(repo.findByUtilisateurId(utilisateurId));
    }
}
