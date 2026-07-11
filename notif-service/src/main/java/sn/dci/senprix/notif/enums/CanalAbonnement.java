package sn.dci.senprix.notif.enums;

/**
 * Canal choisi par un citoyen pour recevoir les notifications de ses
 * abonnements aux alertes de prix. Distinct de CanalNotification
 * (qui décrit le canal réellement utilisé pour un envoi individuel) :
 * ici, EMAIL_PUSH représente un choix combiné fait par l'utilisateur
 * à l'abonnement, qui donnera lieu à deux envois distincts au moment
 * de la notification.
 */
public enum CanalAbonnement {
    EMAIL,
    PUSH,
    EMAIL_PUSH
}
