package sn.dci.senprix.campagne;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import sn.dci.senprix.campagne.config.ServicesExternesProperties;

/**
 * Point d'entrée du microservice Campagne de SEN-PRIX.
 * Gère la création de campagnes de collecte de prix, l'affectation
 * d'agents de collecte et l'association de marchés, en validation
 * croisée en temps réel avec le user-service et le produit-service.
 */
@SpringBootApplication
@EnableConfigurationProperties(ServicesExternesProperties.class)
public class CampagneServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampagneServiceApplication.class, args);
    }
}
