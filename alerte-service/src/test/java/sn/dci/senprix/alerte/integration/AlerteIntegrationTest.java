package sn.dci.senprix.alerte.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sn.dci.senprix.alerte.dto.AlerteCreationRequest;
import sn.dci.senprix.alerte.dto.AlerteResponse;
import sn.dci.senprix.alerte.dto.ResolutionRequest;
import sn.dci.senprix.alerte.service.AlerteService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration couvrant le flux complet HTTP → Service → Repository → BD H2
 * pour le AlerteController, incluant le cycle de vie complet d'une alerte.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlerteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlerteService alerteService;

    private AlerteCreationRequest requestValide() {
        return new AlerteCreationRequest(
                10L, 1L, 1L, new BigDecimal("300.00"), new BigDecimal("100.00"));
    }

    @Test
    void creerAlerte_devraitClasserEtInitialiserLeStatut() {
        // La création d'alerte n'est plus exposée en REST : elle est désormais
        // déclenchée par AlerteEventListener suite à un message RabbitMQ, mais
        // passe par le même AlerteService.creer() que l'ancien endpoint.
        AlerteResponse reponse = alerteService.creer(requestValide());

        assertThat(reponse.getNiveauGravite()).isEqualTo("MOYENNE");
        assertThat(reponse.getStatut()).isEqualTo("NOUVELLE");
    }

    @Test
    void listerAlertes_sansToken_devraitRetourner401() throws Exception {
        mockMvc.perform(get("/api/admin/alertes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listerAlertes_avecRoleAdmin_devraitReussir() throws Exception {
        mockMvc.perform(get("/api/admin/alertes")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void listerAlertes_avecRoleAgentCollecte_devraitRetourner403() throws Exception {
        mockMvc.perform(get("/api/admin/alertes")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("AGENT_COLLECTE"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_AGENT_COLLECTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void cycleDeVieComplet_prendreEnChargeEtResoudre_devraitReussir() throws Exception {
        // Étape 1 : créer l'alerte (simulant la réception d'un message RabbitMQ du prix-service)
        Long alerteId = alerteService.creer(requestValide()).getId();

        // Étape 2 : un admin prend l'alerte en charge
        mockMvc.perform(patch("/api/admin/alertes/" + alerteId + "/prendre-en-charge")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_COURS"));

        // Étape 3 : l'admin résout l'alerte
        ResolutionRequest resolution = new ResolutionRequest("Verifie sur le terrain, prix confirme exact");

        mockMvc.perform(patch("/api/admin/alertes/" + alerteId + "/resoudre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolution))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("RESOLUE"))
                .andExpect(jsonPath("$.commentaireResolution").value("Verifie sur le terrain, prix confirme exact"));
    }

    @Test
    void resoudre_directementDepuisNouvelle_devraitRetourner400() throws Exception {
        Long alerteId = alerteService.creer(requestValide()).getId();

        ResolutionRequest resolution = new ResolutionRequest("Tentative directe");

        mockMvc.perform(patch("/api/admin/alertes/" + alerteId + "/resoudre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolution))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenirAlerteInexistante_devraitRetourner404() throws Exception {
        mockMvc.perform(get("/api/admin/alertes/99999")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }
}
