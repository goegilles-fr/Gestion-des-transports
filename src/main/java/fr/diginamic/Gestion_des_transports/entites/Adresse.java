package fr.diginamic.Gestion_des_transports.entites;

import jakarta.persistence.*;

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

    // Constructor with parameters
    public Adresse(Integer numero, String libelle, String codePostal, String ville) {
        this.numero = numero;
        this.libelle = libelle;
        this.codePostal = codePostal;
        this.ville = ville;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }
}