package sn.dci.senprix.prix.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sn.dci.senprix.prix.entity.Prix;
import sn.dci.senprix.prix.config.ServicesExternesProperties;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Publie une alerte auprès du alerte-service lorsqu'un relevé de prix
 * est détecté comme SUSPECT (variation anormale par rapport à la
 * moyenne du marché).
 *
 * L'appel vers alerte-service est volontairement tolérant aux pannes :
 * si alerte-service est indisponible, l'échec est journalisé mais ne
 * remonte jamais vers PrixServiceImpl. La priorité métier est de
 * toujours enregistrer le relevé de prix lui-même ; la création de
 * l'alerte associée est une action secondaire qui ne doit jamais faire
 * échouer la transaction principale.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PrixEventPublisher {

    private final RestClient restClient;
    private final ServicesExternesProperties properties;

    public void publierPrixSuspect(Prix prix, double moyenneMarche) {
        log.warn("Prix suspect détecté : produit={}, marché={}, montant={}, moyenne marché={} — "
                        + "écart significatif, notification de alerte-service en cours",
                prix.getProduitId(), prix.getMarcheId(), prix.getMontant(), moyenneMarche);

        String url = properties.getAlerteServiceUrl() + "/api/internal/alertes";

        Map<String, Object> requete = Map.of(
                "prixId", prix.getId(),
                "produitId", prix.getProduitId(),
                "marcheId", prix.getMarcheId(),
                "montant", prix.getMontant(),
                "montantMoyen", BigDecimal.valueOf(moyenneMarche)
        );

        try {
            restClient.post()
                    .uri(url)
                    .body(requete)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception ex) {
            log.error("Échec de la notification du alerte-service pour le prix {} : {} - {} "
                            + "— le relevé de prix reste néanmoins enregistré",
                    prix.getId(), ex.getClass().getSimpleName(), ex.getMessage());
        }
    }
}