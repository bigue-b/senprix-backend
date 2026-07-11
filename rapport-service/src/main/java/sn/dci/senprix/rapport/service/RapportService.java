package sn.dci.senprix.rapport.service;

import sn.dci.senprix.rapport.dto.GenerationRapportRequest;
import sn.dci.senprix.rapport.dto.RapportResponse;
import sn.dci.senprix.rapport.dto.RapportResumeResponse;

import java.util.List;

/**
 * Contrat du service métier de génération et consultation de rapports
 * d'évolution de prix, enrichis avec les noms réels des produits et
 * marchés et conservés en base pour consultation ultérieure sans
 * recalcul.
 */
public interface RapportService {

    /**
     * Génère un nouveau rapport d'évolution de prix pour un produit
     * sur un marché donné, sur une période donnée. Récupère les
     * relevés bruts auprès du prix-service, calcule les agrégations
     * (moyenne, min, max), enrichit avec les noms réels obtenus
     * auprès du produit-service, puis persiste le résultat.
     */
    RapportResponse genererSyntheseProduitMarche(GenerationRapportRequest request);

    RapportResponse obtenirParId(Long id);

    List<RapportResumeResponse> listerTous();
}
