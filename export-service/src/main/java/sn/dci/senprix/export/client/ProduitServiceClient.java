package sn.dci.senprix.export.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sn.dci.senprix.export.config.ServicesExternesProperties;

/**
 * Encapsule les appels HTTP synchrones vers l'API publique du
 * produit-service, utilisés pour enrichir les fichiers d'export avec
 * les noms réels des produits et marchés plutôt que leurs seuls
 * identifiants.
 *
 * Comme pour rapport-service, cet enrichissement est non bloquant :
 * si produit-service est indisponible, l'export est tout de même
 * généré avec un nom de repli générique ("Produit #1").
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProduitServiceClient {

    private final RestClient restClient;
    private final ServicesExternesProperties properties;

    public String obtenirNomProduit(Long produitId) {
        String url = properties.getProduitServiceUrl() + "/api/public/produits/" + produitId;

        try {
            ProduitDto produit = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(ProduitDto.class);

            return (produit != null && produit.getNom() != null)
                    ? produit.getNom()
                    : "Produit #" + produitId;

        } catch (Exception ex) {
            log.warn("Échec de récupération du nom du produit {} auprès du produit-service : {} - {} "
                            + "— utilisation d'un nom de repli",
                    produitId, ex.getClass().getSimpleName(), ex.getMessage());
            return "Produit #" + produitId;
        }
    }

    public String obtenirNomMarche(Long marcheId) {
        String url = properties.getProduitServiceUrl() + "/api/public/marches/" + marcheId;

        try {
            MarcheDto marche = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(MarcheDto.class);

            return (marche != null && marche.getNom() != null)
                    ? marche.getNom()
                    : "Marché #" + marcheId;

        } catch (Exception ex) {
            log.warn("Échec de récupération du nom du marché {} auprès du produit-service : {} - {} "
                            + "— utilisation d'un nom de repli",
                    marcheId, ex.getClass().getSimpleName(), ex.getMessage());
            return "Marché #" + marcheId;
        }
    }
}
