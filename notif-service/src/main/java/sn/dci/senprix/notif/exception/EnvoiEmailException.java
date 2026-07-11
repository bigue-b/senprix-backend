package sn.dci.senprix.notif.exception;

/**
 * Exception levée lorsque l'envoi effectif d'un email échoue
 * (serveur SMTP injoignable, identifiants invalides, etc.). Cette
 * exception est interceptée par NotificationServiceImpl : la
 * notification est tout de même persistée avec le statut ECHEC
 * plutôt que de faire échouer toute la requête.
 */
public class EnvoiEmailException extends RuntimeException {

    public EnvoiEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
