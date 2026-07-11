package sn.dci.senprix.rapport.exception;

/**
 * Exception levée lorsqu'une demande de génération de rapport est
 * invalide : produit ou marché inexistant, période sans aucun relevé
 * de prix, ou période incohérente (fin avant début).
 */
public class GenerationRapportException extends RuntimeException {

    public GenerationRapportException(String message) {
        super(message);
    }
}
