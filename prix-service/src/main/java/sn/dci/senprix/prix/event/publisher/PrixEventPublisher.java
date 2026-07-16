package sn.dci.senprix.prix.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import sn.dci.senprix.prix.config.RabbitMQConfig;
import sn.dci.senprix.prix.entity.Prix;

import java.math.BigDecimal;

/**
 * Publie une alerte auprès du alerte-service lorsqu'un relevé de prix
 * est détecté comme SUSPECT (variation anormale par rapport à la
 * moyenne du marché).
 *
 * La notification vers alerte-service est volontairement tolérante aux
 * pannes : si la publication RabbitMQ échoue (broker injoignable, etc.),
 * l'échec est journalisé mais ne remonte jamais vers PrixServiceImpl. La
 * priorité métier est de toujours enregistrer le relevé de prix lui-même ;
 * la création de l'alerte associée est une action secondaire qui ne doit
 * jamais faire échouer la transaction principale.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PrixEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publierPrixSuspect(Prix prix, double moyenneMarche) {
        log.warn("Prix suspect détecté : produit={}, marché={}, montant={}, moyenne marché={} — "
                        + "écart significatif, notification de alerte-service en cours",
                prix.getProduitId(), prix.getMarcheId(), prix.getMontant(), moyenneMarche);

        PrixSuspectMessage message = new PrixSuspectMessage(
                prix.getId(),
                prix.getProduitId(),
                prix.getMarcheId(),
                prix.getMontant(),
                BigDecimal.valueOf(moyenneMarche)
        );

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_PRIX_SUSPECT,
                    RabbitMQConfig.ROUTING_KEY_PRIX_SUSPECT,
                    message);

        } catch (Exception ex) {
            log.error("Échec de la publication RabbitMQ vers alerte-service pour le prix {} : {} - {} "
                            + "— le relevé de prix reste néanmoins enregistré",
                    prix.getId(), ex.getClass().getSimpleName(), ex.getMessage());
        }
    }
}
