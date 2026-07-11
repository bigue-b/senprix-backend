package sn.dci.senprix.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Regroupe les propriétés nécessaires au KeycloakAdminClient pour
 * s'authentifier auprès de l'API Admin de Keycloak et cibler le bon Realm.
 * Valeurs injectées depuis application.yml (préfixe keycloak.admin).
 * Enregistrée comme bean via @EnableConfigurationProperties sur la classe
 * principale de l'application, pas via @Configuration directement ici,
 * pour éviter un double mécanisme d'enregistrement.
 */
@ConfigurationProperties(prefix = "keycloak.admin")
public class KeycloakAdminProperties {

    /** URL de base du serveur Keycloak, ex: http://localhost:8080 */
    private String serverUrl;

    /** Realm cible dans lequel les utilisateurs SEN-PRIX sont créés */
    private String realm;

    /** Realm utilisé pour obtenir le token d'administration (souvent "master") */
    private String adminRealm;

    /** Identifiant du client Keycloak disposant des droits d'administration */
    private String clientId;

    /** Secret du client Keycloak d'administration */
    private String clientSecret;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getAdminRealm() {
        return adminRealm;
    }

    public void setAdminRealm(String adminRealm) {
        this.adminRealm = adminRealm;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
