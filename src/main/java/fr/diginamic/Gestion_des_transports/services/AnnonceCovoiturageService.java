package fr.diginamic.Gestion_des_transports.services;

import fr.diginamic.Gestion_des_transports.dto.AnnonceCovoiturageAvecPlacesDto;
import fr.diginamic.Gestion_des_transports.dto.AnnonceCovoiturageDto;
import fr.diginamic.Gestion_des_transports.entites.*;
import fr.diginamic.Gestion_des_transports.mapper.AnnonceCovoiturageMapper;
import fr.diginamic.Gestion_des_transports.mapper.AdresseMapper;
import fr.diginamic.Gestion_des_transports.repositories.*;
import fr.diginamic.Gestion_des_transports.tools.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des annonces de covoiturage
 */
@Service
@Transactional
public class AnnonceCovoiturageService {

    private final AnnonceCovoiturageRepository annonceCovoiturageRepository;
    private final CovoituragePassagersRepository covoituragePassagersRepository;
    private final VehiculePersonnelRepository vehiculePersonnelRepository;
    private final VehiculeEntrepriseRepository vehiculeEntrepriseRepository;
    private final AdresseRepository adresseRepository;
    private final AnnonceCovoiturageMapper annonceMapper;
    private final AdresseMapper adresseMapper;
    private final UtilisateurService utilisateurService;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    public AnnonceCovoiturageService(
            AnnonceCovoiturageRepository annonceCovoiturageRepository,
            VehiculeEntrepriseRepository vehiculeEntrepriseRepository,
            AdresseRepository adresseRepository,
            AnnonceCovoiturageMapper annonceMapper,
            AdresseMapper adresseMapper,
            UtilisateurService utilisateurService,
            CovoituragePassagersRepository covoituragePassagersRepository,  // Add this
            VehiculePersonnelRepository vehiculePersonnelRepository) {
        this.annonceCovoiturageRepository = annonceCovoiturageRepository;
        this.vehiculeEntrepriseRepository = vehiculeEntrepriseRepository;
        this.adresseRepository = adresseRepository;
        this.annonceMapper = annonceMapper;
        this.adresseMapper = adresseMapper;
        this.utilisateurService = utilisateurService;
        this.covoituragePassagersRepository = covoituragePassagersRepository;
        this.vehiculePersonnelRepository = vehiculePersonnelRepository;
    }

    /**
     * Crée une nouvelle annonce de covoiturage
     * @param annonceDto les données de l'annonce
     * @param idUtilisateurResponsable l'ID de l'utilisateur responsable
     * @return l'annonce créée
     */
    public AnnonceCovoiturageDto creerAnnonce(AnnonceCovoiturageDto annonceDto, Long idUtilisateurResponsable) {
        // Récupérer l'utilisateur responsable
        Utilisateur responsable = utilisateurService.obtenirUtilisateurParId(idUtilisateurResponsable);

        // Convertir le DTO vers l'entité
        AnnonceCovoiturage nouvelleAnnonce = annonceMapper.versEntite(annonceDto);

        // Gérer les adresses (créer ou récupérer existantes)
        Adresse adresseDepart = gererAdresse(annonceDto.adresseDepart());
        Adresse adresseArrivee = gererAdresse(annonceDto.adresseArrivee());

        // Configurer l'annonce
        nouvelleAnnonce.setResponsable(responsable);
        nouvelleAnnonce.setAdresseDepart(adresseDepart);
        nouvelleAnnonce.setAdresseArrivee(adresseArrivee);

        // Gérer le véhicule de service si spécifié
        if (annonceDto.vehiculeServiceId() != null) {
            Optional<VehiculeEntreprise> vehiculeOpt = vehiculeEntrepriseRepository.findById(annonceDto.vehiculeServiceId());
            if (vehiculeOpt.isPresent()) {
                nouvelleAnnonce.setVehiculeService(vehiculeOpt.get());
            } else {
                throw new IllegalArgumentException("Véhicule de service introuvable avec l'ID: " + annonceDto.vehiculeServiceId());
            }



        }

        // Si aucun véhicule de service n'est spécifié, vérifier que l'utilisateur a un véhicule personnel
        if (annonceDto.vehiculeServiceId() == null) {
            List<VehiculePersonnel> vehiculesPersonnels = vehiculePersonnelRepository.findByUtilisateur(responsable);

            if (vehiculesPersonnels.isEmpty()) {
                throw new IllegalArgumentException("Vous devez spécifier un véhicule de service ou posséder un véhicule personnel pour créer une annonce de covoiturage.");
            }
        }



        // Sauvegarder l'annonce
        AnnonceCovoiturage annonceSauvegardee = annonceCovoiturageRepository.save(nouvelleAnnonce);

        // Retourner le DTO de l'annonce créée
        return annonceMapper.versDto(annonceSauvegardee);
    }

