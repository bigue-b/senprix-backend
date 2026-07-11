package sn.dci.senprix.campagne.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Fournit le RestClient utilisé pour les appels HTTP synchrones vers
 * produit-service et user-service lors de la validation croisée des
 * affectations de campagnes.
 *
 * Utilise SimpleClientHttpRequestFactory plutôt que le client par défaut
 * basé sur un pool de connexions persistantes, conformément à la leçon
 * tirée du débogage du user-service (instabilité "Connection reset"
 * observée avec des connexions HTTP/1.1 keep-alive réutilisées).
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
