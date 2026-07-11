package sn.dci.senprix.rapport.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import sn.dci.senprix.rapport.entity.Rapport;
import sn.dci.senprix.rapport.enums.TypeRapport;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RapportRepositoryTest {

    @Autowired
    private RapportRepository rapportRepository;

    @Test
    void findByType_devraitFiltrerCorrectement() {
        // Given
        rapportRepository.save(creerRapport(TypeRapport.EVOLUTION_PRIX_PRODUIT, 1L, 1L));
        rapportRepository.save(creerRapport(TypeRapport.SYNTHESE_CAMPAGNE, 1L, 1L));

        // When
        List<Rapport> resultats = rapportRepository.findByType(TypeRapport.EVOLUTION_PRIX_PRODUIT);

        // Then
        assertThat(resultats).hasSize(1);
    }

    @Test
    void findByProduitIdAndMarcheId_devraitFiltrerCorrectement() {
        // Given
        rapportRepository.save(creerRapport(TypeRapport.EVOLUTION_PRIX_PRODUIT, 1L, 1L));
        rapportRepository.save(creerRapport(TypeRapport.EVOLUTION_PRIX_PRODUIT, 2L, 1L));

        // When
        List<Rapport> resultats = rapportRepository.findByProduitIdAndMarcheId(1L, 1L);

        // Then
        assertThat(resultats).hasSize(1);
    }

    private Rapport creerRapport(TypeRapport type, Long produitId, Long marcheId) {
        return Rapport.builder()
                .type(type)
                .titre("Rapport test")
                .produitId(produitId).produitNom("Produit test")
                .marcheId(marcheId).marcheNom("Marché test")
                .periodeDebut(LocalDate.of(2026, 6, 1))
                .periodeFin(LocalDate.of(2026, 6, 30))
                .build();
    }
}
