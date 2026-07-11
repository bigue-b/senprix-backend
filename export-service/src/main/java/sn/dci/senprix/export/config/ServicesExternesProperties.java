package sn.dci.senprix.export.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Regroupe les URLs de base des microservices distants appelés par
 * le export-service pour collecter (prix-service) et enrichir
 * (produit-service) les données exportées.
 */
@ConfigurationProperties(prefix = "services")
public class ServicesExternesProperties {

    private String prixServiceUrl;
    private String produitServiceUrl;

    public String getPrixServiceUrl() {
        return prixServiceUrl;
    }

    public void setPrixServiceUrl(String prixServiceUrl) {
        this.prixServiceUrl = prixServiceUrl;
    }

    public String getProduitServiceUrl() {
        return produitServiceUrl;
    }

    public void setProduitServiceUrl(String produitServiceUrl) {
        this.produitServiceUrl = produitServiceUrl;
    }
}
