package sn.dci.senprix.gateway.integration;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie que la gateway route correctement une requête entrante vers
 * un service backend, en utilisant un serveur HTTP JDK minimal comme
 * cible de route plutôt qu'un véritable microservice — suffisant pour
 * valider le mécanisme de routage de Spring Cloud Gateway lui-même,
 * sans dépendre de la disponibilité d'un microservice réel pendant
 * les tests automatisés.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GatewayRoutingIntegrationTest {

    private static HttpServer serveurFactice;
    private static int portServeurFactice;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void demarrerServeurFactice() throws IOException {
        serveurFactice = HttpServer.create(new InetSocketAddress(0), 0);
        portServeurFactice = serveurFactice.getAddress().getPort();

        serveurFactice.createContext("/api/public/test/ping", exchange -> {
            byte[] reponse = "{\"message\":\"pong\"}".getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, reponse.length);
            exchange.getResponseBody().write(reponse);
            exchange.close();
        });

        serveurFactice.start();
        System.setProperty("wiremock.server.port", String.valueOf(portServeurFactice));
    }

    @AfterAll
    static void arreterServeurFactice() {
        if (serveurFactice != null) {
            serveurFactice.stop(0);
        }
    }

    // La route de test (id, uri, predicates) est définie dans application-test.yml,
    // avec l'URI paramétrée par la propriété système "wiremock.server.port" positionnée
    // ci-dessus. Un @DynamicPropertySource qui ne fixerait que "routes[0].uri" ici
    // écraserait toute la liste "routes" (id et predicates inclus) : Spring Boot ne
    // fusionne pas les éléments d'une liste indexée entre sources de configuration,
    // la source la plus prioritaire remplace la liste entière.

    @Test
    void requeteVersChemainConnu_devraitEtreRouteeVersLeServiceBackend() {
        webTestClient.get()
                .uri("/api/public/test/ping")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("pong");
    }

    @Test
    void requeteVersCheminInconnu_devraitRetourner404() {
        webTestClient.get()
                .uri("/api/public/chemin-inexistant")
                .exchange()
                .expectStatus().isNotFound();
    }
}
