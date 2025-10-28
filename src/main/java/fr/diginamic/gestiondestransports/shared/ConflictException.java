package fr.diginamic.gestiondestransports.shared;
/**
 * Exception levée lors d'un conflit de ressources (HTTP 409 Conflict).
 * Utilisée pour signaler des conflits d'état ou de données (email déjà existant,
 * réservation en doublon, modification impossible car ressource verrouillée, etc.).
 * Hérite de RuntimeException pour une gestion non-vérifiée des exceptions.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
