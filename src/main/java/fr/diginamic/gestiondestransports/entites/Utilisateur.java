package fr.diginamic.gestiondestransports.entites;

import fr.diginamic.gestiondestransports.enums.RoleEnum;
import jakarta.persistence.*;
import java.util.Set;
/**
 * Entité représentant un utilisateur du système de gestion des transports.
 * Un utilisateur peut être un collaborateur ou un administrateur, capable de créer des annonces de covoiturage,
 * réserver des véhicules de service, et gérer son profil personnel.
 * Cette classe est mappée à la table 'utilisateur' dans la base de données.
 */
@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

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

    @Column(name = "est_supprime")
    private Boolean estSupprime = false;


    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<VehiculePersonnel> vehiculesPersonnels;



    @OneToMany(mappedBy = "responsable", fetch = FetchType.LAZY)
    private Set<AnnonceCovoiturage> annoncesResponsables;



    // Default constructor
    public Utilisateur() {}

    /**
     * Constructeur avec paramètres de base pour créer un utilisateur.
     * Initialise automatiquement les statuts estBanni et estVerifie à false.
     *
     * @param nom le nom de famille de l'utilisateur
     * @param prenom le prénom de l'utilisateur
     * @param email l'adresse email unique de l'utilisateur
     * @param role le rôle de l'utilisateur (COLLABORATEUR ou ADMINISTRATEUR)
     */
    public Utilisateur(String nom, String prenom, String email, RoleEnum role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.estBanni = false;
        this.estVerifie = false;
    }

    /**
     * Récupère l'identifiant unique de l'utilisateur.
     *
     * @return l'identifiant de l'utilisateur
     */
    public Long getId() {
        return id;
    }
    /**
     * Définit l'identifiant unique de l'utilisateur.
     *
     * @param id l'identifiant à définir
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * Récupère le nom de famille de l'utilisateur.
     *
     * @return le nom de famille
     */
    public String getNom() {
        return nom;
    }
    /**
     * Définit le nom de famille de l'utilisateur.
     *
     * @param nom le nom de famille à définir
     */
    public void setNom(String nom) {
        this.nom = nom;
    }
    /**
     * Récupère le prénom de l'utilisateur.
     *
     * @return le prénom
     */
    public String getPrenom() {
        return prenom;
    }
    /**
     * Définit le prénom de l'utilisateur.
     *
     * @param prenom le prénom à définir
     */
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    /**
     * Récupère l'adresse email de l'utilisateur.
     *
     * @return l'adresse email
     */
    public String getEmail() {
        return email;
    }
    /**
     * Définit l'adresse email de l'utilisateur.
     *
     * @param email l'adresse email à définir
     */
    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * Récupère l'adresse postale de l'utilisateur.
     *
     * @return l'adresse postale
     */
    public Adresse getAdresse() {
        return adresse;
    }
    /**
     * Définit l'adresse postale de l'utilisateur.
     *
     * @param adresse l'adresse postale à définir
     */
    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }
    /**
     * Récupère le rôle de l'utilisateur dans le système.
     *
     * @return le rôle (COLLABORATEUR ou ADMINISTRATEUR)
     */
    public RoleEnum getRole() {
        return role;
    }
    /**
     * Définit le rôle de l'utilisateur dans le système.
     *
     * @param role le rôle à définir
     */
    public void setRole(RoleEnum role) {
        this.role = role;
    }
    /**
     * Indique si l'utilisateur est banni du système.
     *
     * @return true si l'utilisateur est banni, false sinon
     */
    public Boolean getEstBanni() {
        return estBanni;
    }
    /**
     * Définit le statut de bannissement de l'utilisateur.
     *
     * @param estBanni true pour bannir l'utilisateur, false sinon
     */
    public void setEstBanni(Boolean estBanni) {
        this.estBanni = estBanni;
    }
    /**
     * Indique si le compte de l'utilisateur a été vérifié par un administrateur.
     *
     * @return true si le compte est vérifié, false sinon
     */
    public Boolean getEstVerifie() {
        return estVerifie;
    }
    /**
     * Définit le statut de vérification du compte utilisateur.
     *
     * @param estVerifie true si le compte est vérifié, false sinon
     */
    public void setEstVerifie(Boolean estVerifie) {
        this.estVerifie = estVerifie;
    }
    /**
     * Récupère l'ensemble des véhicules personnels appartenant à l'utilisateur.
     *
     * @return l'ensemble des véhicules personnels
     */
    public Set<VehiculePersonnel> getVehiculesPersonnels() {
        return vehiculesPersonnels;
    }
    /**
     * Définit l'ensemble des véhicules personnels appartenant à l'utilisateur.
     *
     * @param vehiculesPersonnels l'ensemble des véhicules personnels à définir
     */
    public void setVehiculesPersonnels(Set<VehiculePersonnel> vehiculesPersonnels) {
        this.vehiculesPersonnels = vehiculesPersonnels;
    }




    /**
     * Récupère l'ensemble des annonces de covoiturage dont l'utilisateur est responsable/organisateur.
     *
     * @return l'ensemble des annonces dont l'utilisateur est responsable
     */
    public Set<AnnonceCovoiturage> getAnnoncesResponsables() {
        return annoncesResponsables;
    }
    /**
     * Définit l'ensemble des annonces de covoiturage dont l'utilisateur est responsable/organisateur.
     *
     * @param annoncesResponsables l'ensemble des annonces à définir
     */
    public void setAnnoncesResponsables(Set<AnnonceCovoiturage> annoncesResponsables) {
        this.annoncesResponsables = annoncesResponsables;
    }


    /**
     * Définit le mot de passe crypté de l'utilisateur.
     *
     * @param password le mot de passe crypté à définir
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /**
     * Récupère le mot de passe crypté de l'utilisateur.
     *
     * @return le mot de passe crypté
     */
    public String getPassword() {
        return password;
    }
    /**
     * Indique si l'utilisateur a été supprimé logiquement du système.
     *
     * @return true si l'utilisateur est marqué comme supprimé, false sinon
     */
    public Boolean getEstSupprime() {
        return estSupprime;
    }
    /**
     * Définit le statut de suppression logique de l'utilisateur.
     *
     * @param estSupprime true pour marquer l'utilisateur comme supprimé, false sinon
     */
    public void setEstSupprime(Boolean estSupprime) {
        this.estSupprime = estSupprime;
    }


}