package sn.dci.senprix.notif.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO retourné par l'API pour représenter une notification.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private String destinataireEmail;
    private String canal;
    private String type;
    private String sujet;
    private String statut;
    private String messageErreur;
    private LocalDateTime dateCreation;
    private LocalDateTime dateEnvoi;
}
