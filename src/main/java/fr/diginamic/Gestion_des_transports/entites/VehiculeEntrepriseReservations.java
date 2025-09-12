package fr.diginamic.Gestion_des_transports.entites;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicule-entreprise-reservations")
public class VehiculeEntrepriseReservations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "vehicule_entreprise_id")
    private VehiculeEntreprise vehiculeService;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    // Default constructor
    public VehiculeEntrepriseReservations() {}

    // Constructor with parameters
    public VehiculeEntrepriseReservations(Utilisateur utilisateur, VehiculeEntreprise vehiculeService,
                                          LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.utilisateur = utilisateur;
        this.vehiculeService = vehiculeService;
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

    public VehiculeEntreprise getVehiculeService() {
        return vehiculeService;
    }

    public void setVehiculeService(VehiculeEntreprise vehiculeService) {
        this.vehiculeService = vehiculeService;
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