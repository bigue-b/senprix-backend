package sn.dci.senprix.prix.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import sn.dci.senprix.prix.exception.ServiceDistantIndisponibleException;
import sn.dci.senprix.prix.config.ServicesExternesProperties;
/**
 * Encapsule les appels HTTP synchrones vers l'API publique du
 * produit-service, utilisés pour valider l'existence d'un produit
 * avant l'enregistrement d'un relevé de prix.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProduitServiceClient {

    private final RestClient restClient;
    private final ServicesExternesProperties properties;

    /**
     * Vérifie l'existence d'un produit en interrogeant l'endpoint public
     * GET /api/public/produits/{id} du produit-service. Retourne true si
     * le produit existe (200), false s'il n'existe pas (404).
     */
    public boolean produitExiste(Long produitId) {
        String url = properties.getProduitServiceUrl() + "/api/public/produits/" + produitId;

        try {
            restClient.get()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity();
            return true;

        } catch (HttpClientErrorException.NotFound ex) {
            return false;

        } catch (Exception ex) {
            log.error("Échec de vérification du produit {} auprès du produit-service : {} - {}",
                    produitId, ex.getClass().getSimpleName(), ex.getMessage());
            throw new ServiceDistantIndisponibleException(
                    "Le produit-service est momentanément indisponible pour valider le produit " + produitId, ex);
        }
    }

    public String obtenirNomProduit(Long produitId) {
        try {
            String url = properties.getProduitServiceUrl() + "/api/public/produits/" + produitId;
            var body = restClient.get().uri(url).retrieve().body(java.util.Map.class);
            return body != null && body.get("nom") != null ? body.get("nom").toString() : "Produit #" + produitId;
        } catch (Exception e) {
            return "Produit #" + produitId;
        }
    }

    public String obtenirNomMarche(Long marcheId) {
        try {
            String url = properties.getProduitServiceUrl() + "/api/public/marches/" + marcheId;
            var body = restClient.get().uri(url).retrieve().body(java.util.Map.class);
            return body != null && body.get("nom") != null ? body.get("nom").toString() : "Marché #" + marcheId;
        } catch (Exception e) {
            return "Marché #" + marcheId;
        }
    }
}
