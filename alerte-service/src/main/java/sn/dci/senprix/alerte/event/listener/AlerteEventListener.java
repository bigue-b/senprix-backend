package sn.dci.senprix.alerte.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import sn.dci.senprix.alerte.config.RabbitMQConfig;
import sn.dci.senprix.alerte.dto.AlerteCreationRequest;
import sn.dci.senprix.alerte.service.AlerteService;

/**
 * Consomme les messages publiés par prix-service sur la queue
 * "alerte.queue" lorsqu'un relevé de prix suspect est détecté, et
 * déclenche la création de l'alerte correspondante — remplace l'ancien
 * appel entrant sur POST /api/internal/alertes.
 *
 * Les exceptions levées pendant le traitement sont interceptées et
 * journalisées plutôt que de remonter : sans dead-letter queue en place,
 * laisser l'exception se propager provoquerait un rejeu en boucle du
 * même message par RabbitMQ (nack + requeue par défaut).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlerteEventListener {

    private final AlerteService alerteService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ALERTE)
    public void recevoirPrixSuspect(AlerteCreationRequest request) {
        log.info("Message reçu depuis la file '{}' : prix suspect pour le produit {} (prixId={}, montant={}, moyenne={})",
                RabbitMQConfig.QUEUE_ALERTE, request.getProduitId(), request.getPrixId(),
                request.getMontant(), request.getMontantMoyen());

        try {
            alerteService.creer(request);
        } catch (Exception ex) {
            log.error("Échec de la création de l'alerte pour le prix {} : {} - {}",
                    request.getPrixId(), ex.getClass().getSimpleName(), ex.getMessage());
        }
    }
}
