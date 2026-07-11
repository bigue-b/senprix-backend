package sn.dci.senprix.prix.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Client HTTP vers le notif-service pour déclencher des notifications
 * lors des décisions admin sur les relevés de prix.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotifServiceClient {

    private final RestClient restClient;

    @Value("${notif.service.url:http://localhost:8086}")
    private String notifServiceUrl;

    public void envoyerNotificationValidation(String emailAgent, String produit, String marche, String montant) {
        try {
            restClient.post()
                .uri(notifServiceUrl + "/api/internal/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                    "destinataireEmail", emailAgent,
                    "type", "ALERTE_PRIX_SUSPECT",
                    "variables", Map.of(
                        "produit", produit,
                        "marche", marche,
                        "montant", montant,
                        "statut", "VALIDE"
                    )
                ))
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Impossible d'envoyer la notification de validation : {}", e.getMessage());
        }
    }

    public void envoyerNotificationRejet(String emailAgent, String produit, String marche, String motif) {
        try {
            restClient.post()
                .uri(notifServiceUrl + "/api/internal/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                    "destinataireEmail", emailAgent,
                    "type", "ALERTE_PRIX_SUSPECT",
                    "variables", Map.of(
                        "produit", produit,
                        "marche", marche,
                        "montant", "0",
                        "statut", "REJETE",
                        "motif", motif != null ? motif : "Non spécifié"
                    )
                ))
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Impossible d'envoyer la notification de rejet : {}", e.getMessage());
        }
    }
}
