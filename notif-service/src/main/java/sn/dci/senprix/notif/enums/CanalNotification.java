package sn.dci.senprix.notif.enums;

/**
 * Canal utilisé pour l'envoi d'une notification.
 * Seul EMAIL est implémenté pour l'instant ; SMS est prévu pour une
 * évolution future (nécessiterait un fournisseur de SMS payant).
 */
public enum CanalNotification {
    EMAIL,
    SMS
}
