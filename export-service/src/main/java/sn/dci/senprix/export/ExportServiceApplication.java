package sn.dci.senprix.export;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import sn.dci.senprix.export.config.ServicesExternesProperties;

/**
 * Point d'entrée du microservice Export de SEN-PRIX.
 * Génère à la volée des fichiers téléchargeables (CSV ou Excel)
 * contenant les relevés de prix d'un produit sur un marché, enrichis
 * avec les noms réels obtenus auprès du produit-service. Aucune
 * donnée n'est persistée — l'export est généré et transmis directement
 * en réponse à chaque demande.
 */
@SpringBootApplication
@EnableConfigurationProperties(ServicesExternesProperties.class)
public class ExportServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExportServiceApplication.class, args);
    }
}
