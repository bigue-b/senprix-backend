package sn.dci.senprix.export.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Représente la réponse JSON renvoyée par l'endpoint public
 * GET /api/public/produits/{id} du produit-service. Seuls les champs
 * nécessaires à l'enrichissement des fichiers d'export sont mappés.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProduitDto {

    private Long id;
    private String nom;
}
