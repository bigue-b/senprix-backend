package sn.dci.senprix.notif.service;

import sn.dci.senprix.notif.dto.NotificationRequest;
import sn.dci.senprix.notif.dto.NotificationResponse;

import java.util.List;

/**
 * Contrat du service métier de gestion des notifications, incluant
 * la génération du contenu à partir de modèles prédéfinis et l'envoi
 * effectif par email.
 */
public interface NotificationService {

    /**
     * Compose et envoie une notification. La notification est
     * toujours persistée, que l'envoi réussisse (statut ENVOYEE) ou
     * échoue (statut ECHEC) — l'appelant n'a jamais à se soucier de
     * l'échec d'envoi, qui ne remonte jamais comme une exception
     * HTTP 5xx vers le microservice appelant.
     */
    NotificationResponse envoyer(NotificationRequest request);

    NotificationResponse obtenirParId(Long id);

    List<NotificationResponse> listerToutes();

    List<NotificationResponse> listerParStatut(String statut);
}
