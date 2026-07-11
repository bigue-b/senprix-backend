package sn.dci.senprix.campagne.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Représente la réponse JSON renvoyée par l'endpoint public
 * GET /api/public/utilisateurs/{id}/verification du user-service.
 * Ce DTO ne circule jamais dans l'API publique du campagne-service —
 * il est utilisé uniquement par UserServiceClient pour désérialiser
 * la réponse du user-service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UtilisateurVerificationDto {

    private boolean existe;
    private String role;
    private boolean actif;
}
