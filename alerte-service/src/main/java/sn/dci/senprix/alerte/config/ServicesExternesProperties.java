package sn.dci.senprix.alerte.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Regroupe les URLs de base des microservices distants appelés par
 * le alerte-service, notamment notif-service pour la notification
 * des administrateurs lors de la création d'une alerte.
 */
@ConfigurationProperties(prefix = "services")
public class ServicesExternesProperties {

    private String notifServiceUrl;
    private String adminNotificationEmail;

    public String getNotifServiceUrl() {
        return notifServiceUrl;
    }

    public void setNotifServiceUrl(String notifServiceUrl) {
        this.notifServiceUrl = notifServiceUrl;
    }

    public String getAdminNotificationEmail() {
        return adminNotificationEmail;
    }

    public void setAdminNotificationEmail(String adminNotificationEmail) {
        this.adminNotificationEmail = adminNotificationEmail;
    }
}