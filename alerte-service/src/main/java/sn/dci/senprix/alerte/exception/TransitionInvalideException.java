package sn.dci.senprix.alerte.exception;

/**
 * Exception levée lorsqu'une transition de statut demandée sur une
 * alerte n'est pas autorisée par le cycle de vie défini
 * (NOUVELLE → EN_COURS → RESOLUE), par exemple résoudre directement
 * une alerte déjà résolue, ou reprendre en charge une alerte résolue.
 */
public class TransitionInvalideException extends RuntimeException {

    public TransitionInvalideException(String message) {
        super(message);
    }
}
