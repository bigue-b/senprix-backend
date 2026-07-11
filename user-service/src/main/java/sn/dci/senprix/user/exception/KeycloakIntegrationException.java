package sn.dci.senprix.user.exception;

/**
 * Exception levée lorsqu'un appel à l'API Admin de Keycloak échoue
 * (authentification du client admin, création de compte, assignation
 * de rôle...). Encapsule l'erreur technique pour ne pas exposer les
 * détails internes de l'intégration Keycloak à l'appelant de l'API.
 */
public class KeycloakIntegrationException extends RuntimeException {

    public KeycloakIntegrationException(String message) {
        super(message);
    }

    public KeycloakIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
