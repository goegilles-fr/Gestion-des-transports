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
/**
 * Implémentation du service de gestion des véhicules d'entreprise (véhicules de service).
 * Gère la logique métier complète du parc automobile :
 * - CRUD des véhicules (création, lecture, modification, suppression)
 * - Vérification de disponibilité sur une période donnée
 * - Validation des données (statut, immatriculation, nombre de places)
 * - Gestion du cycle de vie des véhicules (EN_SERVICE, EN_REPARATION, HORS_SERVICE)
 * - Détection des chevauchements de réservations
 * Applique les règles métier du cahier des charges concernant les véhicules de service.
 * Seuls les administrateurs peuvent créer, modifier ou supprimer des véhicules.
 * Toutes les opérations sont transactionnelles pour garantir la cohérence des données.
 */
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
    /**
     * Récupère tous les véhicules d'entreprise.
     * Convertit les entités en DTOs pour la couche présentation.
     *
     * @return liste de tous les véhicules d'entreprise sous forme de DTOs
     */
    @Override
    public List<VehiculeDTO> findAll() {
        return vehiculeMapper.toDtoEntrepriseList(repo.findAll());
    }
    /**
     * Recherche les véhicules d'entreprise disponibles pour une période donnée.
     * Applique les validations suivantes :
     * - Les dates doivent être futures (postérieures à maintenant)
     * - La date de fin doit être postérieure à la date de début
     * Filtre les véhicules selon ces critères :
     * - Statut EN_SERVICE uniquement
     * - Aucune réservation ne chevauche la période demandée
     * Utilisé pour afficher le carrousel de véhicules lors de la création d'une réservation.
     *
     * @param dateDebut date et heure de début de la période recherchée
     * @param dateFin date et heure de fin de la période recherchée
     * @return liste des véhicules disponibles sous forme de DTOs
     * @throws BadRequestException si les dates sont incohérentes ou passées
     * @throws NotFoundException si aucun véhicule n'est en service ou disponible
     */
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
    /**
     * Récupère un véhicule d'entreprise par son identifiant.
     * Convertit l'entité en DTO pour la couche présentation.
     *
     * @param id l'identifiant unique du véhicule
     * @return le DTO du véhicule
     * @throws NotFoundException si le véhicule n'existe pas
     */
    @Override
    public VehiculeDTO findById(Long id) {
        VehiculeEntreprise ve = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Véhicule d'entreprise introuvable: " + id));
        return vehiculeMapper.toDto(ve);
    }
    /**
     * Crée un nouveau véhicule d'entreprise.
     * Valide les données obligatoires :
     * - Marque, modèle, immatriculation non vides
     * - Statut obligatoire
     * - Nombre de places >= 2
     * - CO2/km >= 0 si fourni
     * - Aucun utilisateurId (spécifique aux véhicules d'entreprise)
     *
     * @param dto les données du véhicule à créer
     * @return le DTO du véhicule créé avec son ID
     * @throws BadRequestException si les données sont invalides
     */
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
    /**
     * Modifie un véhicule d'entreprise existant.
     * Supporte la mise à jour partielle (seuls les champs fournis sont modifiés).
     * Applique les mêmes validations que la création sur les champs fournis.
     * Le changement de statut (EN_REPARATION, HORS_SERVICE) doit déclencher
     * l'annulation des réservations (géré au niveau de la couche service supérieure).
     *
     * @param id l'identifiant du véhicule à modifier
     * @param dto les nouvelles données (champs optionnels pour update partiel)
     * @return le DTO du véhicule modifié
     * @throws NotFoundException si le véhicule n'existe pas
     * @throws BadRequestException si les données fournies sont invalides
     */
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
    /**
     * Supprime un véhicule d'entreprise.
     * La suppression doit déclencher l'annulation de toutes les réservations associées
     * et l'envoi d'emails aux utilisateurs concernés (géré au niveau de la couche service supérieure).
     *
     * @param id l'identifiant du véhicule à supprimer
     * @throws NotFoundException si le véhicule n'existe pas
     */
    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Véhicule d'entreprise introuvable: " + id);
        }
        repo.deleteById(id);
    }
    /**
     * Récupère tous les véhicules d'entreprise ayant un statut spécifique.
     * Statuts possibles : EN_SERVICE, EN_REPARATION, HORS_SERVICE.
     * Utile pour les administrateurs pour filtrer les véhicules nécessitant une intervention.
     *
     * @param statut le statut recherché (chaîne convertie en enum StatutVehicule)
     * @return liste des véhicules ayant ce statut sous forme de DTOs
     */
    @Override
    public List<VehiculeDTO> findByStatut(String statut) {
        return vehiculeMapper.toDtoEntrepriseList(repo.findByStatut(StatutVehicule.valueOf(statut)));
    }
}
