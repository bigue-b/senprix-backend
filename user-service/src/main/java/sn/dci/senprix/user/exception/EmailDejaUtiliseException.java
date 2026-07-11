package sn.dci.senprix.user.exception;

/**
 * Exception levée lorsqu'on tente de créer un utilisateur avec un email
 * déjà associé à un compte existant, côté SEN-PRIX ou côté Keycloak.
 */
public class EmailDejaUtiliseException extends RuntimeException {

    public EmailDejaUtiliseException(String email) {
        super("L'adresse email '" + email + "' est déjà associée à un compte existant");
    }
}
