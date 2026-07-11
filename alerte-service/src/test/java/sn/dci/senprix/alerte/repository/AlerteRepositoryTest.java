package sn.dci.senprix.alerte.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import sn.dci.senprix.alerte.entity.Alerte;
import sn.dci.senprix.alerte.enums.NiveauGravite;
import sn.dci.senprix.alerte.enums.StatutAlerte;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AlerteRepositoryTest {

    @Autowired
    private AlerteRepository alerteRepository;

    @Test
    void findByStatut_devraitFiltrerCorrectementParStatut() {
        // Given
        alerteRepository.save(creerAlerte(StatutAlerte.NOUVELLE, NiveauGravite.FAIBLE));
        alerteRepository.save(creerAlerte(StatutAlerte.EN_COURS, NiveauGravite.MOYENNE));
        alerteRepository.save(creerAlerte(StatutAlerte.NOUVELLE, NiveauGravite.ELEVEE));

        // When
        List<Alerte> nouvelles = alerteRepository.findByStatut(StatutAlerte.NOUVELLE);

        // Then
        assertThat(nouvelles).hasSize(2);
    }

    @Test
    void findByNiveauGravite_devraitFiltrerCorrectement() {
        // Given
        alerteRepository.save(creerAlerte(StatutAlerte.NOUVELLE, NiveauGravite.ELEVEE));
        alerteRepository.save(creerAlerte(StatutAlerte.NOUVELLE, NiveauGravite.FAIBLE));

        // When
        List<Alerte> elevees = alerteRepository.findByNiveauGravite(NiveauGravite.ELEVEE);

        // Then
        assertThat(elevees).hasSize(1);
    }

    @Test
    void existsByPrixId_devraitDetecterUneAlerteExistante() {
        // Given
        Alerte alerte = creerAlerte(StatutAlerte.NOUVELLE, NiveauGravite.FAIBLE);
        alerte.setPrixId(42L);
        alerteRepository.save(alerte);

        // When / Then
        assertThat(alerteRepository.existsByPrixId(42L)).isTrue();
        assertThat(alerteRepository.existsByPrixId(999L)).isFalse();
    }

    private Alerte creerAlerte(StatutAlerte statut, NiveauGravite gravite) {
        return Alerte.builder()
                .prixId(1L).produitId(1L).marcheId(1L)
                .montant(new BigDecimal("200.00"))
                .montantMoyen(new BigDecimal("100.00"))
                .ecartPourcentage(new BigDecimal("100.00"))
                .niveauGravite(gravite)
                .statut(statut)
                .build();
    }
}
