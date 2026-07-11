package sn.dci.senprix.prix.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sn.dci.senprix.prix.client.CampagneServiceClient;
import sn.dci.senprix.prix.client.CampagneVerificationDto;
import sn.dci.senprix.prix.client.ProduitServiceClient;
import sn.dci.senprix.prix.dto.PrixRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration couvrant le flux complet HTTP → Service → Repository → BD H2
 * pour le PrixController. Les appels réels vers produit-service et
 * campagne-service sont remplacés par des mocks (@MockBean).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrixIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProduitServiceClient produitServiceClient;

    @MockBean
    private CampagneServiceClient campagneServiceClient;

    private PrixRequest requestValide() {
        return new PrixRequest(
                1L, 1L, 1L, 42L,
                new BigDecimal("500.00"), "kg",
                LocalDate.of(2026, 6, 1), "Test");
    }

    @Test
    void soumettrePrix_sansToken_devraitRetourner401() throws Exception {
        mockMvc.perform(post("/api/agent/prix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void soumettrePrix_avecRoleAgentCollecte_devraitRetourner201() throws Exception {
        when(produitServiceClient.produitExiste(1L)).thenReturn(true);
        when(campagneServiceClient.obtenirCampagne(1L))
                .thenReturn(Optional.of(new CampagneVerificationDto(1L, "EN_COURS", List.of(42L), List.of(1L))));

        mockMvc.perform(post("/api/agent/prix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide()))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("AGENT_COLLECTE"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_AGENT_COLLECTE"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.montant").value(500.00))
                .andExpect(jsonPath("$.statut").value("VALIDE"));
    }

    @Test
    void soumettrePrix_avecRoleConsommateur_devraitRetourner403() throws Exception {
        mockMvc.perform(post("/api/agent/prix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide()))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("CONSOMMATEUR"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_CONSOMMATEUR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void soumettrePrix_avecProduitInexistant_devraitRetourner400() throws Exception {
        when(produitServiceClient.produitExiste(anyLong())).thenReturn(false);

        mockMvc.perform(post("/api/agent/prix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide()))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void soumettrePrix_avecAgentNonAffecte_devraitRetourner400() throws Exception {
        when(produitServiceClient.produitExiste(1L)).thenReturn(true);
        when(campagneServiceClient.obtenirCampagne(1L))
                .thenReturn(Optional.of(new CampagneVerificationDto(1L, "EN_COURS", List.of(999L), List.of(1L))));

        mockMvc.perform(post("/api/agent/prix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide()))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listerPrixPublics_sansToken_devraitRetourner200() throws Exception {
        mockMvc.perform(get("/api/public/prix"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirPrixInexistant_devraitRetourner404() throws Exception {
        mockMvc.perform(get("/api/public/prix/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void changerStatut_avecRoleAdmin_devraitReussir() throws Exception {
        when(produitServiceClient.produitExiste(1L)).thenReturn(true);
        when(campagneServiceClient.obtenirCampagne(1L))
                .thenReturn(Optional.of(new CampagneVerificationDto(1L, "EN_COURS", List.of(42L), List.of(1L))));

        String reponseCreation = mockMvc.perform(post("/api/agent/prix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide()))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andReturn().getResponse().getContentAsString();

        Long prixId = objectMapper.readTree(reponseCreation).get("id").asLong();

        mockMvc.perform(patch("/api/admin/prix/" + prixId + "/statut")
                        .param("statut", "REJETE")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REJETE"));
    }

    @Test
    void changerStatut_avecRoleAgentCollecte_devraitRetourner403() throws Exception {
        mockMvc.perform(patch("/api/admin/prix/1/statut")
                        .param("statut", "REJETE")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("AGENT_COLLECTE"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_AGENT_COLLECTE"))))
                .andExpect(status().isForbidden());
    }
}
