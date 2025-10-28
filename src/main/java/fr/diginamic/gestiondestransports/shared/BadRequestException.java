package fr.diginamic.gestiondestransports.shared;
/**
 * Exception levée lors d'une requête invalide (HTTP 400 Bad Request).
 * Utilisée pour signaler des erreurs de validation métier, des paramètres invalides,
 * ou des contraintes non respectées (dates incohérentes, véhicule non disponible, etc.).
 * Hérite de RuntimeException pour une gestion non-vérifiée des exceptions.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
