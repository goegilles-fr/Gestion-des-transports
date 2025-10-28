package fr.diginamic.gestiondestransports.shared;
/**
 * Exception levée lorsqu'une ressource demandée n'existe pas (HTTP 404 Not Found).
 * Utilisée pour signaler qu'un utilisateur, véhicule, réservation ou annonce
 * recherché par ID n'a pas été trouvé en base de données.
 * Hérite de RuntimeException pour une gestion non-vérifiée des exceptions.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
