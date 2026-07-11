package sn.dci.senprix.prix.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import sn.dci.senprix.prix.entity.Prix;
import sn.dci.senprix.prix.enums.StatutPrix;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Teste les requêtes JPA réelles du repository Prix, en particulier
 * les requêtes d'agrégation (moyenne, min, max) sur une base H2 en
 * mémoire — ces requêtes ne sont pas mockées ici pour s'assurer que
 * la syntaxe JPQL est correcte.
 */
@DataJpaTest
@ActiveProfiles("test")
class PrixRepositoryTest {

    @Autowired
    private PrixRepository prixRepository;

    @Test
    void calculerMoyenne_devraitIgnorerLesPrixNonValides() {
        // Given : deux prix VALIDE (300 et 500) et un prix REJETE (9999, ne doit pas compter)
        prixRepository.save(creerPrix(1L, 1L, new BigDecimal("300.00"), StatutPrix.VALIDE));
        prixRepository.save(creerPrix(1L, 1L, new BigDecimal("500.00"), StatutPrix.VALIDE));
        prixRepository.save(creerPrix(1L, 1L, new BigDecimal("9999.00"), StatutPrix.REJETE));

        // When
        Double moyenne = prixRepository.calculerMoyenne(1L, 1L);

        // Then
        assertThat(moyenne).isCloseTo(400.0, within(0.01));
    }

    @Test
    void calculerMinimumEtMaximum_devraitRetournerLesBornesCorrectes() {
        // Given
        prixRepository.save(creerPrix(2L, 2L, new BigDecimal("250.00"), StatutPrix.VALIDE));
        prixRepository.save(creerPrix(2L, 2L, new BigDecimal("700.00"), StatutPrix.VALIDE));
        prixRepository.save(creerPrix(2L, 2L, new BigDecimal("400.00"), StatutPrix.VALIDE));

        // When
        BigDecimal min = prixRepository.calculerMinimum(2L, 2L);
        BigDecimal max = prixRepository.calculerMaximum(2L, 2L);

        // Then
        assertThat(min).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(max).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    void findByProduitIdAndMarcheId_devraitFiltrerCorrectement() {
        // Given
        prixRepository.save(creerPrix(3L, 3L, new BigDecimal("100.00"), StatutPrix.VALIDE));
        prixRepository.save(creerPrix(3L, 4L, new BigDecimal("200.00"), StatutPrix.VALIDE)); // autre marché

        // When
        List<Prix> resultats = prixRepository.findByProduitIdAndMarcheId(3L, 3L);

        // Then
        assertThat(resultats).hasSize(1);
        assertThat(resultats.get(0).getMontant()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void countByProduitIdAndMarcheIdAndStatut_devraitCompterUniquementLeStatutDemande() {
        // Given
        prixRepository.save(creerPrix(5L, 5L, new BigDecimal("100.00"), StatutPrix.VALIDE));
        prixRepository.save(creerPrix(5L, 5L, new BigDecimal("110.00"), StatutPrix.VALIDE));
        prixRepository.save(creerPrix(5L, 5L, new BigDecimal("999.00"), StatutPrix.SUSPECT));

        // When
        long nombreValides = prixRepository.countByProduitIdAndMarcheIdAndStatut(5L, 5L, StatutPrix.VALIDE);

        // Then
        assertThat(nombreValides).isEqualTo(2);
    }

    private Prix creerPrix(Long produitId, Long marcheId, BigDecimal montant, StatutPrix statut) {
        return Prix.builder()
                .produitId(produitId)
                .marcheId(marcheId)
                .campagneId(1L)
                .agentId(1L)
                .montant(montant)
                .unite("kg")
                .dateReleve(LocalDate.of(2026, 6, 1))
                .statut(statut)
                .build();
    }
}
