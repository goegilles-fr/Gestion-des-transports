package fr.diginamic.Gestion_des_transports.entites;

import fr.diginamic.Gestion_des_transports.enums.RoleEnum;
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "email", unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "adresse_id")
    private Adresse adresse;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private RoleEnum role;

    @Column(name = "est_banni")
    private Boolean estBanni = false;

    @Column(name = "est_verifie")
    private Boolean estVerifie = false;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<VehiculePersonnel> vehiculesPersonnels;



    @OneToMany(mappedBy = "responsable", fetch = FetchType.LAZY)
    private Set<AnnonceCovoiturage> annoncesResponsables;

    @ManyToMany(mappedBy = "utilisateurs")
    private Set<AnnonceCovoiturage> annoncesParticipees;

    // Default constructor
    public Utilisateur() {}

    // Constructor with basic parameters
    public Utilisateur(String nom, String prenom, String email, RoleEnum role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.estBanni = false;
        this.estVerifie = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    public Boolean getEstBanni() {
        return estBanni;
    }

    public void setEstBanni(Boolean estBanni) {
        this.estBanni = estBanni;
    }

    public Boolean getEstVerifie() {
        return estVerifie;
    }

    public void setEstVerifie(Boolean estVerifie) {
        this.estVerifie = estVerifie;
    }

    public Set<VehiculePersonnel> getVehiculesPersonnels() {
        return vehiculesPersonnels;
    }

    public void setVehiculesPersonnels(Set<VehiculePersonnel> vehiculesPersonnels) {
        this.vehiculesPersonnels = vehiculesPersonnels;
    }





    public Set<AnnonceCovoiturage> getAnnoncesResponsables() {
        return annoncesResponsables;
    }

    public void setAnnoncesResponsables(Set<AnnonceCovoiturage> annoncesResponsables) {
        this.annoncesResponsables = annoncesResponsables;
    }

    public Set<AnnonceCovoiturage> getAnnoncesParticipees() {
        return annoncesParticipees;
    }

    public void setAnnoncesParticipees(Set<AnnonceCovoiturage> annoncesParticipees) {
        this.annoncesParticipees = annoncesParticipees;
    }
}