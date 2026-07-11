package sn.dci.senprix.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Représente un credential au format attendu par l'API Admin Keycloak.
 * type="password" avec temporary=true force l'utilisateur à changer
 * son mot de passe lors de sa première connexion (action UPDATE_PASSWORD).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeycloakCredentialPayload {

    @Builder.Default
    private String type = "password";

    private String value;

    @Builder.Default
    private Boolean temporary = true;
}
