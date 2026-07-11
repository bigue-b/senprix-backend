package sn.dci.senprix.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du Gateway de SEN-PRIX.
 * Sert de point d'entrée HTTP unique pour l'ensemble des 8
 * microservices métier, routant chaque requête vers le service
 * approprié selon le préfixe de chemin demandé. Construit sur
 * Spring Cloud Gateway (réactif/WebFlux), seul service du projet
 * sur ce paradigme — les autres microservices restent bloquants
 * (Spring MVC), le routage pur ne nécessitant pas de logique métier
 * justifiant ce changement de modèle de programmation.
 */
@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
