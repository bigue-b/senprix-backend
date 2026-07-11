package sn.dci.senprix.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import sn.dci.senprix.user.config.KeycloakAdminProperties;

/**
 * Point d'entrée du microservice User de SEN-PRIX.
 * Gère la création et l'administration des comptes agents de collecte
 * et administrateurs DCI, en synchronisation avec Keycloak via son
 * API d'administration.
 */
@SpringBootApplication
@EnableConfigurationProperties(KeycloakAdminProperties.class)
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
