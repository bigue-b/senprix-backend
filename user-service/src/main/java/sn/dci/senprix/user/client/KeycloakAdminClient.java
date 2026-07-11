package sn.dci.senprix.user.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.dci.senprix.user.config.KeycloakAdminProperties;
import sn.dci.senprix.user.dto.KeycloakCredentialPayload;
import sn.dci.senprix.user.dto.KeycloakTokenResponse;
import sn.dci.senprix.user.dto.KeycloakUserCreationPayload;
import sn.dci.senprix.user.enums.RoleEnum;
import sn.dci.senprix.user.exception.KeycloakIntegrationException;

import java.util.List;
import java.util.Map;

/**
 * Encapsule l'ensemble des interactions avec l'API Admin REST de Keycloak :
 * obtention d'un token d'administration, création d'un compte utilisateur,
 * assignation d'un rôle de Realm. Aucune autre classe de l'application ne
 * doit dialoguer directement avec Keycloak — ce client est le point d'entrée
 * unique, ce qui permet de limiter la diffusion des credentials sensibles
 * (client_secret) à cette seule classe.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminClient {

    private final RestClient restClient;
    private final KeycloakAdminProperties properties;

    /**
     * Obtient un token d'accès via le grant type client_credentials,
     * permettant ensuite d'appeler l'API Admin de Keycloak avec les
     * droits du client d'administration configuré.
     */
    public String obtenirTokenAdmin() {
        String url = properties.getServerUrl()
                + "/realms/" + properties.getAdminRealm()
                + "/protocol/openid-connect/token";

        MultiValueMap<String, String> corps = new LinkedMultiValueMap<>();
        corps.add("grant_type", "client_credentials");
        corps.add("client_id", properties.getClientId());
        corps.add("client_secret", properties.getClientSecret());

        try {
            KeycloakTokenResponse reponse = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(corps)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);

            if (reponse == null || reponse.getAccessToken() == null) {
                throw new KeycloakIntegrationException(
                        "Réponse vide de Keycloak lors de l'obtention du token d'administration");
            }

            return reponse.getAccessToken();

        } catch (RestClientResponseException ex) {
            log.error("Échec d'obtention du token admin Keycloak : {}", ex.getMessage());
            throw new KeycloakIntegrationException(
                    "Impossible de s'authentifier auprès de Keycloak en tant qu'administrateur", ex);
        }
    }

    /**
     * Crée un nouvel utilisateur dans le Realm SEN-PRIX avec un mot de
     * passe temporaire, puis retourne l'identifiant Keycloak (UUID)
     * généré pour ce compte. L'identifiant est extrait de l'en-tête
     * "Location" de la réponse HTTP 201, conformément au comportement
     * standard de l'API Admin Keycloak.
     *
     * Pour les comptes ADMIN, l'action requise "CONFIGURE_TOTP" est
     * systématiquement ajoutée en plus du changement de mot de passe :
     * la double authentification est obligatoire pour ce rôle, et doit
     * donc être configurée dès la première connexion, sans dépendre
     * d'une intervention manuelle ultérieure dans la console Keycloak.
     */
    public String creerUtilisateur(String tokenAdmin, String username, String email,
                                     String nom, String prenom, String motDePasseTemporaire,
                                     RoleEnum role) {

        String url = properties.getServerUrl()
                + "/admin/realms/" + properties.getRealm() + "/users";

        List<String> actionsRequises = (role == RoleEnum.ADMIN)
                ? List.of("UPDATE_PASSWORD", "CONFIGURE_TOTP")
                : List.of("UPDATE_PASSWORD");

        KeycloakUserCreationPayload payload = KeycloakUserCreationPayload.builder()
                .username(username)
                .email(email)
                .firstName(prenom)
                .lastName(nom)
                .enabled(true)
                .emailVerified(true)
                .credentials(List.of(
                        KeycloakCredentialPayload.builder()
                                .value(motDePasseTemporaire)
                                .temporary(true)
                                .build()
                ))
                .requiredActions(actionsRequises)
                .build();

        try {
            var reponse = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + tokenAdmin)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            String location = reponse.getHeaders().getFirst("Location");
            if (location == null) {
                throw new KeycloakIntegrationException(
                        "Keycloak n'a pas retourné l'emplacement du compte créé");
            }

            // L'en-tête Location se termine par l'UUID Keycloak du nouvel utilisateur
            return location.substring(location.lastIndexOf('/') + 1);

        } catch (RestClientResponseException ex) {
            log.error("Échec de création de l'utilisateur Keycloak '{}' : {}", username, ex.getMessage());
            throw new KeycloakIntegrationException(
                    "Impossible de créer le compte Keycloak pour " + email, ex);
        }
    }

    /**
     * Assigne un rôle de Realm (ADMIN, AGENT_COLLECTE, CONSOMMATEUR) à un
     * utilisateur Keycloak déjà créé. Nécessite de récupérer au préalable
     * la représentation complète du rôle (id + name) auprès de Keycloak,
     * car l'API d'assignation attend ces deux informations.
     */
    public void assignerRole(String tokenAdmin, String keycloakUserId, RoleEnum role) {
        Map<String, Object> representationRole = obtenirRepresentationRole(tokenAdmin, role);

        String url = properties.getServerUrl()
                + "/admin/realms/" + properties.getRealm()
                + "/users/" + keycloakUserId + "/role-mappings/realm";

        try {
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + tokenAdmin)
                    .body(List.of(representationRole))
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException ex) {
            log.error("Échec d'assignation du rôle '{}' à l'utilisateur Keycloak '{}' : {}",
                    role, keycloakUserId, ex.getMessage());
            throw new KeycloakIntegrationException(
                    "Impossible d'assigner le rôle " + role + " au compte créé", ex);
        }
    }

    /**
     * Récupère la représentation JSON complète d'un rôle de Realm
     * (id, name, ...) nécessaire pour l'opération d'assignation.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> obtenirRepresentationRole(String tokenAdmin, RoleEnum role) {
        String url = properties.getServerUrl()
                + "/admin/realms/" + properties.getRealm()
                + "/roles/" + role.name();

        try {
            Map<String, Object> representation = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + tokenAdmin)
                    .retrieve()
                    .body(Map.class);

            if (representation == null) {
                throw new KeycloakIntegrationException(
                        "Le rôle " + role + " n'existe pas dans le Realm Keycloak " + properties.getRealm());
            }

            return representation;

        } catch (KeycloakIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Échec de récupération du rôle '{}' depuis Keycloak (URL: {}) : {} - {}",
                    role, url, ex.getClass().getSimpleName(), ex.getMessage());
            throw new KeycloakIntegrationException(
                    "Impossible de récupérer le rôle " + role + " depuis Keycloak : "
                            + ex.getClass().getSimpleName() + " - " + ex.getMessage(), ex);
        }
    }

    public void definirMotDePassePermanent(String tokenAdmin, String keycloakUserId, String motDePasse) {
        String url = properties.getServerUrl() + "/admin/realms/" + properties.getRealm()
                + "/users/" + keycloakUserId + "/reset-password";
        try {
            restClient.put()
                .uri(url)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tokenAdmin)
                .body(KeycloakCredentialPayload.builder()
                    .value(motDePasse)
                    .temporary(false)
                    .build())
                .retrieve()
                .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Impossible de définir le mot de passe permanent pour {} : {}", keycloakUserId, ex.getMessage());
        }
    }
}
