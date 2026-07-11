package sn.dci.senprix.export.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.dci.senprix.export.client.PrixDto;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExportGeneratorTest {

    private CsvExportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CsvExportGenerator();
    }

    @Test
    void genererCsv_devraitInclureEnteteEtLignesDeDonnees() {
        // Given
        List<PrixDto> prix = List.of(
                new PrixDto(1L, 1L, 1L, 1L, 1L, new BigDecimal("15000.00"), "Sac 25kg",
                        LocalDate.of(2026, 6, 18), "VALIDE")
        );
        Map<Long, String> nomsProduits = Map.of(1L, "Riz brisé 25kg");
        Map<Long, String> nomsMarches = Map.of(1L, "Marché Sandaga");

        // When
        byte[] resultat = generator.genererCsv(prix, nomsProduits, nomsMarches);
        String contenu = new String(resultat, StandardCharsets.UTF_8);

        // Then
        assertThat(contenu).contains("Produit", "Marché", "Montant (FCFA)");
        assertThat(contenu).contains("Riz brisé 25kg");
        assertThat(contenu).contains("Marché Sandaga");
        assertThat(contenu).contains("15000");
        assertThat(contenu).contains("VALIDE");
    }

    @Test
    void genererCsv_avecNomManquant_devraitUtiliserValeurDeRepli() {
        // Given : aucun nom fourni pour le produit 99
        List<PrixDto> prix = List.of(
                new PrixDto(1L, 99L, 1L, 1L, 1L, new BigDecimal("1000"), "kg",
                        LocalDate.of(2026, 6, 18), "VALIDE")
        );

        // When
        byte[] resultat = generator.genererCsv(prix, Map.of(), Map.of());
        String contenu = new String(resultat, StandardCharsets.UTF_8);

        // Then
        assertThat(contenu).contains("Produit #99");
    }

    @Test
    void genererCsv_avecListeVide_devraitRetournerSeulementLEntete() {
        // When
        byte[] resultat = generator.genererCsv(List.of(), Map.of(), Map.of());
        String contenu = new String(resultat, StandardCharsets.UTF_8);

        // Then
        assertThat(contenu).contains("Produit");
        assertThat(contenu.lines().count()).isEqualTo(1); // seulement l'en-tête
    }
}
