package sn.dci.senprix.produit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import sn.dci.senprix.produit.entity.Produit;
import sn.dci.senprix.produit.enums.StatutEnum;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste les requêtes JPA réelles du ProduitRepository sur une base H2 en mémoire.
 */
@DataJpaTest
@ActiveProfiles("test")
class ProduitRepositoryTest {

    @Autowired
    private ProduitRepository produitRepository;

    @Test
    void findByCodeProduit_devraitRetournerLeProduitCorrespondant() {
        // Given
        Produit produit = Produit.builder()
                .codeProduit("PRD-099")
                .nom("Test produit")
                .categorie("Test")
                .unite("Kg")
                .statut(StatutEnum.ACTIF)
                .build();
        produitRepository.save(produit);

        // When
        Optional<Produit> result = produitRepository.findByCodeProduit("PRD-099");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNom()).isEqualTo("Test produit");
    }

    @Test
    void findByStatut_devraitFiltrerCorrectementParStatut() {
        // Given
        produitRepository.save(Produit.builder()
                .codeProduit("PRD-100").nom("Actif 1").categorie("Test").unite("Kg")
                .statut(StatutEnum.ACTIF).build());
        produitRepository.save(Produit.builder()
                .codeProduit("PRD-101").nom("Inactif 1").categorie("Test").unite("Kg")
                .statut(StatutEnum.INACTIF).build());

        // When
        List<Produit> actifs = produitRepository.findByStatut(StatutEnum.ACTIF);

        // Then
        assertThat(actifs).extracting(Produit::getNom).contains("Actif 1");
        assertThat(actifs).extracting(Produit::getNom).doesNotContain("Inactif 1");
    }

    @Test
    void existsByCodeProduit_devraitDetecterUnCodeExistant() {
        // Given
        produitRepository.save(Produit.builder()
                .codeProduit("PRD-200").nom("Existe").categorie("Test").unite("Kg")
                .statut(StatutEnum.ACTIF).build());

        // When / Then
        assertThat(produitRepository.existsByCodeProduit("PRD-200")).isTrue();
        assertThat(produitRepository.existsByCodeProduit("PRD-999")).isFalse();
    }
}
