package fr.diginamic.gestiondestransports.entites;

import jakarta.persistence.*;

/**
 * Entité représentant une adresse postale dans le système de gestion des transports.
 * Cette classe est mappée à la table 'adresse' dans la base de données.
 */
@Entity
@Table(name = "adresse")
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero")
    private Integer numero;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "code_postal", length = 10)
    private String codePostal;

    @Column(name = "ville")
    private String ville;

    // Default constructor
    public Adresse() {}

    /**
     * Constructeur avec paramètres pour créer une adresse complète.
     *
     * @param numero le numéro de rue
     * @param libelle le libellé de la rue (nom de la rue)
     * @param codePostal le code postal
     * @param ville le nom de la ville
     */
    public Adresse(Integer numero, String libelle, String codePostal, String ville) {
        this.numero = numero;
        this.libelle = libelle;
        this.codePostal = codePostal;
        this.ville = ville;
    }

    /**
     * Récupère l'identifiant unique de l'adresse.
     *
     * @return l'identifiant de l'adresse
     */
    public Long getId() {
        return id;
    }
    /**
     * Définit l'identifiant unique de l'adresse.
     *
     * @param id l'identifiant à définir
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * Récupère le numéro de rue.
     *
     * @return le numéro de rue
     */
    public Integer getNumero() {
        return numero;
    }
    /**
     * Définit le numéro de rue.
     *
     * @param numero le numéro de rue à définir
     */
    public void setNumero(Integer numero) {
        this.numero = numero;
    }
    /**
     * Récupère le libellé de la rue.
     *
     * @return le nom de la rue
     */
    public String getLibelle() {
        return libelle;
    }
    /**
     * Définit le libellé de la rue.
     *
     * @param libelle le nom de la rue à définir
     */
    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
    /**
     * Récupère le code postal.
     *
     * @return le code postal
     */
    public String getCodePostal() {
        return codePostal;
    }

    /**
     * Définit le code postal.
     *
     * @param codePostal le code postal à définir
     */
    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }
    /**
     * Récupère le nom de la ville.
     *
     * @return le nom de la ville
     */
    public String getVille() {
        return ville;
    }
    /**
     * Définit le nom de la ville.
     *
     */
    public void setVille(String ville) {
        this.ville = ville;
    }
}