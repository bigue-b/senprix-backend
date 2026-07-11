package sn.dci.senprix.produit.exception;

/**
 * Exception levée lorsqu'un produit demandé n'existe pas en base.
 */
public class ProduitNotFoundException extends RuntimeException {

    public ProduitNotFoundException(Long id) {
        super("Produit introuvable avec l'identifiant : " + id);
    }
}
