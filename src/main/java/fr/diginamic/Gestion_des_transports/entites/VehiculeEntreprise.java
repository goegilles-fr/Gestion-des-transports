package fr.diginamic.Gestion_des_transports.entites;


import fr.diginamic.Gestion_des_transports.enums.Categorie;
import fr.diginamic.Gestion_des_transports.enums.Motorisation;
import fr.diginamic.Gestion_des_transports.enums.StatutVehicule;
import jakarta.persistence.*;

@Entity
@Table(name = "vehicule_entreprise")
public class VehiculeEntreprise extends Vehicule {

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutVehicule statut;



    // Default constructor
    public VehiculeEntreprise() {
        super();
    }

    public VehiculeEntreprise(StatutVehicule statut) {
        this.statut = statut;
    }

    public VehiculeEntreprise(Long id, String immatriculation, Integer nbPlaces, String modele, Integer co2ParKm, String photo, String marque, Motorisation motorisation, Categorie categorie, StatutVehicule statut) {
        super(id, immatriculation, nbPlaces, modele, co2ParKm, photo, marque, motorisation, categorie);
        this.statut = statut;
    }

    public StatutVehicule getStatut() {
        return statut;
    }

    public void setStatut(StatutVehicule statut) {
        this.statut = statut;
    }


}