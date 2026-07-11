package sn.dci.senprix.alerte.exception;

/**
 * Exception levée lorsqu'une alerte demandée n'existe pas en base.
 */
public class AlerteNotFoundException extends RuntimeException {

    public AlerteNotFoundException(Long id) {
        super("Alerte introuvable avec l'identifiant : " + id);
    }
}
