package fr.diginamic.Gestion_des_transports.entites;


import fr.diginamic.Gestion_des_transports.enums.Motorisation;
import fr.diginamic.Gestion_des_transports.enums.StatutVehicule;
import jakarta.persistence.*;

@Entity
@Table(name = "vehicule_service")
public class VehiculeService extends Vehicule {

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutVehicule statut;



    // Default constructor
    public VehiculeService() {
        super();
    }

    // Constructor with parameters
    public VehiculeService(String immatriculation, Integer nbPlaces, String modele, String marque,
                           Motorisation motorisation, StatutVehicule statut) {
        super(immatriculation, nbPlaces, modele, marque, motorisation);
        this.statut = statut;
    }

    public StatutVehicule getStatut() {
        return statut;
    }

    public void setStatut(StatutVehicule statut) {
        this.statut = statut;
    }


}