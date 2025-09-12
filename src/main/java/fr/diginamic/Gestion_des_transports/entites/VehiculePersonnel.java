package fr.diginamic.Gestion_des_transports.entites;

import fr.diginamic.Gestion_des_transports.enums.Categorie;
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

    public VehiculePersonnel(int id, String immatriculation, Integer nbPlaces, String modele, Double co2ParKm, String photo, String marque, Motorisation motorisation, Categorie categorie, Utilisateur utilisateur) {
        super(id, immatriculation, nbPlaces, modele, co2ParKm, photo, marque, motorisation, categorie);
        this.utilisateur = utilisateur;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}