package fr.diginamic.Gestion_des_transports.entites;

import fr.diginamic.Gestion_des_transports.enums.Motorisation;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private int id;

    @Column(name = "immatriculation", unique = true)
    private String immatriculation;

    @Column(name = "nb_places")
    private Integer nbPlaces;

    @Column(name = "modele")
    private String modele;

    @Column(name = "co2_par_km")
    private Double co2ParKm;

    @Column(name = "photo")
    private String photo;

    @Column(name = "marque")
    private String marque;

    @Enumerated(EnumType.STRING)
    @Column(name = "motorisation")
    private Motorisation motorisation;

    // Default constructor
    public Vehicule() {}

    // Constructor with parameters
    public Vehicule(String immatriculation, Integer nbPlaces, String modele, String marque, Motorisation motorisation) {
        this.immatriculation = immatriculation;
        this.nbPlaces = nbPlaces;
        this.modele = modele;
        this.marque = marque;
        this.motorisation = motorisation;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public Integer getNbPlaces() {
        return nbPlaces;
    }

    public void setNbPlaces(Integer nbPlaces) {
        this.nbPlaces = nbPlaces;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public Double getCo2ParKm() {
        return co2ParKm;
    }

    public void setCo2ParKm(Double co2ParKm) {
        this.co2ParKm = co2ParKm;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public Motorisation getMotorisation() {
        return motorisation;
    }

    public void setMotorisation(Motorisation motorisation) {
        this.motorisation = motorisation;
    }
}