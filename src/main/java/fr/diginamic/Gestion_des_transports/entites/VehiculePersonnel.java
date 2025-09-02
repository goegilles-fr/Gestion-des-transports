package fr.diginamic.Gestion_des_transports.entites;

import fr.diginamic.Gestion_des_transports.enums.Motorisation;
import jakarta.persistence.*;

@Entity
@Table(name = "vehicule_personnel")
public class VehiculePersonnel extends Vehicule {


    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    // Default constructor
    public VehiculePersonnel() {
        super();
    }

    // Constructor with parameters
    public VehiculePersonnel(String immatriculation, Integer nbPlaces, String modele, String marque, Motorisation motorisation) {
        super(immatriculation, nbPlaces, modele, marque, motorisation);
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}