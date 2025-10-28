package fr.diginamic.gestiondestransports.services.impl;

import fr.diginamic.gestiondestransports.entites.Adresse;
import fr.diginamic.gestiondestransports.repositories.AdresseRepository;
import fr.diginamic.gestiondestransports.services.AdresseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
/**
 * Implémentation du service de gestion des adresses.
 * Fournit les opérations CRUD complètes pour les entités Adresse.
 * Gère la création d'adresses à partir d'objets ou de paramètres individuels.
 * Les adresses sont partagées entre les utilisateurs et les annonces de covoiturage
 * pour éviter la duplication de données identiques en base.
 * Toutes les opérations sont transactionnelles pour garantir la cohérence des données.
 */
@Service
@Transactional
public class AdresseServiceImpl implements AdresseService {

    @Autowired
    private AdresseRepository adresseRepository;

    /**
     * Créer une nouvelle adresse
     * @param adresse L'adresse à créer
     * @return L'adresse créée avec son ID
     */
    @Override
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
    @Override
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
    @Override
    public Adresse trouverParId(Long id) {
        Optional<Adresse> adresse = adresseRepository.findById(id);
        return adresse.orElse(null);
    }




    /**
     * Mettre à jour une adresse existante
     * @param id L'ID de l'adresse
     * @param adresseModifiee Les nouvelles données de l'adresse
     * @return L'adresse mise à jour ou null si pas trouvée
     */
    @Override
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
    @Override
    public List<Adresse> obtenirToutesLesAdresses() {
        return adresseRepository.findAll();
    }

    /**
     * Supprimer une adresse par ID
     * @param id L'ID de l'adresse à supprimer
     * @return true si supprimée, false si pas trouvée
     */
    @Override
    public boolean supprimerAdresse(Long id) {
        if (adresseRepository.existsById(id)) {
            adresseRepository.deleteById(id);
            return true;
        }
        return false;
    }
}