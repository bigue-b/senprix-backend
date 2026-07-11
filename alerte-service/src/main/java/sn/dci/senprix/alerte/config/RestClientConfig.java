package sn.dci.senprix.alerte.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Fournit le RestClient utilisé pour notifier le notif-service lors
 * de la création d'une alerte. Utilise SimpleClientHttpRequestFactory
 * conformément à la convention déjà établie sur les autres microservices.
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