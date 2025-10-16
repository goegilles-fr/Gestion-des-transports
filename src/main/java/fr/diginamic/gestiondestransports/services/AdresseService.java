package fr.diginamic.gestiondestransports.services;

import fr.diginamic.gestiondestransports.entites.Adresse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AdresseService {
    Adresse creerAdresse(Adresse adresse);

    Adresse creerAdresse(Integer numero, String libelle, String codePostal, String ville);

    @Transactional(readOnly = true)
    Adresse trouverParId(Long id);

    Adresse mettreAJourAdresse(Long id, Adresse adresseModifiee);

    @Transactional(readOnly = true)
    List<Adresse> obtenirToutesLesAdresses();

    boolean supprimerAdresse(Long id);
}
