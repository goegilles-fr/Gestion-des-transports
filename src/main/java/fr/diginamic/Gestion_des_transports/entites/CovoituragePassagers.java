package fr.diginamic.Gestion_des_transports.entites;

import jakarta.persistence.*;

@Entity
@Table(name = "covoiturage_passagers")
public class CovoituragePassagers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "annonce_covoiturage_id")
    private AnnonceCovoiturage annonceCovoiturage;

    // Constructeur par défaut
    public CovoituragePassagers() {}

    // Constructeur avec paramètres
    public CovoituragePassagers(Utilisateur utilisateur, AnnonceCovoiturage annonceCovoiturage) {
        this.utilisateur = utilisateur;
        this.annonceCovoiturage = annonceCovoiturage;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public AnnonceCovoiturage getAnnonceCovoiturage() {
        return annonceCovoiturage;
    }

    public void setAnnonceCovoiturage(AnnonceCovoiturage annonceCovoiturage) {
        this.annonceCovoiturage = annonceCovoiturage;
    }
}