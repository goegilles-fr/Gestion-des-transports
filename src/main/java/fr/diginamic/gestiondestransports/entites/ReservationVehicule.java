package fr.diginamic.gestiondestransports.entites;

import jakarta.persistence.*;
import java.time.LocalDateTime;
/**
 * Entité représentant une réservation de véhicule d'entreprise par un collaborateur.
 * Permet de gérer les réservations de véhicules de service pour des déplacements professionnels.
 * Cette classe est mappée à la table 'vehicule_entreprise_reservations' dans la base de données.
 */
@Entity
@Table(name = "vehicule_entreprise_reservations")
public class ReservationVehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "vehicule_entreprise_id")
    private VehiculeEntreprise vehiculeEntreprise;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    // Default constructor
    public ReservationVehicule() {}

    /**
     * Constructeur avec paramètres pour créer une réservation de véhicule d'entreprise.
     *
     * @param utilisateur l'utilisateur effectuant la réservation
     * @param vehiculeEntreprise le véhicule d'entreprise réservé
     * @param dateDebut la date et heure de début de la réservation
     * @param dateFin la date et heure de fin estimée de la réservation
     */
    public ReservationVehicule(Utilisateur utilisateur, VehiculeEntreprise vehiculeEntreprise,
                               LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.utilisateur = utilisateur;
        this.vehiculeEntreprise = vehiculeEntreprise;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    /**
     * Récupère l'identifiant unique de la réservation.
     *
     * @return l'identifiant de la réservation
     */
    public Long getId() {
        return id;
    }

    /**
     * Définit l'identifiant unique de la réservation.
     *
     * @param id l'identifiant à définir
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Récupère l'utilisateur ayant effectué la réservation.
     *
     * @return l'utilisateur responsable de la réservation
     */
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    /**
     * Définit l'utilisateur ayant effectué la réservation.
     *
     * @param utilisateur l'utilisateur à définir
     */
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
    /**
     * Récupère le véhicule d'entreprise réservé.
     *
     * @return le véhicule d'entreprise
     */
    public VehiculeEntreprise getVehiculeEntreprise() {
        return vehiculeEntreprise;
    }

    /**
     * Définit le véhicule d'entreprise réservé.
     *
     * @param vehiculeService le véhicule d'entreprise à définir
     */
    public void setVehiculeEntreprise(VehiculeEntreprise vehiculeService) {
        this.vehiculeEntreprise = vehiculeService;
    }

    /**
     * Récupère la date et heure de début de la réservation.
     *
     * @return la date et heure de début
     */
    public LocalDateTime getDateDebut() {
        return dateDebut;
    }
    /**
     * Définit la date et heure de début de la réservation.
     *
     * @param dateDebut la date et heure de début à définir
     */
    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }
    /**
     * Récupère la date et heure de fin estimée de la réservation.
     *
     * @return la date et heure de fin
     */
    public LocalDateTime getDateFin() {
        return dateFin;
    }
    /**
     * Définit la date et heure de fin estimée de la réservation.
     *
     * @param dateFin la date et heure de fin à définir
     */
    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }
}