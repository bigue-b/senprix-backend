package sn.dci.senprix.alerte.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralise la gestion des exceptions pour produire des réponses
 * d'erreur HTTP cohérentes sur l'ensemble des endpoints du alerte-service.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlerteNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> gererAlerteNotFound(
            AlerteNotFoundException ex, HttpServletRequest request) {
        return construireReponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(TransitionInvalideException.class)
    public ResponseEntity<ApiErrorResponse> gererTransitionInvalide(
            TransitionInvalideException ex, HttpServletRequest request) {
        return construireReponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> gererValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> erreurs = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                erreurs.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        return construireReponse(
                HttpStatus.BAD_REQUEST,
                "Données invalides — vérifiez les champs du formulaire",
                request,
                erreurs
        );
    }

    /**
     * Intercepte les refus d'accès levés par @PreAuthorize (rôle insuffisant)
     * et retourne un 403 Forbidden explicite, plutôt que de laisser cette
     * exception tomber dans le gestionnaire générique 500.
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> gererAccesRefuse(
            AuthorizationDeniedException ex, HttpServletRequest request) {
        return construireReponse(
                HttpStatus.FORBIDDEN,
                "Accès refusé — vous n'avez pas les droits nécessaires pour cette action",
                request,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> gererExceptionGenerique(
            Exception ex, HttpServletRequest request) {
        return construireReponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue : " + ex.getMessage(),
                request,
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> construireReponse(
            HttpStatus statut, String message, HttpServletRequest request,
            Map<String, String> erreursValidation) {

        ApiErrorResponse erreur = ApiErrorResponse.builder()
                .horodatage(LocalDateTime.now())
                .statut(statut.value())
                .erreur(statut.getReasonPhrase())
                .message(message)
                .chemin(request.getRequestURI())
                .erreursValidation(erreursValidation)
                .build();

        return ResponseEntity.status(statut).body(erreur);
    }
}
