package fr.diginamic.gestiondestransports.entites;

import fr.diginamic.gestiondestransports.enums.Categorie;
import fr.diginamic.gestiondestransports.enums.Motorisation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Vehicule {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicule_seq")
    @SequenceGenerator(name = "vehicule_seq", sequenceName = "vehicule_sequence", allocationSize = 1)
    private Long id;

    @NotBlank
    @Column(name = "immatriculation", unique = true)
    private String immatriculation;

    @Min(1)
    @Column(name = "nb_places")
    private Integer nbPlaces;

    @NotBlank
    @Column(name = "modele")
    private String modele;

    @Column(name = "co2_par_km")
    private Integer co2ParKm;

    @Column(name = "photo")
    private String photo;

    @NotBlank
    @Column(name = "marque")
    private String marque;

    @Enumerated(EnumType.STRING)
    @Column(name = "motorisation")
    private Motorisation motorisation;


    @Enumerated(EnumType.STRING)
    @Column(name = "categorie")
    private Categorie categorie;



    // Default constructor
    public Vehicule() {}

    public Vehicule(Long id, String immatriculation, Integer nbPlaces, String modele, Integer co2ParKm, String photo, String marque, Motorisation motorisation, Categorie categorie) {
        this.id = id;
        this.immatriculation = immatriculation;
        this.nbPlaces = nbPlaces;
        this.modele = modele;
        this.co2ParKm = co2ParKm;
        this.photo = photo;
        this.marque = marque;
        this.motorisation = motorisation;
        this.categorie = categorie;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Integer getCo2ParKm() {
        return co2ParKm;
    }

    public void setCo2ParKm(Integer co2ParKm) {
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

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }
}