package sn.dci.senprix.alerte.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriétés configurables pour les seuils d'alerte.
 * Modifiables via application.yml ou variables d'environnement.
 */
@Component
@ConfigurationProperties(prefix = "senprix.alertes")
@Getter
@Setter
public class SeuilAlertesProperties {

    /** Seuil en pourcentage pour déclencher une alerte de gravité MOYENNE (défaut: 100%) */
    private double seuilMoyenne = 100.0;

    /** Seuil en pourcentage pour déclencher une alerte de gravité ELEVEE (défaut: 200%) */
    private double seuilEleve = 200.0;
}
