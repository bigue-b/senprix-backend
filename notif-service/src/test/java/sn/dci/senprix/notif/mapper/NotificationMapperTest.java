package sn.dci.senprix.notif.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.dci.senprix.notif.dto.NotificationResponse;
import sn.dci.senprix.notif.entity.Notification;
import sn.dci.senprix.notif.enums.CanalNotification;
import sn.dci.senprix.notif.enums.StatutNotification;
import sn.dci.senprix.notif.enums.TypeNotification;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMapperTest {

    private NotificationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NotificationMapper();
    }

    @Test
    void toResponse_devraitConvertirEntiteEnResponse() {
        // Given
        Notification entity = Notification.builder()
                .id(1L)
                .destinataireEmail("admin.test@dci.sn")
                .canal(CanalNotification.EMAIL)
                .type(TypeNotification.ALERTE_PRIX_SUSPECT)
                .sujet("SEN-PRIX — Alerte")
                .contenu("Contenu de test")
                .statut(StatutNotification.ENVOYEE)
                .dateCreation(LocalDateTime.of(2026, 6, 18, 10, 0))
                .dateEnvoi(LocalDateTime.of(2026, 6, 18, 10, 1))
                .build();

        // When
        NotificationResponse response = mapper.toResponse(entity);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDestinataireEmail()).isEqualTo("admin.test@dci.sn");
        assertThat(response.getCanal()).isEqualTo("EMAIL");
        assertThat(response.getStatut()).isEqualTo("ENVOYEE");
        assertThat(response.getDateEnvoi()).isNotNull();
    }
}
