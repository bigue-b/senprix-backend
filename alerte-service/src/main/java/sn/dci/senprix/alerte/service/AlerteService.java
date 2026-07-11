package sn.dci.senprix.alerte.service;

import sn.dci.senprix.alerte.dto.AlerteCreationRequest;
import sn.dci.senprix.alerte.dto.AlerteResponse;
import sn.dci.senprix.alerte.dto.ResolutionRequest;

import java.util.List;

/**
 * Contrat du service métier de gestion des alertes de prix suspects,
 * incluant le calcul automatique du niveau de gravité et la gestion
 * du cycle de vie NOUVELLE → EN_COURS → RESOLUE.
 */
public interface AlerteService {

    /**
     * Crée une nouvelle alerte à partir des informations transmises
     * par le prix-service. Calcule automatiquement l'écart en
     * pourcentage et le niveau de gravité correspondant.
     */
    AlerteResponse creer(AlerteCreationRequest request);

    AlerteResponse obtenirParId(Long id);

    List<AlerteResponse> listerToutes();

    List<AlerteResponse> listerParStatut(String statut);

    /**
     * Fait passer une alerte de NOUVELLE à EN_COURS, signalant qu'un
     * administrateur a commencé à la traiter.
     */
    AlerteResponse prendreEnCharge(Long id);

    /**
     * Fait passer une alerte de EN_COURS à RESOLUE, avec un commentaire
     * documentant la vérification effectuée.
     */
    AlerteResponse resoudre(Long id, ResolutionRequest request);
}
