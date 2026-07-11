package sn.dci.senprix.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Fournit le RestClient utilisé par KeycloakAdminClient pour effectuer
 * les appels HTTP vers l'API Admin de Keycloak (obtention de token,
 * création d'utilisateur, assignation de rôle).
 *
 * Utilise SimpleClientHttpRequestFactory (basé sur HttpURLConnection)
 * plutôt que le client par défaut basé sur un pool de connexions
 * persistantes : Keycloak en mode développement (start-dev) ferme parfois
 * les connexions HTTP/1.1 keep-alive de façon imprévisible, ce qui cause
 * des erreurs sporadiques "Connection reset" lorsque le pool tente de
 * réutiliser une connexion déjà fermée côté serveur.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
