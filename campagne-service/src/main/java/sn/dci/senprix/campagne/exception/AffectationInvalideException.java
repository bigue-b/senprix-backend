package sn.dci.senprix.campagne.exception;

/**
 * Exception levée lorsqu'une affectation d'agent ou d'association de
 * marché est invalide : identifiant inexistant chez le service distant,
 * agent inactif, ou agent ne possédant pas le rôle AGENT_COLLECTE.
 */
public class AffectationInvalideException extends RuntimeException {

    public AffectationInvalideException(String message) {
        super(message);
    }
}
