package sn.dci.senprix.alerte.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sn.dci.senprix.alerte.config.ServicesExternesProperties;
import sn.dci.senprix.alerte.entity.Alerte;

import java.util.Map;

/**
 * Encapsule l'appel HTTP vers le notif-service afin d'avertir un
 * administrateur lorsqu'une nouvelle alerte de prix suspect est créée.
 *
 * Comme pour PrixEventPublisher dans prix-service, cet appel est
 * volontairement tolérant aux pannes : un échec de notification ne
 * doit jamais empêcher la création de l'alerte elle-même, qui reste
 * la donnée métier prioritaire.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotifServiceClient {

    private final RestClient restClient;
    private final ServicesExternesProperties properties;

    public void notifierNouvelleAlerte(Alerte alerte) {
        String url = properties.getNotifServiceUrl() + "/api/internal/notifications";

        Map<String, Object> requete = Map.of(
                "destinataireEmail", properties.getAdminNotificationEmail(),
                "type", "ALERTE_PRIX_SUSPECT",
                "variables", Map.of(
                        "produit", String.valueOf(alerte.getProduitId()),
                        "marche", String.valueOf(alerte.getMarcheId()),
                        "montant", alerte.getMontant().toString(),
                        "montantMoyen", alerte.getMontantMoyen().toString(),
                        "ecartPourcentage", alerte.getEcartPourcentage().toString()
                )
        );

        try {
            restClient.post()
                    .uri(url)
                    .body(requete)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Notification envoyée avec succès pour l'alerte {}", alerte.getId());

        } catch (Exception ex) {
            log.error("Échec de la notification du notif-service pour l'alerte {} : {} - {} "
                            + "— l'alerte reste néanmoins créée",
                    alerte.getId(), ex.getClass().getSimpleName(), ex.getMessage());
        }
    }
}