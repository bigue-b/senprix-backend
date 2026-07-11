package sn.dci.senprix.notif.mapper;

import org.springframework.stereotype.Component;
import sn.dci.senprix.notif.dto.NotificationResponse;
import sn.dci.senprix.notif.entity.Notification;

/**
 * Assure la conversion entre l'entité JPA Notification et les DTOs
 * exposés par l'API.
 */
@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .destinataireEmail(entity.getDestinataireEmail())
                .canal(entity.getCanal().name())
                .type(entity.getType().name())
                .sujet(entity.getSujet())
                .statut(entity.getStatut().name())
                .messageErreur(entity.getMessageErreur())
                .dateCreation(entity.getDateCreation())
                .dateEnvoi(entity.getDateEnvoi())
                .build();
    }
}
