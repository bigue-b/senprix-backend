package sn.dci.senprix.prix.exception;

/**
 * Exception levée lorsqu'un relevé de prix demandé n'existe pas en base.
 */
public class PrixNotFoundException extends RuntimeException {

    public PrixNotFoundException(Long id) {
        super("Relevé de prix introuvable avec l'identifiant : " + id);
    }
}
