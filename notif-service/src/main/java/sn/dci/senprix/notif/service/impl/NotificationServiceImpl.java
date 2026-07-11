package sn.dci.senprix.notif.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dci.senprix.notif.client.EmailClient;
import sn.dci.senprix.notif.dto.NotificationRequest;
import sn.dci.senprix.notif.dto.NotificationResponse;
import sn.dci.senprix.notif.entity.Notification;
import sn.dci.senprix.notif.enums.CanalNotification;
import sn.dci.senprix.notif.enums.StatutNotification;
import sn.dci.senprix.notif.exception.EnvoiEmailException;
import sn.dci.senprix.notif.exception.NotificationNotFoundException;
import sn.dci.senprix.notif.mapper.ModeleNotification;
import sn.dci.senprix.notif.mapper.NotificationMapper;
import sn.dci.senprix.notif.repository.NotificationRepository;
import sn.dci.senprix.notif.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service métier de gestion des notifications.
 * Compose le message à partir du modèle associé au type demandé,
 * tente l'envoi par email, puis persiste systématiquement la
 * notification — en cas d'échec d'envoi, la notification est
 * enregistrée avec le statut ECHEC et le message d'erreur associé,
 * plutôt que de faire échouer la requête de l'appelant.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final ModeleNotification modeleNotification;
    private final EmailClient emailClient;

    @Override
    public NotificationResponse envoyer(NotificationRequest request) {
        String sujet = modeleNotification.genererSujet(request.getType());
        String contenu = modeleNotification.genererContenu(request.getType(), request.getVariables());

        Notification notification = Notification.builder()
                .destinataireEmail(request.getDestinataireEmail())
                .canal(CanalNotification.EMAIL)
                .type(request.getType())
                .sujet(sujet)
                .contenu(contenu)
                .statut(StatutNotification.EN_ATTENTE)
                .build();

        try {
            emailClient.envoyer(request.getDestinataireEmail(), sujet, contenu);
            notification.setStatut(StatutNotification.ENVOYEE);
            notification.setDateEnvoi(LocalDateTime.now());

        } catch (EnvoiEmailException ex) {
            notification.setStatut(StatutNotification.ECHEC);
            notification.setMessageErreur(ex.getMessage());
        }

        notification = notificationRepository.save(notification);
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse obtenirParId(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> listerToutes() {
        return notificationRepository.findAll().stream().map(notificationMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> listerParStatut(String statut) {
        StatutNotification statutEnum = StatutNotification.valueOf(statut.toUpperCase());
        return notificationRepository.findByStatut(statutEnum)
                .stream().map(notificationMapper::toResponse).toList();
    }
}
