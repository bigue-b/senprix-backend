package sn.dci.senprix.campagne.exception;

/**
 * Exception levée lorsqu'une campagne demandée n'existe pas en base.
 */
public class CampagneNotFoundException extends RuntimeException {

    public CampagneNotFoundException(Long id) {
        super("Campagne introuvable avec l'identifiant : " + id);
    }
}
