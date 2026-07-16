package sn.dci.senprix.campagne.integration;

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
import sn.dci.senprix.campagne.client.ProduitServiceClient;
import sn.dci.senprix.campagne.client.UserServiceClient;
import sn.dci.senprix.campagne.client.UtilisateurVerificationDto;
import sn.dci.senprix.campagne.dto.AffectationAgentRequest;
import sn.dci.senprix.campagne.dto.AssociationMarcheRequest;
import sn.dci.senprix.campagne.dto.CampagneRequest;

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
 * pour le CampagneController. Les appels réels vers produit-service et
 * user-service sont remplacés par des mocks (@MockBean) afin de ne jamais
 * dépendre de la disponibilité réelle de ces services pendant les tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CampagneIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProduitServiceClient produitServiceClient;

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    void creerCampagne_sansToken_devraitRetourner401() throws Exception {
        CampagneRequest request = new CampagneRequest(
                "Test", null, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        mockMvc.perform(post("/api/admin/campagnes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void creerCampagne_avecRoleAdmin_devraitRetourner201() throws Exception {
        CampagneRequest request = new CampagneRequest(
                "Collecte Tabaski 2026", "Campagne spéciale",
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        mockMvc.perform(post("/api/admin/campagnes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Collecte Tabaski 2026"))
                .andExpect(jsonPath("$.statut").value("PLANIFIEE"));
    }

    @Test
    void creerCampagne_avecRoleConsommateur_devraitRetourner403() throws Exception {
        // Dates volontairement calculées à partir d'aujourd'hui : une date de début
        // fixe finit dans le passé (violant @FutureOrPresent sur CampagneRequest),
        // ce qui déclenche un 400 de validation avant même le contrôle @PreAuthorize
        // et fait échouer l'assertion attendue sur le 403.
        CampagneRequest request = new CampagneRequest(
                "Test", null, LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(1).plusDays(30));

        mockMvc.perform(post("/api/admin/campagnes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("CONSOMMATEUR"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_CONSOMMATEUR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listerCampagnesPubliques_sansToken_devraitRetourner200() throws Exception {
        // Endpoint public : aucune authentification requise
        mockMvc.perform(get("/api/public/campagnes"))
                .andExpect(status().isOk());
    }

    @Test
    void affecterAgent_avecAgentValide_devraitRetourner200() throws Exception {
        // Given : créer une campagne d'abord
        CampagneRequest campagneRequest = new CampagneRequest(
                "Campagne Pour Affectation", null,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        String reponseCreation = mockMvc.perform(post("/api/admin/campagnes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(campagneRequest))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long campagneId = objectMapper.readTree(reponseCreation).get("id").asLong();

        when(userServiceClient.verifierUtilisateur(anyLong()))
                .thenReturn(new UtilisateurVerificationDto(true, "AGENT_COLLECTE", true));

        AffectationAgentRequest affectationRequest = new AffectationAgentRequest(42L);

        // When / Then
        mockMvc.perform(post("/api/admin/campagnes/" + campagneId + "/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(affectationRequest))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentsIds[0]").value(42));
    }

    @Test
    void affecterAgent_avecAgentInexistant_devraitRetourner400() throws Exception {
        // Given
        CampagneRequest campagneRequest = new CampagneRequest(
                "Campagne Test Agent Invalide", null,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        String reponseCreation = mockMvc.perform(post("/api/admin/campagnes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(campagneRequest))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andReturn().getResponse().getContentAsString();

        Long campagneId = objectMapper.readTree(reponseCreation).get("id").asLong();

        when(userServiceClient.verifierUtilisateur(anyLong()))
                .thenReturn(new UtilisateurVerificationDto(false, null, false));

        AffectationAgentRequest affectationRequest = new AffectationAgentRequest(999L);

        // When / Then
        mockMvc.perform(post("/api/admin/campagnes/" + campagneId + "/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(affectationRequest))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void associerMarche_avecMarcheValide_devraitRetourner200() throws Exception {
        // Given
        CampagneRequest campagneRequest = new CampagneRequest(
                "Campagne Pour Marché", null,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

        String reponseCreation = mockMvc.perform(post("/api/admin/campagnes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(campagneRequest))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andReturn().getResponse().getContentAsString();

        Long campagneId = objectMapper.readTree(reponseCreation).get("id").asLong();

        when(produitServiceClient.marcheExiste(anyLong())).thenReturn(true);

        AssociationMarcheRequest marcheRequest = new AssociationMarcheRequest(7L);

        // When / Then
        mockMvc.perform(post("/api/admin/campagnes/" + campagneId + "/marches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(marcheRequest))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marchesIds[0]").value(7));
    }

    @Test
    void obtenirCampagneInexistante_devraitRetourner404() throws Exception {
        mockMvc.perform(get("/api/public/campagnes/99999"))
                .andExpect(status().isNotFound());
    }
}
