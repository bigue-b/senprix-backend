package sn.dci.senprix.produit.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.dci.senprix.produit.dto.ProduitRequest;
import sn.dci.senprix.produit.dto.ProduitResponse;
import sn.dci.senprix.produit.entity.Produit;
import sn.dci.senprix.produit.enums.StatutEnum;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProduitMapperTest {

    private ProduitMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProduitMapper();
    }

    @Test
    void toEntity_devraitConvertirRequestEnEntitéAvecStatutActif() {
        // Given
        ProduitRequest request = new ProduitRequest(
                "Riz brisé 25kg", "Céréales", "Sac 25kg", "Riz importé de qualité standard");

        // When
        Produit entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getNom()).isEqualTo("Riz brisé 25kg");
        assertThat(entity.getCategorie()).isEqualTo("Céréales");
        assertThat(entity.getUnite()).isEqualTo("Sac 25kg");
        assertThat(entity.getStatut()).isEqualTo(StatutEnum.ACTIF);
        assertThat(entity.getCodeProduit()).isNull(); // généré par le service, pas le mapper
    }

    @Test
    void toResponse_devraitConvertirEntitéEnResponseComplet() {
        // Given
        Produit entity = Produit.builder()
                .id(1L)
                .codeProduit("PRD-001")
                .nom("Huile végétale 5L")
                .categorie("Huiles")
                .unite("Bidon 5L")
                .description("Huile raffinée")
                .statut(StatutEnum.ACTIF)
                .dateCreation(LocalDateTime.of(2026, 6, 1, 10, 0))
                .build();

        // When
        ProduitResponse response = mapper.toResponse(entity);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCodeProduit()).isEqualTo("PRD-001");
        assertThat(response.getNom()).isEqualTo("Huile végétale 5L");
        assertThat(response.getStatut()).isEqualTo("ACTIF");
        assertThat(response.getDateCreation()).isEqualTo(LocalDateTime.of(2026, 6, 1, 10, 0));
    }

    @Test
    void updateEntityFromRequest_devraitMettreAJourSansToucherAuCodeEtStatut() {
        // Given
        Produit entity = Produit.builder()
                .id(1L)
                .codeProduit("PRD-001")
                .nom("Ancien nom")
                .categorie("Ancienne catégorie")
                .unite("Kg")
                .statut(StatutEnum.ACTIF)
                .build();

        ProduitRequest request = new ProduitRequest(
                "Nouveau nom", "Nouvelle catégorie", "Litre", "Nouvelle description");

        // When
        mapper.updateEntityFromRequest(entity, request);

        // Then
        assertThat(entity.getNom()).isEqualTo("Nouveau nom");
        assertThat(entity.getCategorie()).isEqualTo("Nouvelle catégorie");
        assertThat(entity.getUnite()).isEqualTo("Litre");
        // Le code et le statut ne doivent pas être affectés par une modification
        assertThat(entity.getCodeProduit()).isEqualTo("PRD-001");
        assertThat(entity.getStatut()).isEqualTo(StatutEnum.ACTIF);
    }
}
