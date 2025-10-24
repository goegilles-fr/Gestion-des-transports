package fr.diginamic.gestiondestransports.entites;

import jakarta.persistence.*;
/**
 * Entité représentant la relation entre un utilisateur passager et une annonce de covoiturage.
 * Cette table de jointure permet de gérer les réservations des passagers sur les covoiturages.
 * Cette classe est mappée à la table 'covoiturage_passagers' dans la base de données.
 */
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

    /**
     * Constructeur avec paramètres pour créer une réservation de passager.
     *
     * @param utilisateur l'utilisateur qui réserve une place dans le covoiturage
     * @param annonceCovoiturage l'annonce de covoiturage sur laquelle la réservation est effectuée
     */
    public CovoituragePassagers(Utilisateur utilisateur, AnnonceCovoiturage annonceCovoiturage) {
        this.utilisateur = utilisateur;
        this.annonceCovoiturage = annonceCovoiturage;
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
     * Récupère l'utilisateur passager ayant effectué la réservation.
     *
     * @return l'utilisateur passager
     */
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
    /**
     * Définit l'utilisateur passager ayant effectué la réservation.
     *
     * @param utilisateur l'utilisateur passager à définir
     */
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
    /**
     * Récupère l'annonce de covoiturage associée à cette réservation.
     *
     * @return l'annonce de covoiturage
     */
    public AnnonceCovoiturage getAnnonceCovoiturage() {
        return annonceCovoiturage;
    }
    /**
     * Définit l'annonce de covoiturage associée à cette réservation.
     *
     * @param annonceCovoiturage l'annonce de covoiturage à définir
     */
    public void setAnnonceCovoiturage(AnnonceCovoiturage annonceCovoiturage) {
        this.annonceCovoiturage = annonceCovoiturage;
    }
}