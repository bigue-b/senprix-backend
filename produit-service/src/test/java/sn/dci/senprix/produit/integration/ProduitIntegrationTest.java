package sn.dci.senprix.produit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sn.dci.senprix.produit.dto.ProduitRequest;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration couvrant le flux complet HTTP → Service → Repository → BD H2
 * pour le ProduitController, incluant la vérification des règles de sécurité par rôle.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProduitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listerProduitsPublics_sansAuthentification_devraitRetourner200() throws Exception {
        mockMvc.perform(get("/api/public/produits"))
                .andExpect(status().isOk());
    }

    @Test
    void creerProduit_sansToken_devraitRetourner401() throws Exception {
        ProduitRequest request = new ProduitRequest("Test", "Test", "Kg", null);

        mockMvc.perform(post("/api/admin/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void creerProduit_avecRoleConsommateur_devraitRetourner403() throws Exception {
        ProduitRequest request = new ProduitRequest("Test", "Test", "Kg", null);

        mockMvc.perform(post("/api/admin/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("CONSOMMATEUR"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_CONSOMMATEUR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void creerProduit_avecRoleAdmin_devraitCreerEtRetourner201() throws Exception {
        ProduitRequest request = new ProduitRequest(
                "Tomate fraîche 1kg", "Légumes", "Kg", "Tomate locale");

        mockMvc.perform(post("/api/admin/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Tomate fraîche 1kg"))
                .andExpect(jsonPath("$.codeProduit").exists())
                .andExpect(jsonPath("$.statut").value("ACTIF"));
    }

    @Test
    void creerProduit_avecDonneesInvalides_devraitRetourner400() throws Exception {
        // nom vide → viole @NotBlank
        ProduitRequest request = new ProduitRequest("", "Légumes", "Kg", null);

        mockMvc.perform(post("/api/admin/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreursValidation.nom").exists());
    }

    @Test
    void obtenirProduitInexistant_devraitRetourner404() throws Exception {
        mockMvc.perform(get("/api/public/produits/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("introuvable")));
    }
}
