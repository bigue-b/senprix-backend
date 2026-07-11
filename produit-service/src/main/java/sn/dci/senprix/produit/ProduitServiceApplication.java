package sn.dci.senprix.produit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du microservice Produit de SEN-PRIX.
 * Gère le référentiel officiel des produits de première nécessité
 * et des marchés couverts par les campagnes de collecte de la DCI.
 */
@SpringBootApplication
public class ProduitServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProduitServiceApplication.class, args);
    }
}
