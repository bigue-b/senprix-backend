package sn.dci.senprix.alerte.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Fournit le RestClient utilisé pour notifier le notif-service lors
 * de la création d'une alerte. Utilise SimpleClientHttpRequestFactory
 * conformément à la convention déjà établie sur les autres microservices.
 *
 * Le builder est annoté @LoadBalanced : les URLs de la forme
 * http://NOTIF-SERVICE/... voient leur hôte résolu à chaud via l'annuaire
 * Eureka, avec répartition entre les instances disponibles. Aucune
 * adresse ni port n'est donc codé en dur. (La syntaxe lb:// est réservée
 * aux routes de la Gateway ; côté client REST, c'est le nom du service
 * qui prend la place de l'hôte.)
 */
@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        return RestClient.builder()
                .requestFactory(factory);
    }

    @Bean
    public RestClient restClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder.build();
    }
}