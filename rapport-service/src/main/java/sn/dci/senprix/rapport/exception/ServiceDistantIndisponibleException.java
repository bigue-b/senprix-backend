package sn.dci.senprix.rapport.exception;

/**
 * Exception levée lorsqu'un appel HTTP synchrone vers un service distant
 * (produit-service ou prix-service) échoue pour une raison technique
 * (service indisponible, timeout, erreur réseau).
 */
public class ServiceDistantIndisponibleException extends RuntimeException {

    public ServiceDistantIndisponibleException(String message) {
        super(message);
    }

    public ServiceDistantIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
