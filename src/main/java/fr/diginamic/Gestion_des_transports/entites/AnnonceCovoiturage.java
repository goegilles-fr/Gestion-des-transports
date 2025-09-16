package fr.diginamic.Gestion_des_transports.entites;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "annonce_covoiturage")
public class AnnonceCovoiturage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "heure_depart")
    private LocalDateTime heureDepart;

    @Column(name = "duree_trajet")
    private Integer dureeTrajet;

    @Column(name = "distance")
    private Integer distance;

    @ManyToOne
    @JoinColumn(name = "adresse_depart")
    private Adresse adresseDepart;

    @ManyToOne
    @JoinColumn(name = "adresse_arrivee")
    private Adresse adresseArrivee;

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Utilisateur responsable;

    @ManyToOne
    @JoinColumn(name = "vehicule_service_id")
    private VehiculeEntreprise vehiculeService;

    // Relation avec les passagers via la table de jointure CovoituragePassagers
    @OneToMany(mappedBy = "annonceCovoiturage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CovoituragePassagers> passagers;

    // Constructeur par défaut
    public AnnonceCovoiturage() {}

    // Constructeur avec paramètres
    public AnnonceCovoiturage(LocalDateTime heureDepart, Integer dureeTrajet, Integer distance,
                              Adresse adresseDepart, Adresse adresseArrivee, Utilisateur responsable) {
        this.heureDepart = heureDepart;
        this.dureeTrajet = dureeTrajet;
        this.distance = distance;
        this.adresseDepart = adresseDepart;
        this.adresseArrivee = adresseArrivee;
        this.responsable = responsable;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(LocalDateTime heureDepart) {
        this.heureDepart = heureDepart;
    }

    public Integer getDureeTrajet() {
        return dureeTrajet;
    }

    public void setDureeTrajet(Integer dureeTrajet) {
        this.dureeTrajet = dureeTrajet;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Adresse getAdresseDepart() {
        return adresseDepart;
    }

    public void setAdresseDepart(Adresse adresseDepart) {
        this.adresseDepart = adresseDepart;
    }

    public Adresse getAdresseArrivee() {
        return adresseArrivee;
    }

    public void setAdresseArrivee(Adresse adresseArrivee) {
        this.adresseArrivee = adresseArrivee;
    }

    public Utilisateur getResponsable() {
        return responsable;
    }

    public void setResponsable(Utilisateur responsable) {
        this.responsable = responsable;
    }

    public VehiculeEntreprise getVehiculeService() {
        return vehiculeService;
    }

    public void setVehiculeService(VehiculeEntreprise vehiculeService) {
        this.vehiculeService = vehiculeService;
    }

    public Set<CovoituragePassagers> getPassagers() {
        return passagers;
    }

    public void setPassagers(Set<CovoituragePassagers> passagers) {
        this.passagers = passagers;
    }
}