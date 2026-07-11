package sn.dci.senprix.user.integration;

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
import sn.dci.senprix.user.dto.KeycloakCredentialsResponse;
import sn.dci.senprix.user.dto.UtilisateurRequest;
import sn.dci.senprix.user.enums.RoleEnum;
import sn.dci.senprix.user.service.KeycloakAdminService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration couvrant le flux complet HTTP → Service → Repository → BD H2
 * pour le UtilisateurController. L'appel réel à Keycloak est remplacé par un mock
 * (@MockBean) afin de ne jamais dépendre d'un serveur Keycloak réellement
 * disponible pendant l'exécution des tests automatisés.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UtilisateurIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KeycloakAdminService keycloakAdminService;

    @Test
    void creerUtilisateur_sansToken_devraitRetourner401() throws Exception {
        UtilisateurRequest request = new UtilisateurRequest(
                "Test", "Test", "test@dci.sn", null, RoleEnum.ADMIN, null, null);

        mockMvc.perform(post("/api/admin/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void creerUtilisateur_avecRoleAgentCollecte_devraitRetourner403() throws Exception {
        UtilisateurRequest request = new UtilisateurRequest(
                "Test", "Test", "test2@dci.sn", null, RoleEnum.ADMIN, null, null);

        mockMvc.perform(post("/api/admin/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("AGENT_COLLECTE"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_AGENT_COLLECTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void creerUtilisateur_avecRoleAdmin_devraitCreerEtRetourner201() throws Exception {
        UtilisateurRequest request = new UtilisateurRequest(
                "Sarr", "Khady", "khady.sarr@dci.sn", "+221770000099",
                RoleEnum.ADMIN, null, null);

        KeycloakCredentialsResponse credentials = KeycloakCredentialsResponse.builder()
                .username("khady.sarr@dci.sn")
                .motDePasseTemporaire("Mot2Passe#Temp")
                .changementMotDePasseRequis(true)
                .build();

        when(keycloakAdminService.creerCompte(anyString(), anyString(), anyString(), any()))
                .thenReturn(new KeycloakAdminService.KeycloakAccountCreationResult(
                        "kc-uuid-integration-001", "khady.sarr@dci.sn", credentials));

        mockMvc.perform(post("/api/admin/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.utilisateur.nom").value("Sarr"))
                .andExpect(jsonPath("$.utilisateur.role").value("ADMIN"))
                .andExpect(jsonPath("$.credentials.motDePasseTemporaire").value("Mot2Passe#Temp"))
                .andExpect(jsonPath("$.credentials.changementMotDePasseRequis").value(true));
    }

    @Test
    void creerUtilisateur_agentSansMatricule_devraitRetourner500() throws Exception {
        // Le matricule manquant est levé comme IllegalArgumentException,
        // capturée par le handler générique (500) plutôt qu'un handler dédié.
        UtilisateurRequest request = new UtilisateurRequest(
                "Test", "Test", "agent.test@dci.sn", null,
                RoleEnum.AGENT_COLLECTE, null, "Dakar");

        mockMvc.perform(post("/api/admin/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void creerUtilisateur_avecDonneesInvalides_devraitRetourner400() throws Exception {
        // email vide → viole @NotBlank
        UtilisateurRequest request = new UtilisateurRequest(
                "Test", "Test", "", null, RoleEnum.ADMIN, null, null);

        mockMvc.perform(post("/api/admin/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreursValidation.email").exists());
    }

    @Test
    void obtenirUtilisateurInexistant_devraitRetourner404() throws Exception {
        mockMvc.perform(get("/api/admin/utilisateurs/99999")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("realm_access",
                                        Map.of("roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void verifierUtilisateurInexistant_sansToken_devraitRetourner200AvecExisteFaux() throws Exception {
        // Endpoint public : aucune authentification requise, contrairement
        // à tous les endpoints sous /api/admin/utilisateurs/**.
        mockMvc.perform(get("/api/public/utilisateurs/99999/verification"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existe").value(false));
    }
}
