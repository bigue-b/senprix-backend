package sn.dci.senprix.rapport.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.dci.senprix.rapport.dto.RapportResponse;
import sn.dci.senprix.rapport.dto.RapportResumeResponse;
import sn.dci.senprix.rapport.entity.Rapport;
import sn.dci.senprix.rapport.enums.TypeRapport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RapportMapperTest {

    private RapportMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RapportMapper();
    }

    @Test
    void toResponse_devraitConvertirEntiteEnResponseComplete() {
        // Given
        Rapport entity = Rapport.builder()
                .id(1L)
                .type(TypeRapport.EVOLUTION_PRIX_PRODUIT)
                .titre("Évolution du prix de Riz brisé — Sandaga")
                .produitId(1L).produitNom("Riz brisé 25kg")
                .marcheId(1L).marcheNom("Marché Sandaga")
                .periodeDebut(LocalDate.of(2026, 6, 1))
                .periodeFin(LocalDate.of(2026, 6, 30))
                .prixMoyen(new BigDecimal("15000.00"))
                .prixMin(new BigDecimal("14000.00"))
                .prixMax(new BigDecimal("16000.00"))
                .nombreReleves(5L)
                .contenuDetailleJson("[]")
                .dateGeneration(LocalDateTime.of(2026, 7, 1, 10, 0))
                .build();

        // When
        RapportResponse response = mapper.toResponse(entity);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProduitNom()).isEqualTo("Riz brisé 25kg");
        assertThat(response.getMarcheNom()).isEqualTo("Marché Sandaga");
        assertThat(response.getPrixMoyen()).isEqualByComparingTo("15000.00");
        assertThat(response.getNombreReleves()).isEqualTo(5L);
    }

    @Test
    void toResumeResponse_neDoitPasInclureLeContenuDetailleJson() {
        // Given
        Rapport entity = Rapport.builder()
                .id(1L)
                .type(TypeRapport.EVOLUTION_PRIX_PRODUIT)
                .titre("Test")
                .produitNom("Riz")
                .marcheNom("Sandaga")
                .periodeDebut(LocalDate.of(2026, 6, 1))
                .periodeFin(LocalDate.of(2026, 6, 30))
                .contenuDetailleJson("[{\"date\":\"2026-06-01\",\"montant\":15000}]")
                .build();

        // When
        RapportResumeResponse response = mapper.toResumeResponse(entity);

        // Then
        assertThat(response.getTitre()).isEqualTo("Test");
        assertThat(response.getProduitNom()).isEqualTo("Riz");
        // RapportResumeResponse n'a volontairement pas de champ contenuDetailleJson
    }
}
