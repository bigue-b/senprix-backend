package sn.dci.senprix.produit.service;

import sn.dci.senprix.produit.dto.ProduitRequest;
import sn.dci.senprix.produit.dto.ProduitResponse;

import java.util.List;

/**
 * Contrat du service métier de gestion des produits.
 */
public interface ProduitService {

    ProduitResponse creer(ProduitRequest request);

    ProduitResponse modifier(Long id, ProduitRequest request);

    ProduitResponse obtenirParId(Long id);

    List<ProduitResponse> listerTous();

    List<ProduitResponse> listerActifs();

    void desactiver(Long id);

    void activer(Long id);
}
