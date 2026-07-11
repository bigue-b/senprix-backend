package sn.dci.senprix.notif.exception;

public class AbonnementNotFoundException extends RuntimeException {
    public AbonnementNotFoundException(Long id) {
        super("Aucun abonnement trouvé avec l'identifiant " + id);
    }
}
