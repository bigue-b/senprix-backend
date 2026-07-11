package sn.dci.senprix.campagne.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import sn.dci.senprix.campagne.entity.Campagne;
import sn.dci.senprix.campagne.entity.CampagneAgent;
import sn.dci.senprix.campagne.enums.StatutCampagne;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste les requêtes JPA réelles des repositories du campagne-service
 * sur une base H2 en mémoire.
 */
@DataJpaTest
@ActiveProfiles("test")
class CampagneRepositoryTest {

    @Autowired
    private CampagneRepository campagneRepository;

    @Autowired
    private CampagneAgentRepository campagneAgentRepository;

    @Test
    void findByStatut_devraitFiltrerCorrectementParStatut() {
        // Given
        campagneRepository.save(Campagne.builder()
                .nom("Campagne Planifiée").statut(StatutCampagne.PLANIFIEE)
                .dateDebut(LocalDate.of(2026, 7, 1)).dateFin(LocalDate.of(2026, 7, 31))
                .build());
        campagneRepository.save(Campagne.builder()
                .nom("Campagne En Cours").statut(StatutCampagne.EN_COURS)
                .dateDebut(LocalDate.of(2026, 6, 1)).dateFin(LocalDate.of(2026, 6, 30))
                .build());

        // When
        List<Campagne> enCours = campagneRepository.findByStatut(StatutCampagne.EN_COURS);

        // Then
        assertThat(enCours).extracting(Campagne::getNom).contains("Campagne En Cours");
        assertThat(enCours).extracting(Campagne::getNom).doesNotContain("Campagne Planifiée");
    }

    @Test
    void existsByCampagneIdAndAgentId_devraitDetecterUneAffectationExistante() {
        // Given
        Campagne campagne = campagneRepository.save(Campagne.builder()
                .nom("Campagne Test").statut(StatutCampagne.PLANIFIEE)
                .dateDebut(LocalDate.of(2026, 7, 1)).dateFin(LocalDate.of(2026, 7, 31))
                .build());
        campagneAgentRepository.save(CampagneAgent.builder()
                .campagne(campagne).agentId(10L).build());

        // When / Then
        assertThat(campagneAgentRepository.existsByCampagneIdAndAgentId(campagne.getId(), 10L)).isTrue();
        assertThat(campagneAgentRepository.existsByCampagneIdAndAgentId(campagne.getId(), 99L)).isFalse();
    }

    @Test
    void findByCampagneId_devraitRetournerTousLesAgentsAffectes() {
        // Given
        Campagne campagne = campagneRepository.save(Campagne.builder()
                .nom("Campagne Multi-Agents").statut(StatutCampagne.PLANIFIEE)
                .dateDebut(LocalDate.of(2026, 8, 1)).dateFin(LocalDate.of(2026, 8, 31))
                .build());
        campagneAgentRepository.save(CampagneAgent.builder().campagne(campagne).agentId(1L).build());
        campagneAgentRepository.save(CampagneAgent.builder().campagne(campagne).agentId(2L).build());

        // When
        List<CampagneAgent> agents = campagneAgentRepository.findByCampagneId(campagne.getId());

        // Then
        assertThat(agents).hasSize(2);
        assertThat(agents).extracting(CampagneAgent::getAgentId).containsExactlyInAnyOrder(1L, 2L);
    }
}
