package sn.dci.senprix.rapport.exception;

/**
 * Exception levée lorsqu'un rapport demandé n'existe pas en base.
 */
public class RapportNotFoundException extends RuntimeException {

    public RapportNotFoundException(Long id) {
        super("Rapport introuvable avec l'identifiant : " + id);
    }
}
