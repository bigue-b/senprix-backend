package sn.dci.senprix.notif.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.dci.senprix.notif.enums.TypeNotification;

import java.util.Map;

/**
 * DTO reçu sur l'endpoint interne /api/internal/notifications, envoyé
 * par les autres microservices (alerte-service, user-service,
 * campagne-service) lorsqu'un événement nécessite l'envoi d'une
 * notification. Le contenu exact du message est généré par
 * notif-service à partir du type et des variables fournies, plutôt
 * que d'être composé par l'appelant — centralise les modèles de
 * message en un seul endroit.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "L'email du destinataire est obligatoire")
    @Email(message = "L'email du destinataire doit être valide")
    private String destinataireEmail;

    @NotNull(message = "Le type de notification est obligatoire")
    private TypeNotification type;

    /**
     * Variables utilisées pour composer le message à partir du modèle
     * associé au type (ex: {"produit": "Riz", "montant": "80000"}).
     */
    private Map<String, String> variables;
}
