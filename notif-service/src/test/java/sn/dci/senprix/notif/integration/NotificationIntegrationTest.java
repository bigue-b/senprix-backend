package sn.dci.senprix.notif.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sn.dci.senprix.notif.dto.NotificationRequest;
import sn.dci.senprix.notif.enums.TypeNotification;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration couvrant le flux complet HTTP → Service → Repository → BD H2
 * pour le NotificationController. JavaMailSender est remplacé par un mock
 * (@MockBean) afin de ne jamais effectuer de véritable envoi SMTP pendant
 * les tests automatisés.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JavaMailSender mailSender;

    private NotificationRequest requestValide() {
        return new NotificationRequest(
                "admin.test@dci.sn", TypeNotification.ALERTE_PRIX_SUSPECT,
                Map.of("produit", "Riz brisé 25kg", "marche", "Sandaga"));
    }

    @Test
    void envoyerNotification_sansToken_devraitReussirCarEndpointInterne() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        // L'endpoint /api/internal/notifications est volontairement sans authentification
        mockMvc.perform(post("/api/internal/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.destinataireEmail").value("admin.test@dci.sn"))
                .andExpect(jsonPath("$.statut").value("ENVOYEE"));
    }

    @Test
    void envoyerNotification_avecEchecSmtp_devraitRetourner201AvecStatutEchec() throws Exception {
        // Simule un serveur SMTP indisponible : createMimeMessage lève une exception
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Connexion SMTP refusée"));

        mockMvc.perform(post("/api/internal/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("ECHEC"));
    }

    @Test
    void envoyerNotification_avecEmailInvalide_devraitRetourner400() throws Exception {
        NotificationRequest requestInvalide = new NotificationRequest(
                "pas-un-email", TypeNotification.COMPTE_CREE, Map.of());

        mockMvc.perform(post("/api/internal/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalide)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listerNotifications_sansToken_devraitRetourner401() throws Exception {
        mockMvc.perform(get("/api/admin/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listerNotifications_avecRoleAdmin_devraitReussir() throws Exception {
        mockMvc.perform(get("/api/admin/notifications")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirNotificationInexistante_devraitRetourner404() throws Exception {
        mockMvc.perform(get("/api/admin/notifications/99999")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }
}
