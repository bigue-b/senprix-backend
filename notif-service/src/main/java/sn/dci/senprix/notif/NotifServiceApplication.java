package sn.dci.senprix.notif;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du microservice Notif de SEN-PRIX.
 * Reçoit des autres microservices les demandes d'envoi de
 * notifications par email, génère le contenu à partir de modèles
 * prédéfinis, envoie le message via SMTP, et conserve un historique
 * complet des envois (réussis ou échoués) à des fins d'audit.
 */
@SpringBootApplication
public class NotifServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifServiceApplication.class, args);
    }
}
