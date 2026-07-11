package sn.dci.senprix.alerte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import sn.dci.senprix.alerte.config.ServicesExternesProperties;

/**
 * Point d'entrée du microservice Alerte de SEN-PRIX.
 * Reçoit du prix-service les signalements de relevés de prix suspects,
 * calcule automatiquement leur niveau de gravité, notifie les
 * administrateurs via le notif-service, et gère leur cycle de vie de
 * traitement (NOUVELLE → EN_COURS → RESOLUE).
 */
@SpringBootApplication
@EnableConfigurationProperties(ServicesExternesProperties.class)
public class AlerteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlerteServiceApplication.class, args);
    }
}