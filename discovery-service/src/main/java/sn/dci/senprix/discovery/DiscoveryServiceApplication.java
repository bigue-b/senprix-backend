package sn.dci.senprix.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Annuaire de services SEN-PRIX (Eureka Server).
 *
 * Chaque microservice s'enregistre ici au démarrage sous son
 * spring.application.name ; la gateway interroge cet annuaire pour
 * router dynamiquement (lb://NOM-SERVICE) sans connaître les adresses
 * ni les ports des instances.
 *
 * Tableau de bord : http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServiceApplication.class, args);
    }
}
