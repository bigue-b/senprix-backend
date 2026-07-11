package sn.dci.senprix.rapport.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sn.dci.senprix.rapport.config.ServicesExternesProperties;
import sn.dci.senprix.rapport.exception.ServiceDistantIndisponibleException;

import java.util.List;

/**
 * Encapsule les appels HTTP synchrones vers l'API publique du
 * prix-service, utilisés pour récupérer les relevés de prix bruts
 * nécessaires au calcul des agrégations d'un rapport. Contrairement
 * à ProduitServiceClient (enrichissement non bloquant), un échec ici
 * empêche la génération du rapport — les données de prix sont la
 * matière première indispensable, pas un simple détail d'affichage.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PrixServiceClient {

    private final RestClient restClient;
    private final ServicesExternesProperties properties;

    public List<PrixDto> listerPrixParProduitEtMarche(Long produitId, Long marcheId) {
        String url = properties.getPrixServiceUrl()
                + "/api/public/prix?produitId=" + produitId + "&marcheId=" + marcheId;

        try {
            List<PrixDto> prix = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PrixDto>>() {});

            return (prix != null) ? prix : List.of();

        } catch (Exception ex) {
            log.error("Échec de récupération des relevés de prix (produit={}, marché={}) "
                            + "auprès du prix-service : {} - {}",
                    produitId, marcheId, ex.getClass().getSimpleName(), ex.getMessage());
            throw new ServiceDistantIndisponibleException(
                    "Le prix-service est momentanément indisponible pour générer ce rapport", ex);
        }
    }
}
