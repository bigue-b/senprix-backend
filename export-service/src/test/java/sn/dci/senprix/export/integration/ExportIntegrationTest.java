package sn.dci.senprix.export.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sn.dci.senprix.export.client.PrixDto;
import sn.dci.senprix.export.client.PrixServiceClient;
import sn.dci.senprix.export.client.ProduitServiceClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration couvrant le flux complet HTTP → Service → Génération
 * de fichier pour le ExportController. Les appels réels vers prix-service
 * et produit-service sont remplacés par des mocks (@MockBean).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrixServiceClient prixServiceClient;

    @MockBean
    private ProduitServiceClient produitServiceClient;

    private List<PrixDto> prixDeTest() {
        return List.of(
                new PrixDto(1L, 1L, 1L, 1L, 1L, new BigDecimal("15000.00"), "Sac 25kg",
                        LocalDate.of(2026, 6, 18), "VALIDE")
        );
    }

    @Test
    void exporterPrix_sansToken_devraitRetourner401() throws Exception {
        mockMvc.perform(get("/api/admin/exports/prix")
                        .param("produitId", "1")
                        .param("marcheId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exporterPrix_avecRoleAdmin_formatCsv_devraitTelechargerLeFichier() throws Exception {
        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(prixDeTest());
        when(produitServiceClient.obtenirNomProduit(1L)).thenReturn("Riz brisé 25kg");
        when(produitServiceClient.obtenirNomMarche(1L)).thenReturn("Marché Sandaga");

        mockMvc.perform(get("/api/admin/exports/prix")
                        .param("produitId", "1")
                        .param("marcheId", "1")
                        .param("format", "CSV")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("prix_produit1_marche1.csv")));
    }

    @Test
    void exporterPrix_avecRoleAdmin_formatXlsx_devraitTelechargerLeFichier() throws Exception {
        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(prixDeTest());
        when(produitServiceClient.obtenirNomProduit(1L)).thenReturn("Riz brisé 25kg");
        when(produitServiceClient.obtenirNomMarche(1L)).thenReturn("Marché Sandaga");

        mockMvc.perform(get("/api/admin/exports/prix")
                        .param("produitId", "1")
                        .param("marcheId", "1")
                        .param("format", "XLSX")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("prix_produit1_marche1.xlsx")));
    }

    @Test
    void exporterPrix_avecRoleConsommateur_devraitRetourner403() throws Exception {
        mockMvc.perform(get("/api/admin/exports/prix")
                        .param("produitId", "1")
                        .param("marcheId", "1")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("CONSOMMATEUR"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_CONSOMMATEUR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void exporterPrix_sansAucunReleve_devraitRetourner400() throws Exception {
        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/exports/prix")
                        .param("produitId", "1")
                        .param("marcheId", "1")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }
}