    /**
     * Modifie une annonce de covoiturage existante
     * @param idAnnonce l'ID de l'annonce à modifier
     * @param annonceDto les nouvelles données
     * @param idUtilisateurResponsable l'ID de l'utilisateur responsable
     * @return l'annonce modifiée
     */
    public AnnonceCovoiturageDto modifierAnnonce(Long idAnnonce, AnnonceCovoiturageDto annonceDto, Long idUtilisateurResponsable) {
        // Vérifier que l'annonce existe
        AnnonceCovoiturage annonceExistante = annonceCovoiturageRepository.findById(idAnnonce)
                .orElseThrow(() -> new IllegalArgumentException("Annonce de covoiturage introuvable avec l'ID: " + idAnnonce));

        // Vérifier que l'utilisateur est bien le responsable de l'annonce
        if (!annonceExistante.getResponsable().getId().equals(idUtilisateurResponsable)) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier cette annonce");
        }
        Integer placesOccupees = obtenirNombrePlacesOccupees(idAnnonce);
        if (placesOccupees > 0) {
            throw new IllegalArgumentException("Impossible de modifier cette annonce car " + placesOccupees + " passager(s) ont déjà réservé une place");
        }

        // Gérer les nouvelles adresses
        if (annonceDto.adresseDepart() != null) {
            Adresse nouvelleAdresseDepart = gererAdresse(annonceDto.adresseDepart());
            annonceExistante.setAdresseDepart(nouvelleAdresseDepart);
        }

        if (annonceDto.adresseArrivee() != null) {
            Adresse nouvelleAdresseArrivee = gererAdresse(annonceDto.adresseArrivee());
            annonceExistante.setAdresseArrivee(nouvelleAdresseArrivee);
        }

        // Gérer le véhicule de service
        if (annonceDto.vehiculeServiceId() != null) {
            Optional<VehiculeEntreprise> vehiculeOpt = vehiculeEntrepriseRepository.findById(annonceDto.vehiculeServiceId());
            if (vehiculeOpt.isPresent()) {
                annonceExistante.setVehiculeService(vehiculeOpt.get());
            } else {
                throw new IllegalArgumentException("Véhicule de service introuvable avec l'ID: " + annonceDto.vehiculeServiceId());
            }
        } else {
            // Si vehiculeServiceId est null, retirer le véhicule de service
            annonceExistante.setVehiculeService(null);
        }

        // Mettre à jour les autres champs
        annonceMapper.mettreAJourEntite(annonceDto, annonceExistante);

        // Sauvegarder les modifications
        AnnonceCovoiturage annonceMiseAJour = annonceCovoiturageRepository.save(annonceExistante);

