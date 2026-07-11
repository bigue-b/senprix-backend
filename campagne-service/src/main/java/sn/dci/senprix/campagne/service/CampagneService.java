package sn.dci.senprix.campagne.service;

import sn.dci.senprix.campagne.dto.CampagneRequest;
import sn.dci.senprix.campagne.dto.CampagneResponse;

import java.util.List;

/**
 * Contrat du service métier de gestion des campagnes de collecte de prix,
 * incluant l'affectation d'agents et l'association de marchés, validées
 * en temps réel auprès du user-service et du produit-service.
 */
public interface CampagneService {

    CampagneResponse creer(CampagneRequest request);

    CampagneResponse modifier(Long id, CampagneRequest request);

    CampagneResponse obtenirParId(Long id);

    List<CampagneResponse> listerToutes();

    /**
     * Affecte un agent de collecte à une campagne, après avoir vérifié
     * auprès du user-service que l'agent existe, est actif, et possède
     * bien le rôle AGENT_COLLECTE.
     */
    CampagneResponse affecterAgent(Long campagneId, Long agentId);

    void retirerAgent(Long campagneId, Long agentId);

    /**
     * Associe un marché à une campagne, après avoir vérifié auprès du
     * produit-service que le marché existe.
     */
    CampagneResponse associerMarche(Long campagneId, Long marcheId);

    void dissocierMarche(Long campagneId, Long marcheId);

    CampagneResponse associerProduit(Long campagneId, Long produitId);

    void dissocierProduit(Long campagneId, Long produitId);

    CampagneResponse demarrer(Long id);

    CampagneResponse terminer(Long id);

    CampagneResponse annuler(Long id);
}
