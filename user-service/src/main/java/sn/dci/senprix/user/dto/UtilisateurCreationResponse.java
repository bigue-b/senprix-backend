package sn.dci.senprix.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO combiné retourné par l'endpoint de création d'un utilisateur.
 * Regroupe les informations du profil créé et ses identifiants
 * temporaires Keycloak, afin que l'administrateur DCI dispose en
 * une seule réponse de tout ce qu'il doit communiquer au nouvel agent.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurCreationResponse {

    private UtilisateurResponse utilisateur;
    private KeycloakCredentialsResponse credentials;
}
