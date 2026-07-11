package sn.dci.senprix.export.service.impl;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.dci.senprix.export.client.PrixDto;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class XlsxExportGeneratorTest {

    private XlsxExportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new XlsxExportGenerator();
    }

    @Test
    void genererXlsx_devraitProduireUnFichierExcelValideAvecLesDonnees() throws Exception {
        // Given
        List<PrixDto> prix = List.of(
                new PrixDto(1L, 1L, 1L, 1L, 1L, new BigDecimal("15000.00"), "Sac 25kg",
                        LocalDate.of(2026, 6, 18), "VALIDE")
        );
        Map<Long, String> nomsProduits = Map.of(1L, "Riz brisé 25kg");
        Map<Long, String> nomsMarches = Map.of(1L, "Marché Sandaga");

        // When
        byte[] resultat = generator.genererXlsx(prix, nomsProduits, nomsMarches);

        // Then : on relit le fichier généré pour vérifier son contenu réel
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(resultat))) {
            Sheet sheet = workbook.getSheetAt(0);

            Row entete = sheet.getRow(0);
            assertThat(entete.getCell(0).getStringCellValue()).isEqualTo("Produit");

            Row ligneDonnee = sheet.getRow(1);
            assertThat(ligneDonnee.getCell(0).getStringCellValue()).isEqualTo("Riz brisé 25kg");
            assertThat(ligneDonnee.getCell(1).getStringCellValue()).isEqualTo("Marché Sandaga");
            assertThat(ligneDonnee.getCell(2).getNumericCellValue()).isEqualTo(15000.00);
        }
    }

    @Test
    void genererXlsx_avecPlusieursLignes_devraitToutesLesInclure() throws Exception {
        // Given
        List<PrixDto> prix = List.of(
                new PrixDto(1L, 1L, 1L, 1L, 1L, new BigDecimal("100"), "kg",
                        LocalDate.of(2026, 6, 1), "VALIDE"),
                new PrixDto(2L, 1L, 1L, 1L, 1L, new BigDecimal("200"), "kg",
                        LocalDate.of(2026, 6, 2), "VALIDE"),
                new PrixDto(3L, 1L, 1L, 1L, 1L, new BigDecimal("300"), "kg",
                        LocalDate.of(2026, 6, 3), "VALIDE")
        );

        // When
        byte[] resultat = generator.genererXlsx(prix, Map.of(1L, "Riz"), Map.of(1L, "Sandaga"));

        // Then
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(resultat))) {
            Sheet sheet = workbook.getSheetAt(0);
            // 1 ligne d'en-tête + 3 lignes de données = dernière ligne d'indice 3
            assertThat(sheet.getLastRowNum()).isEqualTo(3);
        }
    }
}
