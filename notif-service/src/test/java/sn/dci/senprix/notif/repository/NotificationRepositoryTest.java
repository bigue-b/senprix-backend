package sn.dci.senprix.notif.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import sn.dci.senprix.notif.entity.Notification;
import sn.dci.senprix.notif.enums.CanalNotification;
import sn.dci.senprix.notif.enums.StatutNotification;
import sn.dci.senprix.notif.enums.TypeNotification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void findByStatut_devraitFiltrerCorrectement() {
        // Given
        notificationRepository.save(creerNotification("a@dci.sn", StatutNotification.ENVOYEE));
        notificationRepository.save(creerNotification("b@dci.sn", StatutNotification.ECHEC));
        notificationRepository.save(creerNotification("c@dci.sn", StatutNotification.ENVOYEE));

        // When
        List<Notification> envoyees = notificationRepository.findByStatut(StatutNotification.ENVOYEE);

        // Then
        assertThat(envoyees).hasSize(2);
    }

    @Test
    void findByDestinataireEmail_devraitRetournerHistoriqueDuDestinataire() {
        // Given
        notificationRepository.save(creerNotification("admin.test@dci.sn", StatutNotification.ENVOYEE));
        notificationRepository.save(creerNotification("admin.test@dci.sn", StatutNotification.ECHEC));
        notificationRepository.save(creerNotification("autre@dci.sn", StatutNotification.ENVOYEE));

        // When
        List<Notification> historique = notificationRepository.findByDestinataireEmail("admin.test@dci.sn");

        // Then
        assertThat(historique).hasSize(2);
    }

    private Notification creerNotification(String email, StatutNotification statut) {
        return Notification.builder()
                .destinataireEmail(email)
                .canal(CanalNotification.EMAIL)
                .type(TypeNotification.ALERTE_PRIX_SUSPECT)
                .sujet("Sujet test")
                .contenu("Contenu test")
                .statut(statut)
                .build();
    }
}
