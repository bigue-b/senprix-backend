package sn.dci.senprix.rapport.integration;

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
import sn.dci.senprix.rapport.client.PrixDto;
import sn.dci.senprix.rapport.client.PrixServiceClient;
import sn.dci.senprix.rapport.client.ProduitServiceClient;
import sn.dci.senprix.rapport.dto.GenerationRapportRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration couvrant le flux complet HTTP → Service → Repository → BD H2
 * pour le RapportController. Les appels réels vers prix-service et
 * produit-service sont remplacés par des mocks (@MockBean).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RapportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrixServiceClient prixServiceClient;

    @MockBean
    private ProduitServiceClient produitServiceClient;

    private GenerationRapportRequest requestValide() {
        return new GenerationRapportRequest(
                1L, 1L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
    }

    private List<PrixDto> prixValides() {
        return List.of(
                new PrixDto(1L, 1L, 1L, 1L, 1L, new BigDecimal("300"), "kg",
                        LocalDate.of(2026, 6, 10), "VALIDE"),
                new PrixDto(2L, 1L, 1L, 1L, 1L, new BigDecimal("500"), "kg",
                        LocalDate.of(2026, 6, 15), "VALIDE")
        );
    }

    @Test
    void genererRapport_sansToken_devraitRetourner401() throws Exception {
        mockMvc.perform(post("/api/admin/rapports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void genererRapport_avecRoleAdmin_devraitReussir() throws Exception {
        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(prixValides());
        when(produitServiceClient.obtenirNomProduit(1L)).thenReturn("Riz brisé 25kg");
        when(produitServiceClient.obtenirNomMarche(1L)).thenReturn("Marché Sandaga");

        mockMvc.perform(post("/api/admin/rapports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide()))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produitNom").value("Riz brisé 25kg"))
                .andExpect(jsonPath("$.prixMoyen").value(400.00))
                .andExpect(jsonPath("$.nombreReleves").value(2));
    }

    @Test
    void genererRapport_avecRoleConsommateur_devraitRetourner403() throws Exception {
        mockMvc.perform(post("/api/admin/rapports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide()))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("CONSOMMATEUR"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_CONSOMMATEUR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void genererRapport_sansAucunReleve_devraitRetourner400() throws Exception {
        when(prixServiceClient.listerPrixParProduitEtMarche(anyLong(), anyLong()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/admin/rapports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide()))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listerRapports_sansToken_devraitReussirCarEndpointPublic() throws Exception {
        mockMvc.perform(get("/api/public/rapports"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirRapportInexistant_devraitRetourner404() throws Exception {
        mockMvc.perform(get("/api/public/rapports/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cycleComplet_genererPuisConsulter_devraitReussir() throws Exception {
        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(prixValides());
        when(produitServiceClient.obtenirNomProduit(1L)).thenReturn("Riz brisé 25kg");
        when(produitServiceClient.obtenirNomMarche(1L)).thenReturn("Marché Sandaga");

        String reponseGeneration = mockMvc.perform(post("/api/admin/rapports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValide()))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andReturn().getResponse().getContentAsString();

        Long rapportId = objectMapper.readTree(reponseGeneration).get("id").asLong();

        mockMvc.perform(get("/api/public/rapports/" + rapportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produitNom").value("Riz brisé 25kg"));
    }
}
