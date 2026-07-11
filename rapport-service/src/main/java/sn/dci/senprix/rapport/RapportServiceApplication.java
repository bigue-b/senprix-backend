package sn.dci.senprix.rapport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import sn.dci.senprix.rapport.config.ServicesExternesProperties;

/**
 * Point d'entrée du microservice Rapport de SEN-PRIX.
 * Génère des rapports d'évolution de prix en agrégeant les relevés
 * bruts du prix-service et en les enrichissant avec les noms réels
 * obtenus auprès du produit-service, puis conserve ces rapports en
 * base pour consultation ultérieure sans recalcul.
 */
@SpringBootApplication
@EnableConfigurationProperties(ServicesExternesProperties.class)
public class RapportServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RapportServiceApplication.class, args);
    }
}
