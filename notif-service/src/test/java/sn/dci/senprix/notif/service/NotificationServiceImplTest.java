package sn.dci.senprix.notif.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.dci.senprix.notif.client.EmailClient;
import sn.dci.senprix.notif.dto.NotificationRequest;
import sn.dci.senprix.notif.dto.NotificationResponse;
import sn.dci.senprix.notif.entity.Notification;
import sn.dci.senprix.notif.enums.StatutNotification;
import sn.dci.senprix.notif.enums.TypeNotification;
import sn.dci.senprix.notif.exception.EnvoiEmailException;
import sn.dci.senprix.notif.exception.NotificationNotFoundException;
import sn.dci.senprix.notif.mapper.ModeleNotification;
import sn.dci.senprix.notif.mapper.NotificationMapper;
import sn.dci.senprix.notif.repository.NotificationRepository;
import sn.dci.senprix.notif.service.impl.NotificationServiceImpl;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private ModeleNotification modeleNotification;

    @Mock
    private EmailClient emailClient;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void envoyer_avecEnvoiReussi_devraitPersisterStatutEnvoyee() {
        // Given
        NotificationRequest request = new NotificationRequest(
                "admin.test@dci.sn", TypeNotification.ALERTE_PRIX_SUSPECT, Map.of("produit", "Riz"));

        when(modeleNotification.genererSujet(TypeNotification.ALERTE_PRIX_SUSPECT)).thenReturn("Sujet test");
        when(modeleNotification.genererContenu(eq(TypeNotification.ALERTE_PRIX_SUSPECT), any()))
                .thenReturn("Contenu test");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notificationMapper.toResponse(any(Notification.class)))
                .thenReturn(NotificationResponse.builder().statut("ENVOYEE").build());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // When
        NotificationResponse result = notificationService.envoyer(request);

        // Then
        verify(emailClient, times(1)).envoyer("admin.test@dci.sn", "Sujet test", "Contenu test");
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatut()).isEqualTo(StatutNotification.ENVOYEE);
        assertThat(captor.getValue().getDateEnvoi()).isNotNull();
        assertThat(result.getStatut()).isEqualTo("ENVOYEE");
    }

    @Test
    void envoyer_avecEchecEnvoi_devraitPersisterStatutEchecSansLeverException() {
        // Given
        NotificationRequest request = new NotificationRequest(
                "admin.test@dci.sn", TypeNotification.COMPTE_CREE, Map.of());

        when(modeleNotification.genererSujet(any())).thenReturn("Sujet test");
        when(modeleNotification.genererContenu(any(), any())).thenReturn("Contenu test");
        doThrow(new EnvoiEmailException("Serveur SMTP injoignable", new RuntimeException()))
                .when(emailClient).envoyer(anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notificationMapper.toResponse(any(Notification.class)))
                .thenReturn(NotificationResponse.builder().statut("ECHEC").build());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // When : ne doit pas lever d'exception malgré l'échec d'envoi
        NotificationResponse result = notificationService.envoyer(request);

        // Then
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatut()).isEqualTo(StatutNotification.ECHEC);
        assertThat(captor.getValue().getMessageErreur()).contains("Serveur SMTP injoignable");
        assertThat(result.getStatut()).isEqualTo("ECHEC");
    }

    @Test
    void obtenirParId_quandNotificationInexistante_devraitLeverException() {
        // Given
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> notificationService.obtenirParId(99L))
                .isInstanceOf(NotificationNotFoundException.class);
    }
}
