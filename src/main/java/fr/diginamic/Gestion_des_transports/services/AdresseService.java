package fr.diginamic.Gestion_des_transports.services;

import fr.diginamic.Gestion_des_transports.entites.Adresse;
import fr.diginamic.Gestion_des_transports.repositories.AdresseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdresseService {

    @Autowired
    private AdresseRepository adresseRepository;

    /**
     * Créer une nouvelle adresse
     * @param adresse L'adresse à créer
     * @return L'adresse créée avec son ID
     */
    public Adresse creerAdresse(Adresse adresse) {
        return adresseRepository.save(adresse);
    }

    /**
     * Créer une adresse à partir des paramètres
     * @param numero Le numéro de rue
     * @param libelle Le nom de la rue
     * @param codePostal Le code postal
     * @param ville La ville
     * @return L'adresse créée
     */
    public Adresse creerAdresse(Integer numero, String libelle, String codePostal, String ville) {
        Adresse nouvelleAdresse = new Adresse(numero, libelle, codePostal, ville);
        return adresseRepository.save(nouvelleAdresse);
    }

    /**
     * Trouver une adresse par ID
     * @param id L'ID de l'adresse
     * @return L'adresse trouvée ou null si pas trouvée
     */
    @Transactional(readOnly = true)
    public Adresse trouverParId(Long id) {
        Optional<Adresse> adresse = adresseRepository.findById(id);
        return adresse.orElse(null);
    }

    /**
     * Rechercher des adresses par ville
     * @param ville La ville à rechercher
     * @return Liste des adresses dans cette ville
     */


    /**
     * Rechercher des adresses par code postal
     * @param codePostal Le code postal à rechercher
     * @return Liste des adresses avec ce code postal
     */
    @Transactional(readOnly = true)
    public List<Adresse> rechercherParCodePostal(String codePostal) {
        // Vous devrez ajouter cette méthode au repository
        return adresseRepository.findByCodePostal(codePostal);
    }

    /**
     * Mettre à jour une adresse existante
     * @param id L'ID de l'adresse
     * @param adresseModifiee Les nouvelles données de l'adresse
     * @return L'adresse mise à jour ou null si pas trouvée
     */
    public Adresse mettreAJourAdresse(Long id, Adresse adresseModifiee) {
        Optional<Adresse> adresseExistante = adresseRepository.findById(id);
        if (adresseExistante.isPresent()) {
            Adresse adresse = adresseExistante.get();
            adresse.setNumero(adresseModifiee.getNumero());
            adresse.setLibelle(adresseModifiee.getLibelle());
            adresse.setCodePostal(adresseModifiee.getCodePostal());
            adresse.setVille(adresseModifiee.getVille());
            return adresseRepository.save(adresse);
        }
        return null;
    }

    /**
     * Obtenir toutes les adresses
     * @return Liste de toutes les adresses
     */
    @Transactional(readOnly = true)
    public List<Adresse> obtenirToutesLesAdresses() {
        return adresseRepository.findAll();
    }

    /**
     * Supprimer une adresse par ID
     * @param id L'ID de l'adresse à supprimer
     * @return true si supprimée, false si pas trouvée
     */
    public boolean supprimerAdresse(Long id) {
        if (adresseRepository.existsById(id)) {
            adresseRepository.deleteById(id);
            return true;
        }
        return false;
    }
}