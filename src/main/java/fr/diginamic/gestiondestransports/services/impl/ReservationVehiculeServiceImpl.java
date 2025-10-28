package fr.diginamic.gestiondestransports.services.impl;

import fr.diginamic.gestiondestransports.dto.ReservationVehiculeDTO;
import fr.diginamic.gestiondestransports.entites.AnnonceCovoiturage;
import fr.diginamic.gestiondestransports.entites.Utilisateur;
import fr.diginamic.gestiondestransports.entites.VehiculeEntreprise;
import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import fr.diginamic.gestiondestransports.mapper.ReservationVehiculeMapper;
import fr.diginamic.gestiondestransports.entites.ReservationVehicule;
import fr.diginamic.gestiondestransports.repositories.AnnonceCovoiturageRepository;
import fr.diginamic.gestiondestransports.repositories.ReservationVehiculeRepository;
import fr.diginamic.gestiondestransports.repositories.VehiculeEntrepriseRepository;
import fr.diginamic.gestiondestransports.services.ReservationVehiculeService;
import fr.diginamic.gestiondestransports.shared.BadRequestException;
import fr.diginamic.gestiondestransports.shared.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Implémentation du service de gestion des réservations de véhicules d'entreprise.
 * Gère la logique métier complète : création, modification, suppression, consultation des réservations.
 * Applique les règles de validation : disponibilité des véhicules, chevauchement de périodes, statut du véhicule.
 * Vérifie les conflits avec les annonces de covoiturage lors de la suppression.
 * Un utilisateur ne peut avoir qu'une seule réservation active à la fois (pas de chevauchement).
 * Conforme aux règles métier du cahier des charges concernant les réservations de véhicules de service.
 */
@Service
@Transactional
public class ReservationVehiculeServiceImpl implements ReservationVehiculeService {

    private final ReservationVehiculeRepository repo;
    private final VehiculeEntrepriseRepository vehiculeEntrepriseRepo;
    private final AnnonceCovoiturageRepository annonceCovoiturageRepo;
    private final ReservationVehiculeMapper reservationMapper;

