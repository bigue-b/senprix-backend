package sn.dci.senprix.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie que les 8 routes définies dans application.yml (une par
 * microservice métier) sont correctement chargées par le contexte
 * Spring Cloud Gateway au démarrage, sans dépendre de la disponibilité
 * réelle des microservices ciblés (le chargement des routes ne
 * nécessite aucun appel réseau).
 */
@SpringBootTest
class GatewayRoutesConfigurationTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void toutesLesRoutesDesMicroservicesDevraientEtreChargees() {
        StepVerifier.create(routeLocator.getRoutes().collectList())
                .assertNext(routes -> {
                    var idsRoutes = routes.stream().map(route -> route.getId()).toList();

                    assertThat(idsRoutes).contains(
                            "user-service",
                            "produit-service",
                            "campagne-service",
                            "prix-service",
                            "alerte-service",
                            "notif-service",
                            "rapport-service",
                            "export-service"
                    );
                    assertThat(idsRoutes).hasSizeGreaterThanOrEqualTo(8);
                })
                .verifyComplete();
    }
}
