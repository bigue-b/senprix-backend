package sn.dci.senprix.user.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.dci.senprix.user.dto.UtilisateurRequest;
import sn.dci.senprix.user.dto.UtilisateurResponse;
import sn.dci.senprix.user.entity.AgentCollecte;
import sn.dci.senprix.user.entity.Utilisateur;
import sn.dci.senprix.user.enums.RoleEnum;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UtilisateurMapperTest {

    private UtilisateurMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UtilisateurMapper();
    }

    @Test
    void toEntity_devraitConvertirRequestEnEntitéActive() {
        // Given
        UtilisateurRequest request = new UtilisateurRequest(
                "Diop", "Amadou", "amadou.diop@dci.sn", "+221770000000",
                RoleEnum.AGENT_COLLECTE, "AG-001", "Dakar");

        // When
        Utilisateur entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getNom()).isEqualTo("Diop");
        assertThat(entity.getPrenom()).isEqualTo("Amadou");
        assertThat(entity.getEmail()).isEqualTo("amadou.diop@dci.sn");
        assertThat(entity.getRole()).isEqualTo(RoleEnum.AGENT_COLLECTE);
        assertThat(entity.getActif()).isTrue();
        assertThat(entity.getKeycloakId()).isNull(); // affecté séparément par le service
    }

    @Test
    void toAgentCollecteEntity_devraitConstruireAvecMatriculeEtZone() {
        // Given
        Utilisateur utilisateur = Utilisateur.builder().id(1L).build();
        UtilisateurRequest request = new UtilisateurRequest(
                "Ndiaye", "Fatou", "fatou.ndiaye@dci.sn", null,
                RoleEnum.AGENT_COLLECTE, "AG-002", "Thiès");

        // When
        AgentCollecte agentCollecte = mapper.toAgentCollecteEntity(utilisateur, request);

        // Then
        assertThat(agentCollecte.getUtilisateur()).isEqualTo(utilisateur);
        assertThat(agentCollecte.getMatricule()).isEqualTo("AG-002");
        assertThat(agentCollecte.getZoneAffectation()).isEqualTo("Thiès");
        assertThat(agentCollecte.getNbreReleves()).isZero();
    }

    @Test
    void toResponse_sansAgentCollecte_devraitOmettreLesChampsAgent() {
        // Given
        Utilisateur entity = Utilisateur.builder()
                .id(1L)
                .nom("Sow")
                .prenom("Moussa")
                .email("moussa.sow@dci.sn")
                .role(RoleEnum.ADMIN)
                .actif(true)
                .dateCreation(LocalDateTime.of(2026, 6, 1, 9, 0))
                .build();

        // When
        UtilisateurResponse response = mapper.toResponse(entity);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRole()).isEqualTo("ADMIN");
        assertThat(response.getMatricule()).isNull();
        assertThat(response.getZoneAffectation()).isNull();
    }

    @Test
    void toResponse_avecAgentCollecte_devraitInclureMatriculeEtZone() {
        // Given
        Utilisateur entity = Utilisateur.builder()
                .id(2L)
                .nom("Diallo")
                .prenom("Aissatou")
                .email("aissatou.diallo@dci.sn")
                .role(RoleEnum.AGENT_COLLECTE)
                .actif(true)
                .build();

        AgentCollecte agentCollecte = AgentCollecte.builder()
                .id(2L)
                .matricule("AG-010")
                .zoneAffectation("Saint-Louis")
                .nbreReleves(5)
                .build();

        // When
        UtilisateurResponse response = mapper.toResponse(entity, agentCollecte);

        // Then
        assertThat(response.getMatricule()).isEqualTo("AG-010");
        assertThat(response.getZoneAffectation()).isEqualTo("Saint-Louis");
    }
}
