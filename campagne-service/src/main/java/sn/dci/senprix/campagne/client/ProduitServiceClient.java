package sn.dci.senprix.campagne.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sn.dci.senprix.campagne.config.ServicesExternesProperties;
import sn.dci.senprix.campagne.exception.ServiceDistantIndisponibleException;

/**
 * Encapsule les appels HTTP synchrones vers l'API publique du
 * produit-service, utilisés pour valider l'existence d'un marché
 * avant de l'associer à une campagne (flux d'intégration inter-services
 * de l'architecture fonctionnelle SEN-PRIX).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProduitServiceClient {

    private final RestClient restClient;
    private final ServicesExternesProperties properties;

    /**
     * Vérifie l'existence d'un marché en interrogeant l'endpoint public
     * GET /api/public/marches/{id} du produit-service. Retourne true si
     * le marché existe (200), false s'il n'existe pas (404).
     */
    public boolean marcheExiste(Long marcheId) {
        String url = properties.getProduitServiceUrl() + "/api/public/marches/" + marcheId;
        try {
            restClient.get().uri(url).retrieve().toBodilessEntity();
            return true;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            return false;
        } catch (Exception ex) {
            log.error("Échec de vérification du marché {} : {}", marcheId, ex.getMessage());
            throw new ServiceDistantIndisponibleException(
                    "Le produit-service est momentanément indisponible pour valider le marché " + marcheId, ex);
        }
    }

    public boolean produitExiste(Long produitId) {
        String url = properties.getProduitServiceUrl() + "/api/public/produits/" + produitId;
        try {
            restClient.get().uri(url).retrieve().toBodilessEntity();
            return true;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            return false;
        } catch (Exception ex) {
            log.error("Échec de vérification du produit {} : {}", produitId, ex.getMessage());
            throw new ServiceDistantIndisponibleException(
                    "Le produit-service est momentanément indisponible pour valider le produit " + produitId, ex);
        }
    }
}
