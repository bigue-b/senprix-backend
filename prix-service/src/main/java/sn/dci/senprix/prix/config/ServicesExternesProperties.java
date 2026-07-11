package sn.dci.senprix.prix.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services")
public class ServicesExternesProperties {

    private String produitServiceUrl;
    private String campagneServiceUrl;
    private String alerteServiceUrl;

    public String getProduitServiceUrl() { return produitServiceUrl; }
    public void setProduitServiceUrl(String produitServiceUrl) { this.produitServiceUrl = produitServiceUrl; }

    public String getCampagneServiceUrl() { return campagneServiceUrl; }
    public void setCampagneServiceUrl(String campagneServiceUrl) { this.campagneServiceUrl = campagneServiceUrl; }

    public String getAlerteServiceUrl() { return alerteServiceUrl; }
    public void setAlerteServiceUrl(String alerteServiceUrl) { this.alerteServiceUrl = alerteServiceUrl; }
}