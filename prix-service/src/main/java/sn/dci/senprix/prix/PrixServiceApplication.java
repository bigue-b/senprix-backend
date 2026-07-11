package sn.dci.senprix.prix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import sn.dci.senprix.prix.config.ServicesExternesProperties;
/**
 * Point d'entrée du microservice Prix de SEN-PRIX.
 * Gère l'enregistrement des relevés de prix par les agents de collecte,
 * leur validation croisée en temps réel avec le produit-service et le
 * campagne-service, la détection de variations anormales, et le calcul
 * de statistiques agrégées exposées publiquement.
 */
@SpringBootApplication
@EnableConfigurationProperties(ServicesExternesProperties.class)
public class PrixServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrixServiceApplication.class, args);
    }
}
