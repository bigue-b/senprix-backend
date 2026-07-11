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
import sn.dci.senprix.alerte.dto.ResolutionRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    private AlerteCreationRequest requestValide() {
        return new AlerteCreationRequest(
                10L, 1L, 1L, new BigDecimal("300.00"), new BigDecimal("100.00"));
    }

    @Test
    void creerAlerte_sansToken_devraitReussirCarEndpointInterne() throws Exception {
        // L'endpoint /api/internal/alertes est volontairement sans authentification
        mockMvc.perform(post("/api/internal/alertes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.niveauGravite").value("MOYENNE"))
                .andExpect(jsonPath("$.statut").value("NOUVELLE"));
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
        // Étape 1 : créer l'alerte (simulant un appel du prix-service)
        String reponseCreation = mockMvc.perform(post("/api/internal/alertes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide())))
                .andReturn().getResponse().getContentAsString();

        Long alerteId = objectMapper.readTree(reponseCreation).get("id").asLong();

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
        String reponseCreation = mockMvc.perform(post("/api/internal/alertes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide())))
                .andReturn().getResponse().getContentAsString();

        Long alerteId = objectMapper.readTree(reponseCreation).get("id").asLong();

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
