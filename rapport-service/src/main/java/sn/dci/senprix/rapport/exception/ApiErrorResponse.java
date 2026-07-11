package sn.dci.senprix.rapport.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Structure uniforme de réponse d'erreur renvoyée par l'API du rapport-service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {

    private LocalDateTime horodatage;
    private int statut;
    private String erreur;
    private String message;
    private String chemin;
    private Map<String, String> erreursValidation;
}
