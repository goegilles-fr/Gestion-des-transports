package fr.diginamic.gestiondestransports.entites;

import fr.diginamic.gestiondestransports.enums.Categorie;
import fr.diginamic.gestiondestransports.enums.Motorisation;
import jakarta.persistence.*;
/**
 * Entité représentant un véhicule personnel appartenant à un collaborateur.
 * Ce type de véhicule peut être utilisé par son propriétaire pour organiser des covoiturages.
 * Hérite de la classe abstraite Vehicule et ajoute la relation avec l'utilisateur propriétaire.
 * Cette classe est mappée à la table 'vehicule_personnel' dans la base de données.
 */
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
    /**
     * Constructeur complet pour créer un véhicule personnel avec tous ses attributs.
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
     * @param utilisateur l'utilisateur propriétaire du véhicule
     */
    public VehiculePersonnel(Long id, String immatriculation, Integer nbPlaces, String modele, Integer co2ParKm, String photo, String marque, Motorisation motorisation, Categorie categorie, Utilisateur utilisateur) {
        super(id, immatriculation, nbPlaces, modele, co2ParKm, photo, marque, motorisation, categorie);
        this.utilisateur = utilisateur;
    }
    /**
     * Récupère l'utilisateur propriétaire du véhicule personnel.
     *
     * @return l'utilisateur propriétaire
     */
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
    /**
     * Définit l'utilisateur propriétaire du véhicule personnel.
     *
     * @param utilisateur l'utilisateur propriétaire à définir
     */
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}