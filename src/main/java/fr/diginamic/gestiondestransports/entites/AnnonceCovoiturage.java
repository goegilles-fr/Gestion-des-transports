package fr.diginamic.gestiondestransports.entites;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
/**
 * Entité représentant une annonce de covoiturage dans le système de gestion des transports.
 * Une annonce permet à un collaborateur d'organiser un covoiturage entre deux adresses.
 * Cette classe est mappée à la table 'annonce_covoiturage' dans la base de données.
 */
@Entity
@Table(name = "annonce_covoiturage")
public class AnnonceCovoiturage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "heure_depart")
    private LocalDateTime heureDepart;

    @Column(name = "duree_trajet")
    private Integer dureeTrajet;

    @Column(name = "distance")
    private Integer distance;

    @ManyToOne
    @JoinColumn(name = "adresse_depart")
    private Adresse adresseDepart;

    @ManyToOne
    @JoinColumn(name = "adresse_arrivee")
    private Adresse adresseArrivee;

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Utilisateur responsable;

    @ManyToOne
    @JoinColumn(name = "vehicule_service_id")
    private VehiculeEntreprise vehiculeService;

    // Relation avec les passagers via la table de jointure CovoituragePassagers
    @OneToMany(mappedBy = "annonceCovoiturage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CovoituragePassagers> passagers;

    // Constructeur par défaut
    public AnnonceCovoiturage() {}

    /**
     * Constructeur avec paramètres pour créer une annonce de covoiturage.
     *
     * @param heureDepart la date et heure de départ du covoiturage
     * @param dureeTrajet la durée estimée du trajet en minutes
     * @param distance la distance du trajet en kilomètres
     * @param adresseDepart l'adresse de départ du covoiturage
     * @param adresseArrivee l'adresse d'arrivée du covoiturage
     * @param responsable l'utilisateur responsable/organisateur du covoiturage
     */
    public AnnonceCovoiturage(LocalDateTime heureDepart, Integer dureeTrajet, Integer distance,
                              Adresse adresseDepart, Adresse adresseArrivee, Utilisateur responsable) {
        this.heureDepart = heureDepart;
        this.dureeTrajet = dureeTrajet;
        this.distance = distance;
        this.adresseDepart = adresseDepart;
        this.adresseArrivee = adresseArrivee;
        this.responsable = responsable;
    }

    /**
     * Récupère l'identifiant unique de l'annonce de covoiturage.
     *
     * @return l'identifiant de l'annonce
     */
    public Long getId() {
        return id;
    }
    /**
     * Définit l'identifiant unique de l'annonce de covoiturage.
     *
     * @param id l'identifiant à définir
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * Récupère la date et heure de départ du covoiturage.
     *
     * @return la date et heure de départ
     */
    public LocalDateTime getHeureDepart() {
        return heureDepart;
    }
    /**
     * Définit la date et heure de départ du covoiturage.
     *
     * @param heureDepart la date et heure de départ à définir
     */
    public void setHeureDepart(LocalDateTime heureDepart) {
        this.heureDepart = heureDepart;
    }
    /**
     * Récupère la durée estimée du trajet.
     *
     * @return la durée du trajet en minutes
     */
    public Integer getDureeTrajet() {
        return dureeTrajet;
    }

    /**
     * Définit la durée estimée du trajet.
     *
     * @param dureeTrajet la durée du trajet en minutes à définir
     */
    public void setDureeTrajet(Integer dureeTrajet) {
        this.dureeTrajet = dureeTrajet;
    }

    /**
     * Récupère la distance du trajet.
     *
     * @return la distance en kilomètres
     */
    public Integer getDistance() {
        return distance;
    }

    /**
     * Définit la distance du trajet.
     *
     * @param distance la distance en kilomètres à définir
     */
    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    /**
     * Récupère l'adresse de départ du covoiturage.
     *
     * @return l'adresse de départ
     */
    public Adresse getAdresseDepart() {
        return adresseDepart;
    }

    /**
     * Définit l'adresse de départ du covoiturage.
     *
     * @param adresseDepart l'adresse de départ à définir
     */
    public void setAdresseDepart(Adresse adresseDepart) {
        this.adresseDepart = adresseDepart;
    }
    /**
     * Récupère l'adresse d'arrivée du covoiturage.
     *
     * @return l'adresse d'arrivée
     */
    public Adresse getAdresseArrivee() {
        return adresseArrivee;
    }
    /**
     * Définit l'adresse d'arrivée du covoiturage.
     *
     * @param adresseArrivee l'adresse d'arrivée à définir
     */
    public void setAdresseArrivee(Adresse adresseArrivee) {
        this.adresseArrivee = adresseArrivee;
    }
    /**
     * Récupère l'utilisateur responsable et organisateur du covoiturage.
     *
     * @return l'utilisateur responsable
     */
    public Utilisateur getResponsable() {
        return responsable;
    }
    /**
     * Définit l'utilisateur responsable et organisateur du covoiturage.
     *
     * @param responsable l'utilisateur responsable à définir
     */
    public void setResponsable(Utilisateur responsable) {
        this.responsable = responsable;
    }
    /**
     * Récupère le véhicule de service utilisé pour ce covoiturage.
     *
     * @return le véhicule de service, ou null si un véhicule personnel est utilisé
     */
    public VehiculeEntreprise getVehiculeService() {
        return vehiculeService;
    }

    /**
     * Définit le véhicule de service utilisé pour ce covoiturage.
     *
     * @param vehiculeService le véhicule de service à définir
     */
    public void setVehiculeService(VehiculeEntreprise vehiculeService) {
        this.vehiculeService = vehiculeService;
    }

    /**
     * Récupère l'ensemble des passagers ayant réservé sur ce covoiturage.
     *
     * @return l'ensemble des relations passagers-covoiturage
     */
    public Set<CovoituragePassagers> getPassagers() {
        return passagers;
    }
    /**
     * Définit l'ensemble des passagers ayant réservé sur ce covoiturage.
     *
     * @param passagers l'ensemble des relations passagers-covoiturage à définir
     */
    public void setPassagers(Set<CovoituragePassagers> passagers) {
        this.passagers = passagers;
    }
}