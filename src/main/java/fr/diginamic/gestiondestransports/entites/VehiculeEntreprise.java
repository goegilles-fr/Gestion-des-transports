package fr.diginamic.gestiondestransports.entites;


import fr.diginamic.gestiondestransports.enums.Categorie;
import fr.diginamic.gestiondestransports.enums.Motorisation;
import fr.diginamic.gestiondestransports.enums.StatutVehicule;
import jakarta.persistence.*;
/**
 * Entité représentant un véhicule de service appartenant à l'entreprise.
 * Ce type de véhicule peut être réservé par les collaborateurs pour leurs déplacements professionnels.
 * Hérite de la classe abstraite Vehicule et ajoute la gestion du statut du véhicule.
 * Cette classe est mappée à la table 'vehicule_entreprise' dans la base de données.
 */
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
    /**
     * Constructeur avec le statut uniquement.
     *
     * @param statut le statut du véhicule d'entreprise (EN_SERVICE, EN_REPARATION, HORS_SERVICE)
     */
    public VehiculeEntreprise(StatutVehicule statut) {
        this.statut = statut;
    }
    /**
     * Constructeur complet pour créer un véhicule d'entreprise avec tous ses attributs.
     *
     * @param id l'identifiant unique du véhicule
     * @param immatriculation le numéro d'immatriculation unique du véhicule
     * @param nbPlaces le nombre de places disponibles dans le véhicule
     * @param modele le modèle du véhicule
     * @param co2ParKm les émissions de CO2 par kilomètre
     * @param photo l'URL de la photo du véhicule
     * @param marque la marque du véhicule
     * @param motorisation le type de motorisation du véhicule
     * @param categorie la catégorie du véhicule
     * @param statut le statut du véhicule d'entreprise
     */
    public VehiculeEntreprise(Long id, String immatriculation, Integer nbPlaces, String modele, Integer co2ParKm, String photo, String marque, Motorisation motorisation, Categorie categorie, StatutVehicule statut) {
        super(id, immatriculation, nbPlaces, modele, co2ParKm, photo, marque, motorisation, categorie);
        this.statut = statut;
    }
    /**
     * Récupère le statut actuel du véhicule d'entreprise.
     * Le statut détermine si le véhicule est disponible pour la réservation.
     *
     * @return le statut du véhicule (EN_SERVICE, EN_REPARATION, HORS_SERVICE)
     */
    public StatutVehicule getStatut() {
        return statut;
    }
    /**
     * Définit le statut du véhicule d'entreprise.
     * Si le véhicule est mis hors service ou en réparation, les réservations associées seront annulées.
     *
     * @param statut le statut à définir
     */
    public void setStatut(StatutVehicule statut) {
        this.statut = statut;
    }


}