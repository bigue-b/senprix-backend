package sn.dci.senprix.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.dci.senprix.user.dto.KeycloakCredentialsResponse;
import sn.dci.senprix.user.dto.UtilisateurCreationResponse;
import sn.dci.senprix.user.dto.UtilisateurRequest;
import sn.dci.senprix.user.dto.UtilisateurResponse;
import sn.dci.senprix.user.entity.AgentCollecte;
import sn.dci.senprix.user.entity.KeycloakUser;
import sn.dci.senprix.user.entity.Utilisateur;
import sn.dci.senprix.user.enums.RoleEnum;
import sn.dci.senprix.user.exception.EmailDejaUtiliseException;
import sn.dci.senprix.user.exception.UtilisateurNotFoundException;
import sn.dci.senprix.user.mapper.UtilisateurMapper;
import sn.dci.senprix.user.repository.AgentCollecteRepository;
import sn.dci.senprix.user.repository.KeycloakUserRepository;
import sn.dci.senprix.user.repository.UtilisateurRepository;
import sn.dci.senprix.user.service.impl.UtilisateurServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private AgentCollecteRepository agentCollecteRepository;

    @Mock
    private KeycloakUserRepository keycloakUserRepository;

    @Mock
    private UtilisateurMapper utilisateurMapper;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    @Test
    void creer_avecRoleAdmin_devraitCreerSansAgentCollecte() {
        // Given
        UtilisateurRequest request = new UtilisateurRequest(
                "Sow", "Moussa", "moussa.sow@dci.sn", "+221770000001",
                RoleEnum.ADMIN, null, null);

        Utilisateur entiteAvantSauvegarde = Utilisateur.builder()
                .nom("Sow").prenom("Moussa").email("moussa.sow@dci.sn").role(RoleEnum.ADMIN)
                .build();
        Utilisateur entiteSauvegardee = Utilisateur.builder()
                .id(1L).nom("Sow").prenom("Moussa").email("moussa.sow@dci.sn").role(RoleEnum.ADMIN)
                .keycloakId("kc-uuid-001")
                .build();

        KeycloakCredentialsResponse credentials = KeycloakCredentialsResponse.builder()
                .username("moussa.sow@dci.sn")
                .motDePasseTemporaire("Xy9#aZ12")
                .changementMotDePasseRequis(true)
                .build();
        KeycloakAdminService.KeycloakAccountCreationResult resultatKeycloak =
                new KeycloakAdminService.KeycloakAccountCreationResult(
                        "kc-uuid-001", "moussa.sow@dci.sn", credentials);

        when(utilisateurRepository.existsByEmail("moussa.sow@dci.sn")).thenReturn(false);
        when(keycloakAdminService.creerCompte("moussa.sow@dci.sn", "Sow", "Moussa", RoleEnum.ADMIN))
                .thenReturn(resultatKeycloak);
        when(utilisateurMapper.toEntity(request)).thenReturn(entiteAvantSauvegarde);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(entiteSauvegardee);
        when(utilisateurMapper.toResponse(entiteSauvegardee, null))
                .thenReturn(UtilisateurResponse.builder().id(1L).role("ADMIN").build());

        // When
        UtilisateurCreationResponse result = utilisateurService.creer(request);

        // Then
        assertThat(result.getUtilisateur().getRole()).isEqualTo("ADMIN");
        assertThat(result.getCredentials().getMotDePasseTemporaire()).isEqualTo("Xy9#aZ12");
        verify(agentCollecteRepository, never()).save(any());
        verify(keycloakUserRepository, times(1)).save(any(KeycloakUser.class));
    }

    @Test
    void creer_avecRoleAgentCollecte_devraitCreerAgentCollecteAssocie() {
        // Given
        UtilisateurRequest request = new UtilisateurRequest(
                "Diop", "Amadou", "amadou.diop@dci.sn", null,
                RoleEnum.AGENT_COLLECTE, "AG-001", "Dakar");

        Utilisateur entiteAvantSauvegarde = Utilisateur.builder()
                .nom("Diop").prenom("Amadou").email("amadou.diop@dci.sn").role(RoleEnum.AGENT_COLLECTE)
                .build();
        Utilisateur entiteSauvegardee = Utilisateur.builder()
                .id(2L).nom("Diop").prenom("Amadou").email("amadou.diop@dci.sn")
                .role(RoleEnum.AGENT_COLLECTE).keycloakId("kc-uuid-002")
                .build();
        AgentCollecte agentCollecte = AgentCollecte.builder()
                .id(2L).matricule("AG-001").zoneAffectation("Dakar").nbreReleves(0)
                .build();

        KeycloakCredentialsResponse credentials = KeycloakCredentialsResponse.builder()
                .username("amadou.diop@dci.sn").motDePasseTemporaire("Ab12#Cd34")
                .changementMotDePasseRequis(true).build();
        KeycloakAdminService.KeycloakAccountCreationResult resultatKeycloak =
                new KeycloakAdminService.KeycloakAccountCreationResult(
                        "kc-uuid-002", "amadou.diop@dci.sn", credentials);

        when(utilisateurRepository.existsByEmail("amadou.diop@dci.sn")).thenReturn(false);
        when(keycloakAdminService.creerCompte(anyString(), anyString(), anyString(), any()))
                .thenReturn(resultatKeycloak);
        when(utilisateurMapper.toEntity(request)).thenReturn(entiteAvantSauvegarde);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(entiteSauvegardee);
        when(utilisateurMapper.toAgentCollecteEntity(entiteSauvegardee, request)).thenReturn(agentCollecte);
        when(agentCollecteRepository.save(agentCollecte)).thenReturn(agentCollecte);
        when(utilisateurMapper.toResponse(entiteSauvegardee, agentCollecte))
                .thenReturn(UtilisateurResponse.builder().id(2L).matricule("AG-001").build());

        // When
        UtilisateurCreationResponse result = utilisateurService.creer(request);

        // Then
        assertThat(result.getUtilisateur().getMatricule()).isEqualTo("AG-001");
        verify(agentCollecteRepository, times(1)).save(agentCollecte);
    }

    @Test
    void creer_avecEmailDejaUtilise_devraitLeverExceptionSansAppelerKeycloak() {
        // Given
        UtilisateurRequest request = new UtilisateurRequest(
                "Test", "Test", "existe@dci.sn", null, RoleEnum.ADMIN, null, null);
        when(utilisateurRepository.existsByEmail("existe@dci.sn")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> utilisateurService.creer(request))
                .isInstanceOf(EmailDejaUtiliseException.class);

        verify(keycloakAdminService, never()).creerCompte(any(), any(), any(), any());
    }

    @Test
    void creer_agentSansMatricule_devraitLeverExceptionSansAppelerKeycloak() {
        // Given : rôle AGENT_COLLECTE mais matricule absent
        UtilisateurRequest request = new UtilisateurRequest(
                "Test", "Test", "agent.sans.matricule@dci.sn", null,
                RoleEnum.AGENT_COLLECTE, null, "Dakar");
        when(utilisateurRepository.existsByEmail("agent.sans.matricule@dci.sn")).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> utilisateurService.creer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("matricule");

        verify(keycloakAdminService, never()).creerCompte(any(), any(), any(), any());
    }

    @Test
    void obtenirParId_quandUtilisateurInexistant_devraitLeverException() {
        // Given
        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> utilisateurService.obtenirParId(99L))
                .isInstanceOf(UtilisateurNotFoundException.class);
    }

    @Test
    void desactiver_devraitChangerActifAFaux() {
        // Given
        Utilisateur utilisateur = Utilisateur.builder().id(1L).actif(true).build();
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // When
        utilisateurService.desactiver(1L);

        // Then
        assertThat(utilisateur.getActif()).isFalse();
        verify(utilisateurRepository, times(1)).save(utilisateur);
    }

    @Test
    void verifier_avecUtilisateurExistant_devraitRetournerExisteVraiEtSonRole() {
        // Given
        Utilisateur utilisateur = Utilisateur.builder()
                .id(5L).role(RoleEnum.AGENT_COLLECTE).actif(true).build();
        when(utilisateurRepository.findById(5L)).thenReturn(Optional.of(utilisateur));

        // When
        var result = utilisateurService.verifier(5L);

        // Then
        assertThat(result.isExiste()).isTrue();
        assertThat(result.getRole()).isEqualTo("AGENT_COLLECTE");
        assertThat(result.isActif()).isTrue();
    }

    @Test
    void verifier_avecUtilisateurInexistant_devraitRetournerExisteFaux() {
        // Given
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        var result = utilisateurService.verifier(999L);

        // Then
        assertThat(result.isExiste()).isFalse();
        assertThat(result.getRole()).isNull();
    }
}
