package sn.dci.senprix.prix.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sn.dci.senprix.prix.config.ServicesExternesProperties;
import sn.dci.senprix.prix.entity.Prix;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Encapsule l'appel HTTP synchrone vers l'endpoint interne du
 * alerte-service (/api/internal/alertes), déclenché lorsque le
 * prix-service détecte un relevé de prix dont le montant s'écarte
 * significativement de la moyenne déjà validée pour ce produit/marché.
 *
 * Endpoint interne, sans authentification, suivant le même principe
 * que les endpoints publics/internes des autres microservices.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlerteServiceClient {

    private final RestClient restClient;
    private final ServicesExternesProperties properties;

    public void creerAlerte(Prix prix, BigDecimal montantMoyen) {
        String url = properties.getAlerteServiceUrl() + "/api/internal/alertes";

        Map<String, Object> body = Map.of(
                "prixId", prix.getId(),
                "produitId", prix.getProduitId(),
                "marcheId", prix.getMarcheId(),
                "montant", prix.getMontant(),
                "montantMoyen", montantMoyen
        );

        try {
            restClient.post()
                    .uri(url)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Alerte créée avec succès pour le prix {} (produit={}, marché={}, montant={}, moyenne={})",
                    prix.getId(), prix.getProduitId(), prix.getMarcheId(), prix.getMontant(), montantMoyen);

        } catch (Exception ex) {
            // On ne bloque jamais la validation d'un relevé si alerte-service est
            // indisponible : l'échec de notification est journalisé, pas propagé.
            log.error("Échec de création de l'alerte auprès du alerte-service pour le prix {} : {} - {}",
                    prix.getId(), ex.getClass().getSimpleName(), ex.getMessage());
        }
    }
}
