package fr.diginamic.gestiondestransports.entites;

import fr.diginamic.gestiondestransports.enums.Categorie;
import fr.diginamic.gestiondestransports.enums.Motorisation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
/**
 * Classe abstraite représentant un véhicule dans le système de gestion des transports.
 * Cette classe est la classe parente pour les véhicules personnels et d'entreprise.
 * Utilise une stratégie d'héritage TABLE_PER_CLASS pour le mapping JPA.
 */
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

    @Min(2)
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
    protected  Vehicule() {}
    /**
     * Constructeur avec paramètres pour créer un véhicule complet.
     *
     * @param id l'identifiant unique du véhicule
     * @param immatriculation le numéro d'immatriculation unique du véhicule
     * @param nbPlaces le nombre de places disponibles dans le véhicule (minimum 2)
     * @param modele le modèle du véhicule
     * @param co2ParKm les émissions de CO2 par kilomètre
     * @param photo l'URL de la photo du véhicule
     * @param marque la marque du véhicule
     * @param motorisation le type de motorisation du véhicule
     * @param categorie la catégorie du véhicule
     */
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

    /**
     * Récupère l'identifiant unique du véhicule.
     *
     * @return l'identifiant du véhicule
     */
    public Long getId() {
        return id;
    }
    /**
     * Définit l'identifiant unique du véhicule.
     *
     * @param id l'identifiant à définir
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * Récupère le numéro d'immatriculation du véhicule.
     *
     * @return le numéro d'immatriculation
     */
    public String getImmatriculation() {
        return immatriculation;
    }
    /**
     * Définit le numéro d'immatriculation du véhicule.
     *
     * @param immatriculation le numéro d'immatriculation à définir
     */
    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }
    /**
     * Récupère le nombre de places disponibles dans le véhicule.
     *
     * @return le nombre de places (chauffeur inclus)
     */
    public Integer getNbPlaces() {
        return nbPlaces;
    }
    /**
     * Définit le nombre de places disponibles dans le véhicule.
     *
     * @param nbPlaces le nombre de places à définir (minimum 2)
     */
    public void setNbPlaces(Integer nbPlaces) {
        this.nbPlaces = nbPlaces;
    }
    /**
     * Récupère le modèle du véhicule.
     *
     * @return le modèle du véhicule
     */
    public String getModele() {
        return modele;
    }
    /**
     * Définit le modèle du véhicule.
     *
     * @param modele le modèle à définir
     */
    public void setModele(String modele) {
        this.modele = modele;
    }
    /**
     * Récupère les émissions de CO2 par kilomètre du véhicule.
     *
     * @return les émissions de CO2 en grammes par kilomètre
     */
    public Integer getCo2ParKm() {
        return co2ParKm;
    }
    /**
     * Définit les émissions de CO2 par kilomètre du véhicule.
     *
     * @param co2ParKm les émissions de CO2 à définir
     */
    public void setCo2ParKm(Integer co2ParKm) {
        this.co2ParKm = co2ParKm;
    }
    /**
     * Récupère l'URL de la photo du véhicule.
     *
     * @return l'URL de la photo
     */
    public String getPhoto() {
        return photo;
    }
    /**
     * Définit l'URL de la photo du véhicule.
     *
     * @param photo l'URL de la photo à définir
     */
    public void setPhoto(String photo) {
        this.photo = photo;
    }
    /**
     * Récupère la marque du véhicule.
     *
     * @return la marque du véhicule
     */
    public String getMarque() {
        return marque;
    }
    /**
     * Définit la marque du véhicule.
     *
     * @param marque la marque à définir
     */
    public void setMarque(String marque) {
        this.marque = marque;
    }

    /**
     * Récupère le type de motorisation du véhicule.
     *
     * @return le type de motorisation (électrique, hybride, etc.)
     */
    public Motorisation getMotorisation() {
        return motorisation;
    }
    /**
     * Définit le type de motorisation du véhicule.
     *
     * @param motorisation le type de motorisation à définir
     */
    public void setMotorisation(Motorisation motorisation) {
        this.motorisation = motorisation;
    }
    /**
     * Récupère la catégorie du véhicule.
     *
     * @return la catégorie (citadine, berline, SUV, etc.)
     */
    public Categorie getCategorie() {
        return categorie;
    }
    /**
     * Définit la catégorie du véhicule.
     *
     * @param categorie la catégorie à définir
     */
    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }
}