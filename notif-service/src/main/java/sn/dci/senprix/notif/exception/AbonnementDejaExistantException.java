package sn.dci.senprix.notif.exception;

public class AbonnementDejaExistantException extends RuntimeException {
    public AbonnementDejaExistantException(Long produitId, Long marcheId) {
        super("Vous êtes déjà abonné au produit " + produitId + " sur le marché " + marcheId);
    }
}
