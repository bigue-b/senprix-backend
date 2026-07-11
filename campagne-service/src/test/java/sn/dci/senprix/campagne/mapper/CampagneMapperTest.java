package sn.dci.senprix.campagne.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.dci.senprix.campagne.dto.CampagneRequest;
import sn.dci.senprix.campagne.dto.CampagneResponse;
import sn.dci.senprix.campagne.entity.Campagne;
import sn.dci.senprix.campagne.entity.CampagneAgent;
import sn.dci.senprix.campagne.entity.CampagneMarche;
import sn.dci.senprix.campagne.entity.CampagneProduit;
import sn.dci.senprix.campagne.enums.StatutCampagne;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CampagneMapperTest {

    private CampagneMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CampagneMapper();
    }

    @Test
    void toEntity_devraitConvertirRequestEnEntite() {
        CampagneRequest request = new CampagneRequest(
                "Collecte Ramadan 2026", "Campagne spéciale",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        Campagne entity = mapper.toEntity(request);

        assertThat(entity.getNom()).isEqualTo("Collecte Ramadan 2026");
        assertThat(entity.getDescription()).isEqualTo("Campagne spéciale");
        assertThat(entity.getDateDebut()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(entity.getDateFin()).isEqualTo(LocalDate.of(2026, 7, 31));
    }

    @Test
    void toResponse_sansAgentsNiMarches_devraitRetournerListesVides() {
        Campagne entity = Campagne.builder()
                .id(1L)
                .nom("Campagne Test")
                .statut(StatutCampagne.PLANIFIEE)
                .dateDebut(LocalDate.of(2026, 8, 1))
                .dateFin(LocalDate.of(2026, 8, 31))
                .dateCreation(LocalDateTime.of(2026, 6, 1, 9, 0))
                .build();

        CampagneResponse response = mapper.toResponse(entity, List.of(), List.of(), List.of());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatut()).isEqualTo("PLANIFIEE");
        assertThat(response.getAgentsIds()).isEmpty();
        assertThat(response.getMarchesIds()).isEmpty();
        assertThat(response.getProduitsIds()).isEmpty();
    }

    @Test
    void toResponse_avecAgentsEtMarches_devraitInclureLeursIdentifiants() {
        Campagne entity = Campagne.builder()
                .id(2L)
                .nom("Campagne Test 2")
                .statut(StatutCampagne.EN_COURS)
                .dateDebut(LocalDate.of(2026, 9, 1))
                .dateFin(LocalDate.of(2026, 9, 30))
                .build();

        CampagneAgent agent1 = CampagneAgent.builder().agentId(10L).build();
        CampagneAgent agent2 = CampagneAgent.builder().agentId(20L).build();
        CampagneMarche marche1 = CampagneMarche.builder().marcheId(100L).build();
        CampagneProduit produit1 = CampagneProduit.builder().produitId(1L).build();

        CampagneResponse response = mapper.toResponse(
                entity, List.of(agent1, agent2), List.of(marche1), List.of(produit1));

        assertThat(response.getAgentsIds()).containsExactly(10L, 20L);
        assertThat(response.getMarchesIds()).containsExactly(100L);
        assertThat(response.getProduitsIds()).containsExactly(1L);
    }
}
