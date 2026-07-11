package sn.dci.senprix.user.exception;

/**
 * Exception levée lorsqu'un utilisateur demandé n'existe pas en base.
 */
public class UtilisateurNotFoundException extends RuntimeException {

    public UtilisateurNotFoundException(Long id) {
        super("Utilisateur introuvable avec l'identifiant : " + id);
    }

    public UtilisateurNotFoundException(String message) {
        super(message);
    }
}
