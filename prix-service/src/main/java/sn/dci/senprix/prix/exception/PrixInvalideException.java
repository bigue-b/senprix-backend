package sn.dci.senprix.prix.exception;

/**
 * Exception levée lorsqu'un relevé de prix est invalide : produit
 * inexistant, campagne inexistante, ou agent n'appartenant pas à
 * la campagne déclarée.
 */
public class PrixInvalideException extends RuntimeException {

    public PrixInvalideException(String message) {
        super(message);
    }
}
