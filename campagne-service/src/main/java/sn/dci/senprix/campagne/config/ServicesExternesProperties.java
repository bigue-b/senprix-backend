package sn.dci.senprix.campagne.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Regroupe les URLs de base des microservices distants appelés par
 * le campagne-service pour la validation croisée des affectations.
 * Valeurs injectées depuis application.yml (préfixe services).
 */
@ConfigurationProperties(prefix = "services")
public class ServicesExternesProperties {

    private String produitServiceUrl;
    private String userServiceUrl;

    public String getProduitServiceUrl() {
        return produitServiceUrl;
    }

    public void setProduitServiceUrl(String produitServiceUrl) {
        this.produitServiceUrl = produitServiceUrl;
    }

    public String getUserServiceUrl() {
        return userServiceUrl;
    }

    public void setUserServiceUrl(String userServiceUrl) {
        this.userServiceUrl = userServiceUrl;
    }
}
