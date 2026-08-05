package sn.dci.senprix.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie que le contexte de l'annuaire Eureka démarre correctement et
 * que le serveur Eureka est bien activé (bean d'amorçage présent), sans
 * ouvrir de port réseau ni contacter de pair distant.
 */
@SpringBootTest(properties = {
        "server.port=0",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class DiscoveryServiceApplicationTest {

    @Autowired
    private ApplicationContext contexte;

    @Test
    void leContexteDuServeurEurekaDevraitDemarrer() {
        assertThat(contexte).isNotNull();
        assertThat(contexte.containsBean("eurekaServerBootstrap")).isTrue();
    }
}