        // Retourner le DTO de l'annonce modifiée
        return annonceMapper.versDto(annonceMiseAJour);
    }

    /**
     * Supprime une annonce de covoiturage
     * @param idAnnonce l'ID de l'annonce à supprimer
     * @param idUtilisateurResponsable l'ID de l'utilisateur responsable
     */
    public void supprimerAnnonce(Long idAnnonce, Long idUtilisateurResponsable) {
        System.out.println("deleting annonce id=" +idAnnonce);
        // Vérifier que l'annonce existe
        AnnonceCovoiturage annonceExistante = annonceCovoiturageRepository.findById(idAnnonce)
                .orElseThrow(() -> new IllegalArgumentException("Annonce de covoiturage introuvable avec l'ID: " + idAnnonce));

        // Vérifier que l'utilisateur est bien le responsable de l'annonce
        if (!annonceExistante.getResponsable().getId().equals(idUtilisateurResponsable)) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à supprimer cette annonce");
        }


        // Récupérer tous les passagers de cette annonce pour les notifier
        List<CovoituragePassagers> passagers = covoituragePassagersRepository.findByAnnonceCovoiturageId(idAnnonce);

        System.out.println("Passagers à notifier pour l'annonce " + idAnnonce + ":");
        for (CovoituragePassagers passager : passagers) {
            System.out.println(passager.getUtilisateur().getEmail());
        }


        // Envoyer un email à chaque passager pour les informer de l'annulation
        for (CovoituragePassagers passager : passagers) {
            Utilisateur utilisateurPassager = passager.getUtilisateur();
            String nomCompletResponsable = annonceExistante.getResponsable().getPrenom() + " " +
                    annonceExistante.getResponsable().getNom();

            // Formatage des informations du trajet
            String infoTrajet = annonceExistante.getAdresseDepart().getVille() + " → " +
                    annonceExistante.getAdresseArrivee().getVille() +
                    " le " + annonceExistante.getHeureDepart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));

            emailSender.send(
                    utilisateurPassager.getEmail(),
                    "Le covoiturage " + infoTrajet + " organisé par " + nomCompletResponsable +
                            " a été annulé. Nous nous excusons pour ce désagrément. Vous pouvez rechercher d'autres covoiturages sur notre plateforme.",
                    "Hello " + utilisateurPassager.getPrenom() + " " + utilisateurPassager.getNom() +
                            ", votre covoiturage a été annulé",
                    "Annulation de votre covoiturage"
            );
        }



        // Supprimer l'annonce
        annonceCovoiturageRepository.delete(annonceExistante);
    }

    /**
     * Gère la création ou récupération d'une adresse
     * @param adresseDto l'adresse sous forme DTO
     * @return l'entité Adresse
     */
    private Adresse gererAdresse(fr.diginamic.Gestion_des_transports.dto.AdresseDto adresseDto) {
        if (adresseDto.id() != null) {
            // Si l'adresse a un ID, la récupérer depuis la base
            Optional<Adresse> adresseOpt = adresseRepository.findById(adresseDto.id());
            if (adresseOpt.isPresent()) {
                return adresseOpt.get();
            }
        }

        // Sinon, créer une nouvelle adresse
        Adresse nouvelleAdresse = adresseMapper.versEntite(adresseDto);
        return adresseRepository.save(nouvelleAdresse);
    }


    /**
     * Récupère une annonce de covoiturage par son ID
     * @param idAnnonce l'ID de l'annonce
     * @return l'annonce trouvée
     */
    public AnnonceCovoiturageDto obtenirAnnonceParId(Long idAnnonce) {
        AnnonceCovoiturage annonce = annonceCovoiturageRepository.findById(idAnnonce.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Annonce de covoiturage introuvable avec l'ID: " + idAnnonce));

        return annonceMapper.versDto(annonce);
    }

    /**
     * Retourne le nombre total de places disponibles pour les passagers dans le véhicule de l'annonce
     * @param idAnnonce l'ID de l'annonce de covoiturage
     * @return le nombre de places pour passagers
     */
    public Integer obtenirNombrePlacesTotales(Long idAnnonce) {
        AnnonceCovoiturage annonce = annonceCovoiturageRepository.findById(idAnnonce)
                .orElseThrow(() -> new IllegalArgumentException("Annonce de covoiturage introuvable avec l'ID: " + idAnnonce));

        if (annonce.getVehiculeService() != null) {
            return annonce.getVehiculeService().getNbPlaces();
        }

        List<VehiculePersonnel> vehiculesPersonnels = vehiculePersonnelRepository.findByUtilisateur(annonce.getResponsable());

        if (vehiculesPersonnels.isEmpty()) {
            throw new IllegalStateException("Aucun véhicule trouvé pour cette annonce. L'annonce a été créée incorrectement.");
        }

        return vehiculesPersonnels.get(0).getNbPlaces();
    }

    /**
     * Retourne le nombre de places occupées par les passagers dans l'annonce
     * @param idAnnonce l'ID de l'annonce de covoiturage
     * @return le nombre de places occupées
     */
    public Integer obtenirNombrePlacesOccupees(Long idAnnonce) {
        AnnonceCovoiturage annonce = annonceCovoiturageRepository.findById(idAnnonce)
                .orElseThrow(() -> new IllegalArgumentException("Annonce de covoiturage introuvable avec l'ID: " + idAnnonce));

        Long nombrePassagers = covoituragePassagersRepository.countPassagersParCovoiturage(annonce);
        return nombrePassagers.intValue();
    }
    /**
     * Réserve une place dans un covoiturage pour un utilisateur
     * @param idAnnonce l'ID de l'annonce de covoiturage
     * @param idUtilisateur l'ID de l'utilisateur qui réserve
     * @return confirmation de la réservation
     */
    public void reserverPlace(Long idAnnonce, Long idUtilisateur) {
        // Vérifier que l'annonce existe
        AnnonceCovoiturage annonce = annonceCovoiturageRepository.findById(idAnnonce)
                .orElseThrow(() -> new IllegalArgumentException("Annonce de covoiturage introuvable avec l'ID: " + idAnnonce));
        System.out.println("idUtilisateur"+idUtilisateur);
        // Récupérer l'utilisateur
        Utilisateur utilisateur = utilisateurService.obtenirUtilisateurParId(idUtilisateur);

        // Vérifier que l'utilisateur n'est pas déjà inscrit
        Optional<CovoituragePassagers> reservationExistante = covoituragePassagersRepository
                .findByUtilisateurAndAnnonceCovoiturage(utilisateur, annonce);

        if (reservationExistante.isPresent()) {
            throw new IllegalArgumentException("Vous êtes déjà inscrit à ce covoiturage");
        }
        System.out.println("annonce.getResponsable().getId()"+annonce.getResponsable().getId());
        // Vérifier que l'utilisateur n'est pas le responsable du covoiturage
        if (annonce.getResponsable().getId().equals(idUtilisateur)) {
            throw new IllegalArgumentException("Vous ne pouvez pas réserver une place dans votre propre covoiturage");
        }

        // Vérifier s'il reste des places disponibles
        Integer placesTotales = obtenirNombrePlacesTotales(idAnnonce);
        Integer placesOccupees = obtenirNombrePlacesOccupees(idAnnonce);

        if (placesOccupees >= placesTotales) {
            throw new IllegalArgumentException("Aucune place disponible dans ce covoiturage");
        }

        // Créer la réservation
        CovoituragePassagers nouvelleReservation = new CovoituragePassagers(utilisateur, annonce);
        covoituragePassagersRepository.save(nouvelleReservation);
    }

    public void annulerReservation(Long idAnnonce, Long idUtilisateur) {
        // Vérifier que l'annonce existe
        AnnonceCovoiturage annonce = annonceCovoiturageRepository.findById(idAnnonce)
                .orElseThrow(() -> new IllegalArgumentException("Annonce de covoiturage introuvable avec l'ID: " + idAnnonce));

        // Récupérer l'utilisateur
        Utilisateur utilisateur = utilisateurService.obtenirUtilisateurParId(idUtilisateur);

        // Vérifier que l'utilisateur a bien une réservation pour ce covoiturage
        Optional<CovoituragePassagers> reservationExistante = covoituragePassagersRepository
                .findByUtilisateurAndAnnonceCovoiturage(utilisateur, annonce);

        if (reservationExistante.isEmpty()) {
            throw new IllegalArgumentException("Aucune réservation trouvée pour ce covoiturage");
        }

        // Supprimer la réservation
        covoituragePassagersRepository.delete(reservationExistante.get());
    }


    /**
     * Récupère toutes les annonces de covoiturage avec informations sur les places
     * @return liste de toutes les annonces avec détails des places
     */
    public List<AnnonceCovoiturageAvecPlacesDto> obtenirToutesLesAnnonces() {
        List<AnnonceCovoiturage> annonces = annonceCovoiturageRepository.findAll();

        return annonces.stream()
                .map(annonce -> {
                    AnnonceCovoiturageDto dto = annonceMapper.versDto(annonce);
                    Integer placesTotales = obtenirNombrePlacesTotales((long) annonce.getId());
                    Integer placesOccupees = obtenirNombrePlacesOccupees((long) annonce.getId());

                    return AnnonceCovoiturageAvecPlacesDto.of(dto, placesTotales, placesOccupees);
                })
                .toList();
    }

    /**
     * Récupère toutes les annonces de covoiturage où l'utilisateur est passager
     * @param idUtilisateur l'ID de l'utilisateur connecté
     * @return liste des annonces où l'utilisateur est passager avec détails des places
     */
    public List<AnnonceCovoiturageAvecPlacesDto> obtenirReservationsUtilisateur(Long idUtilisateur) {
        // Récupérer l'utilisateur pour vérifier qu'il existe
        Utilisateur utilisateur = utilisateurService.obtenirUtilisateurParId(idUtilisateur);

        // Récupérer toutes les annonces où cet utilisateur est passager
        List<AnnonceCovoiturage> annoncesAvecReservations = annonceCovoiturageRepository.findByUtilisateurParticipant(utilisateur);
        if (annoncesAvecReservations.isEmpty()) {
            throw new IllegalArgumentException("Aucune réservation trouvée pour cet utilisateur");
        }
        return annoncesAvecReservations.stream()
                .map(annonce -> {
                    AnnonceCovoiturageDto dto = annonceMapper.versDto(annonce);
                    Integer placesTotales = obtenirNombrePlacesTotales(annonce.getId());
                    Integer placesOccupees = obtenirNombrePlacesOccupees(annonce.getId());

                    return AnnonceCovoiturageAvecPlacesDto.of(dto, placesTotales, placesOccupees);
                })
                .toList();
    }





}