package sn.dci.senprix.notif.exception;

/**
 * Exception levée lorsqu'une notification demandée n'existe pas en base.
 */
public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(Long id) {
        super("Notification introuvable avec l'identifiant : " + id);
    }
}
