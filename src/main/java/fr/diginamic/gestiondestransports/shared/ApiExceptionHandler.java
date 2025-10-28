package fr.diginamic.gestiondestransports.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * Gestionnaire global des exceptions pour l'API REST.
 * Intercepte toutes les exceptions levées par les contrôleurs et les convertit
 * en réponses HTTP standardisées avec codes de statut appropriés.
 * Fournit des messages d'erreur cohérents et structurés au format JSON.
 * Structure de réponse : timestamp, status, error, message, path, method, details (optionnel).
 * Améliore l'expérience développeur en retournant des erreurs exploitables par le frontend.
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    /**
     * Gère les exceptions BadRequestException (erreurs métier de validation).
     * Retourne une réponse HTTP 400 avec le message d'erreur personnalisé.
     * Utilisé pour les règles métier non respectées (dates invalides, véhicule non disponible, etc.).
     *
     * @param ex l'exception BadRequestException levée
     * @param req la requête HTTP qui a causé l'erreur
     * @return ResponseEntity avec statut 400 et détails de l'erreur au format JSON
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
    }
    /**
     * Gère les exceptions ConflictException (conflits de ressources).
     * Retourne une réponse HTTP 409 avec le message d'erreur personnalisé.
     * Utilisé pour les conflits d'état (email déjà existant, ressource déjà modifiée, etc.).
     *
     * @param ex l'exception ConflictException levée
     * @param req la requête HTTP qui a causé l'erreur
     * @return ResponseEntity avec statut 409 et détails de l'erreur au format JSON
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }
    /**
     * Gère les exceptions NotFoundException (ressources introuvables).
     * Retourne une réponse HTTP 404 avec le message d'erreur personnalisé.
     * Utilisé quand un utilisateur, véhicule, réservation ou annonce n'existe pas.
     *
     * @param ex l'exception NotFoundException levée
     * @param req la requête HTTP qui a causé l'erreur
     * @return ResponseEntity avec statut 404 et détails de l'erreur au format JSON
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }
    /**
     * Gère les erreurs de validation des annotations Jakarta Validation (@Valid).
     * Retourne une réponse HTTP 400 avec la liste des champs en erreur et leurs messages.
     * Utilisé automatiquement par Spring lors de l'échec de validation des DTOs.
     * Le champ "details" contient un Map des erreurs par champ (fieldName -> errorMessage).
     *
     * @param ex l'exception MethodArgumentNotValidException contenant les erreurs de validation
     * @param req la requête HTTP qui a causé l'erreur
     * @return ResponseEntity avec statut 400 et détails des erreurs de validation par champ
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Requête invalide.", req, fieldErrors);
    }
    /**
     * Gère les violations de contraintes de validation (paramètres de méthode invalides).
     * Retourne une réponse HTTP 400 avec la liste des contraintes violées.
     * Utilisé pour les validations sur les paramètres de requête (@RequestParam, @PathVariable).
     * Le champ "details" contient un Map des violations par propriété.
     *
     * @param ex l'exception ConstraintViolationException contenant les violations
     * @param req la requête HTTP qui a causé l'erreur
     * @return ResponseEntity avec statut 400 et détails des violations de contraintes
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> violations = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> violations.put(v.getPropertyPath().toString(), v.getMessage()));
        return build(HttpStatus.BAD_REQUEST, "Paramètres invalides.", req, violations);
    }

    /**
     * Gestionnaire de secours pour toutes les exceptions non gérées explicitement.
     * Retourne une réponse HTTP 500 avec le message d'erreur générique.
     * Évite l'exposition de stack traces au client en cas d'erreur imprévue.
     * Log l'exception pour investigation côté serveur.
     *
     * @param ex l'exception non gérée levée
     * @param req la requête HTTP qui a causé l'erreur
     * @return ResponseEntity avec statut 500 et message d'erreur interne
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req, null);
    }
    /**
     * Construit une réponse d'erreur standardisée au format JSON.
     * Structure de réponse :
     * - timestamp : date/heure de l'erreur (ISO 8601)
     * - status : code HTTP numérique (400, 404, 409, 500, etc.)
     * - error : libellé du statut HTTP (Bad Request, Not Found, etc.)
     * - message : message d'erreur descriptif
     * - path : URI de la requête ayant échoué
     * - method : méthode HTTP utilisée (GET, POST, PUT, DELETE)
     * - details : map optionnel des erreurs détaillées par champ (pour validations)
     *
     * @param status le code de statut HTTP à retourner
     * @param message le message d'erreur principal
     * @param req la requête HTTP source de l'erreur
     * @param details map optionnel des détails d'erreur par champ (peut être null)
     * @return ResponseEntity contenant le body JSON structuré et le statut HTTP approprié
     */
    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, HttpServletRequest req,
                                                      Map<String, String> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", req.getRequestURI());
        body.put("method", req.getMethod());
        if (details != null && !details.isEmpty()) body.put("details", details);
        return ResponseEntity.status(status).body(body);
    }
}
