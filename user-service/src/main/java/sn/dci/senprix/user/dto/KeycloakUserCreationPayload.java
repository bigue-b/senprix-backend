package sn.dci.senprix.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Représente exactement la structure JSON attendue par l'endpoint
 * POST /admin/realms/{realm}/users de l'API Admin REST de Keycloak.
 * Ce DTO ne circule jamais dans l'API publique de SEN-PRIX — il est
 * utilisé uniquement par le KeycloakAdminClient pour dialoguer avec Keycloak.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeycloakUserCreationPayload {

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean enabled;
    private Boolean emailVerified;

    private List<KeycloakCredentialPayload> credentials;

    @JsonProperty("requiredActions")
    private List<String> requiredActions;
}
