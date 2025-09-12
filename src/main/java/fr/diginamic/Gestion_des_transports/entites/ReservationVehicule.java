package fr.diginamic.Gestion_des_transports.entites;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicule-entreprise-reservations")
public class ReservationVehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

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

    // Constructor with parameters
    public ReservationVehicule(Utilisateur utilisateur, VehiculeEntreprise vehiculeEntreprise,
                               LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.utilisateur = utilisateur;
        this.vehiculeEntreprise = vehiculeEntreprise;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public VehiculeEntreprise getVehiculeEntreprise() {
        return vehiculeEntreprise;
    }

    public void setVehiculeEntreprise(VehiculeEntreprise vehiculeService) {
        this.vehiculeEntreprise = vehiculeService;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }
}