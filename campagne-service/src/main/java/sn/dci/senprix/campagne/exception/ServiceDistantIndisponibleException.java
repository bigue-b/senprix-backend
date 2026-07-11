package sn.dci.senprix.campagne.exception;

/**
 * Exception levée lorsqu'un appel HTTP synchrone vers un service distant
 * (produit-service ou user-service) échoue pour une raison technique
 * (service indisponible, timeout, erreur réseau), distincte du cas où
 * la ressource demandée n'existe simplement pas (404).
 */
public class ServiceDistantIndisponibleException extends RuntimeException {

    public ServiceDistantIndisponibleException(String message) {
        super(message);
    }

    public ServiceDistantIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