    public ReservationVehiculeServiceImpl(ReservationVehiculeRepository repo,
                                          VehiculeEntrepriseRepository vehiculeEntrepriseRepo,
                                          AnnonceCovoiturageRepository annonceCovoiturageRepo,
                                          ReservationVehiculeMapper reservationMapper) {
        this.repo = repo;
        this.vehiculeEntrepriseRepo = vehiculeEntrepriseRepo;
        this.annonceCovoiturageRepo = annonceCovoiturageRepo;
        this.reservationMapper = reservationMapper;
    }
    /**
     * Récupère toutes les réservations de véhicules existantes.
     * Convertit les entités en DTOs pour la couche présentation.
     *
     * @return liste de toutes les réservations sous forme de DTOs
     */
    @Override
    public List<ReservationVehiculeDTO> findAll() {
        return reservationMapper.toDtoList(repo.findAll());
    }
    /**
     * Récupère une réservation spécifique par son identifiant.
     * Vérifie que l'utilisateur demandeur est bien le propriétaire de la réservation.
     * Les administrateurs peuvent consulter toutes les réservations.
     *
     * @param user l'utilisateur authentifié effectuant la demande
     * @param id l'identifiant de la réservation
     * @return le DTO de la réservation
     * @throws NotFoundException si la réservation n'existe pas
     * @throws BadRequestException si l'utilisateur n'est pas le propriétaire
     */
    @Override
    public ReservationVehiculeDTO findById(Utilisateur user, Long id) {
        ReservationVehicule entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable: " + id));
        if (!entity.getUtilisateur().getId().equals(user.getId())) {
            throw new BadRequestException("L'utilisateur ne correspond pas.");
        }
        return reservationMapper.toDto(entity);
    }
    /**
     * Crée une nouvelle réservation de véhicule d'entreprise.
     * Applique les règles de validation :
     * - Le véhicule doit être EN_SERVICE
     * - Les dates doivent être futures et cohérentes (début < fin)
     * - Le véhicule doit être disponible sur la période (pas de chevauchement)
     * - L'utilisateur ne doit pas avoir d'autre réservation sur cette période
     *
     * @param user l'utilisateur effectuant la réservation
     * @param dto les données de la réservation (vehiculeId, dateDebut, dateFin)
     * @return le DTO de la réservation créée avec son ID
     * @throws BadRequestException si les données sont invalides ou si le véhicule n'est pas disponible
     * @throws NotFoundException si le véhicule n'existe pas
     */
    @Override
    public ReservationVehiculeDTO create(Utilisateur user, ReservationVehiculeDTO dto) {
        if (dto.vehiculeId() == null) {
            throw new BadRequestException("Le vehiculeId est obligatoire.");
        }
        if (dto.dateDebut() == null || dto.dateFin() == null) {
            throw new BadRequestException("La dateDebut et la dateFin sont obligatoires.");
        }
        validateReservation(dto.vehiculeId(), dto.dateDebut(), dto.dateFin(), null);
        validateUser(dto.dateDebut(), dto.dateFin(), user.getId(), null);
        ReservationVehicule entity = reservationMapper.toEntity(dto);

        entity.setUtilisateur(user);
        entity.setVehiculeEntreprise(vehiculeEntrepriseRepo.getReferenceById(dto.vehiculeId()));

        ReservationVehicule saved = repo.save(entity);
        return reservationMapper.toDto(saved);
    }
    /**
     * Modifie une réservation existante.
     * Supporte la mise à jour partielle (seuls les champs fournis sont modifiés).
     * Vérifie que l'utilisateur est le propriétaire de la réservation.
     * Réapplique toutes les validations sur les nouvelles valeurs effectives.
     * Si le véhicule est modifié ou les dates changent, vérifie la disponibilité.
     *
     * @param user l'utilisateur effectuant la modification
     * @param id l'identifiant de la réservation à modifier
     * @param dto les nouvelles données (champs optionnels pour update partiel)
     * @return le DTO de la réservation modifiée
     * @throws NotFoundException si la réservation n'existe pas
     * @throws BadRequestException si l'utilisateur n'est pas propriétaire ou si les nouvelles données sont invalides
     */
    @Override
    public ReservationVehiculeDTO update(Utilisateur user, Long id, ReservationVehiculeDTO dto) {
        ReservationVehicule entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable: " + id));
        if (!entity.getUtilisateur().getId().equals(user.getId())) {
            throw new BadRequestException("L'utilisateur ne correspond pas.");
        }

        // On calcule les nouvelles valeurs effectives (permet l'update partiel)
        LocalDateTime newDebut = dto.dateDebut() != null ? dto.dateDebut() : entity.getDateDebut();
        LocalDateTime newFin = dto.dateFin() != null ? dto.dateFin() : entity.getDateFin();
        Long newVehiculeId = dto.vehiculeId() != null ? dto.vehiculeId() : entity.getVehiculeEntreprise().getId();

        // Validations temporelles sur le résultat effectif
        validateReservation(newVehiculeId, newDebut, newFin, id);
        validateUser(newDebut, newFin, user.getId(), id);
        // Mise à jour des champs SEULEMENT si fournis
        if (dto.dateDebut() != null) {
            entity.setDateDebut(dto.dateDebut());
        }
        if (dto.dateFin() != null) {
            entity.setDateFin(dto.dateFin());
        }
        if (dto.vehiculeId() != null) {
            entity.setVehiculeEntreprise(vehiculeEntrepriseRepo.getReferenceById(dto.vehiculeId()));
        }

        return reservationMapper.toDto(entity);
    }

    /**
     * Recherche une réservation de véhicule couvrant une période spécifique pour un utilisateur.
     * La réservation doit commencer avant ou au moment du début recherché
     * et finir après ou au moment de la fin recherchée (englobe complètement la période).
     * Utilisé pour valider qu'un utilisateur a bien réservé un véhicule pour créer une annonce de covoiturage.
     *
     * @param user l'utilisateur authentifié
     * @param dateDebut date et heure de début de la période recherchée
     * @param dureeMinutes durée en minutes de la période
     * @return le DTO de la réservation couvrant cette période
     * @throws BadRequestException si les paramètres sont invalides
     * @throws NotFoundException si aucune réservation ne couvre cette période
     */
    @Override
    public ReservationVehiculeDTO findByUtilisateurAndPeriode(Utilisateur user, LocalDateTime dateDebut, Integer dureeMinutes) {
        // Validation des paramètres
        if (dateDebut == null) {
            throw new BadRequestException("La dateDebut est obligatoire.");
        }
        if (dureeMinutes == null) {
            throw new BadRequestException("La dureeMinutes est obligatoire.");
        }
        if (dureeMinutes <= 0) {
            throw new BadRequestException("La dureeMinutes doit être strictement positive.");
        }

        // Calcul de la date de fin recherchée
        LocalDateTime dateFinRecherche = dateDebut.plusMinutes(dureeMinutes);

        // Recherche de la réservation couvrant cette période
        ReservationVehicule entity = repo.findByUtilisateurIdAndPeriodeCouvrante(
                user.getId(),
                dateDebut,
                dateFinRecherche
        ).orElseThrow(() -> new NotFoundException(
                "Aucune réservation trouvée pour la période du " + dateDebut +
                        " au " + dateFinRecherche
        ));

        return reservationMapper.toDto(entity);
    }

