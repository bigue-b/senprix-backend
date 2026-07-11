package sn.dci.senprix.export.exception;

/**
 * Exception levée lorsqu'une demande d'export ne correspond à aucune
 * donnée exportable (aucun relevé de prix trouvé pour les critères
 * demandés).
 */
public class ExportImpossibleException extends RuntimeException {

    public ExportImpossibleException(String message) {
        super(message);
    }
}
