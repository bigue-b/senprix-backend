package sn.dci.senprix.produit.exception;

/**
 * Exception levée lorsqu'un marché demandé n'existe pas en base.
 */
public class MarcheNotFoundException extends RuntimeException {

    public MarcheNotFoundException(Long id) {
        super("Marché introuvable avec l'identifiant : " + id);
    }
}