    /**
     * Supprime une réservation de véhicule.
     * Vérifie que l'utilisateur est le propriétaire.
     * Refuse la suppression si le véhicule est utilisé dans des annonces de covoiturage
     * dont la période chevauche celle de la réservation.
     * Fournit un message d'erreur détaillé listant les covoiturages en conflit.
     *
     * @param user l'utilisateur effectuant la suppression
     * @param id l'identifiant de la réservation à supprimer
     * @throws NotFoundException si la réservation n'existe pas
     * @throws BadRequestException si l'utilisateur n'est pas propriétaire ou si des covoiturages utilisent ce véhicule
     */
    @Override
    public void delete(Utilisateur user, Long id) {
        // Récupération de la réservation
        ReservationVehicule entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable: " + id));

        // Vérification que l'utilisateur est bien le propriétaire de la réservation
        if (!entity.getUtilisateur().getId().equals(user.getId())) {
            throw new BadRequestException("L'utilisateur ne correspond pas.");
        }

        // Vérification qu'aucun covoiturage n'utilise ce véhicule pendant cette période
        if (entity.getVehiculeEntreprise() != null) {
            Long vehiculeServiceId = entity.getVehiculeEntreprise().getId();
            LocalDateTime dateDebutReservation = entity.getDateDebut();
            LocalDateTime dateFinReservation = entity.getDateFin();

            // Recherche des annonces de covoiturage qui pourraient chevaucher cette période
            // On cherche toutes les annonces qui démarrent avant la fin de la réservation
            List<AnnonceCovoiturage> annoncesAVerifier =
                    annonceCovoiturageRepo.findByVehiculeServiceIdBetweenDates(
                            vehiculeServiceId,
                            dateDebutReservation,
                            dateFinReservation
                    );

            // Filtrage manuel pour vérifier les chevauchements réels
            List<AnnonceCovoiturage> annoncesConflictuelles = annoncesAVerifier.stream()
                    .filter(annonce -> {
                        LocalDateTime heureDebutCovoiturage = annonce.getHeureDepart();
                        LocalDateTime heureFinCovoiturage = heureDebutCovoiturage
                                .plusMinutes(annonce.getDureeTrajet());

                        // Vérification de chevauchement : deux périodes se chevauchent si :
                        // - le covoiturage commence avant la fin de la réservation ET
                        // - le covoiturage finit après le début de la réservation
                        return heureDebutCovoiturage.isBefore(dateFinReservation)
                                && heureFinCovoiturage.isAfter(dateDebutReservation);
                    })
                    .toList();

            // Si des annonces utilisent ce véhicule pendant cette période, on refuse la suppression
            if (!annoncesConflictuelles.isEmpty()) {
                StringBuilder message = new StringBuilder(
                        "Impossible de supprimer cette réservation. Le véhicule est utilisé dans " +
                                annoncesConflictuelles.size() + " annonce(s) de covoiturage pendant cette période :\n"
                );

                // Construction d'un message détaillé avec les annonces en conflit
                for (AnnonceCovoiturage annonce : annoncesConflictuelles) {
                    LocalDateTime heureFinCovoiturage = annonce.getHeureDepart()
                            .plusMinutes(annonce.getDureeTrajet());
                    message.append("- Covoiturage #")
                            .append(annonce.getId())
                            .append(" : du ")
                            .append(annonce.getHeureDepart())
                            .append(" au ")
                            .append(heureFinCovoiturage)
                            .append("\n");
                }

                throw new BadRequestException(message.toString());
            }
        }

        // Si toutes les validations passent, on supprime la réservation
        repo.deleteById(id);
    }
    /**
     * Récupère toutes les réservations d'un utilisateur spécifique.
     * Inclut les réservations passées, en cours et futures.
     *
     * @param user l'utilisateur dont on veut les réservations
     * @return liste des réservations de l'utilisateur sous forme de DTOs
     */
    @Override
    public List<ReservationVehiculeDTO> findByUtilisateurId(Utilisateur user) {
        return reservationMapper.toDtoList(repo.findByUtilisateurId(user.getId()));
    }
    /**
     * Récupère toutes les réservations associées à un véhicule spécifique.
     * Utile pour les administrateurs pour consulter l'historique d'utilisation d'un véhicule.
     * Affiche dans la page de modification du véhicule.
     *
     * @param vehiculeId l'identifiant du véhicule d'entreprise
     * @return liste des réservations de ce véhicule sous forme de DTOs
     */
    @Override
    public List<ReservationVehiculeDTO> findByVehiculeId(Long vehiculeId) {
        return reservationMapper.toDtoList(repo.findByVehiculeEntrepriseId(vehiculeId));
    }
    /**
     * Valide les contraintes d'une réservation de véhicule.
     * Vérifie :
     * - Les dates sont futures et cohérentes
     * - Le véhicule existe et est EN_SERVICE
     * - Le véhicule est disponible (pas de chevauchement avec d'autres réservations)
     * Exclut une réservation spécifique de la vérification (pour les mises à jour).
     *
     * @param vehiculeId l'identifiant du véhicule à réserver
     * @param debut date et heure de début de la réservation
     * @param fin date et heure de fin de la réservation
     * @param reservationIdAExclure identifiant de la réservation à exclure (null pour création)
     * @throws BadRequestException si une des validations échoue
     * @throws NotFoundException si le véhicule n'existe pas
     */
    private void validateReservation(Long vehiculeId, LocalDateTime debut, LocalDateTime fin, Long reservationIdAExclure) {
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

        VehiculeEntreprise vehicule = vehiculeEntrepriseRepo.findById(vehiculeId)
                .orElseThrow(() -> new NotFoundException("Véhicule introuvable: " + vehiculeId));

        if (vehicule.getStatut() != StatutVehicule.EN_SERVICE) {
            throw new BadRequestException("Le véhicule n'est pas en service. Statut actuel: " + vehicule.getStatut());
        }


        List<ReservationVehicule> reservationsExistantes = repo.findByVehiculeEntrepriseId(vehiculeId);
        // Verification que le vehicule est disponible pour ces dates
        for (ReservationVehicule reservationExistante : reservationsExistantes) {
            LocalDateTime debutExistant = reservationExistante.getDateDebut();
            LocalDateTime finExistante = reservationExistante.getDateFin();

            if (reservationExistante.getId().equals(reservationIdAExclure)) {
                continue;
            }

            // Vérification de chevauchement : deux périodes se chevauchent si :
            // - la nouvelle période commence avant la fin de l'existante ET
            // - la nouvelle période finit après le début de l'existante
            if (debut.isBefore(finExistante) && fin.isAfter(debutExistant)) {
                throw new BadRequestException("Le véhicule n'est pas disponible pour ces dates. " +
                        "Conflit avec une réservation existante du " + debutExistant + " au " + finExistante);
            }
        }
    }
    /**
     * Valide qu'un utilisateur n'a pas de chevauchement avec ses propres réservations.
     * Règle métier : un utilisateur ne peut avoir qu'une seule réservation active à la fois.
     * Exclut une réservation spécifique de la vérification (pour les mises à jour).
     *
     * @param debut date et heure de début de la nouvelle réservation
     * @param fin date et heure de fin de la nouvelle réservation
     * @param userId l'identifiant de l'utilisateur
     * @param reservationIdAExclure identifiant de la réservation à exclure (null pour création)
     * @throws BadRequestException si un chevauchement est détecté avec une autre réservation de l'utilisateur
     */
    private void validateUser(LocalDateTime debut, LocalDateTime fin, Long userId, Long reservationIdAExclure) {
        // Vérification que l'utilisateur n'a pas d'autre réservation en même temps
        List<ReservationVehicule> reservationsUtilisateur = repo.findByUtilisateurId(userId);
        for (ReservationVehicule reservationUtilisateur : reservationsUtilisateur) {
            // Exclure la réservation courante si c'est une mise à jour
            if (reservationIdAExclure != null && reservationUtilisateur.getId().equals(reservationIdAExclure)) {
                continue;
            }

            LocalDateTime debutUtilisateur = reservationUtilisateur.getDateDebut();
            LocalDateTime finUtilisateur = reservationUtilisateur.getDateFin();

            // Vérification de chevauchement avec les autres réservations de l'utilisateur
            if (debut.isBefore(finUtilisateur) && fin.isAfter(debutUtilisateur)) {
                throw new BadRequestException("Vous avez déjà une réservation de véhicule pour cette période. " +
                        "Conflit avec votre réservation du " + debutUtilisateur + " au " + finUtilisateur);
            }
        }
    }




}