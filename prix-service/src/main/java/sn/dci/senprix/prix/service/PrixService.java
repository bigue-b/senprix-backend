package sn.dci.senprix.prix.service;

import sn.dci.senprix.prix.dto.PrixRequest;
import sn.dci.senprix.prix.dto.PrixResponse;
import sn.dci.senprix.prix.dto.StatistiquePrixResponse;
import sn.dci.senprix.prix.enums.StatutPrix;

import java.util.List;

/**
 * Contrat du service métier de gestion des relevés de prix, incluant
 * la validation croisée auprès du produit-service et du campagne-service,
 * la détection de variations anormales, et le calcul de statistiques
 * agrégées par produit et marché.
 */
public interface PrixService {

    /**
     * Enregistre un nouveau relevé de prix, après avoir vérifié auprès
     * du produit-service que le produit existe, et auprès du
     * campagne-service que la campagne existe et que l'agent déclaré
     * lui est bien affecté.
     */
    PrixResponse creer(PrixRequest request);

    PrixResponse obtenirParId(Long id);

    List<PrixResponse> listerTous();

    List<PrixResponse> listerParProduitEtMarche(Long produitId, Long marcheId);

    List<PrixResponse> listerParCampagne(Long campagneId);

    List<PrixResponse> listerParAgent(Long agentId);

    StatistiquePrixResponse calculerStatistiques(Long produitId, Long marcheId);

    PrixResponse changerStatut(Long id, StatutPrix nouveauStatut);

    void supprimer(Long id);
}
