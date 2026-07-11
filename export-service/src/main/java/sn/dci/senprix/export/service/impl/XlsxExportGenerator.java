package sn.dci.senprix.export.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import sn.dci.senprix.export.client.PrixDto;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Génère un fichier Excel (xlsx) à partir d'une liste de relevés de
 * prix, enrichis avec les noms réels de produit et marché. Utilise
 * Apache POI, avec un en-tête mis en gras pour la lisibilité.
 */
@Component
public class XlsxExportGenerator {

    public byte[] genererXlsx(List<PrixDto> prixListe, Map<Long, String> nomsProduits,
                               Map<Long, String> nomsMarches) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Relevés de prix");

            CellStyle styleEntete = workbook.createCellStyle();
            Font policeGrasse = workbook.createFont();
            policeGrasse.setBold(true);
            styleEntete.setFont(policeGrasse);

            Row entete = sheet.createRow(0);
            String[] colonnes = {"Produit", "Marché", "Montant (FCFA)", "Unité", "Date du relevé", "Statut"};
            for (int i = 0; i < colonnes.length; i++) {
                Cell cellule = entete.createCell(i);
                cellule.setCellValue(colonnes[i]);
                cellule.setCellStyle(styleEntete);
            }

            int numeroLigne = 1;
            for (PrixDto prix : prixListe) {
                Row ligne = sheet.createRow(numeroLigne++);
                ligne.createCell(0).setCellValue(
                        nomsProduits.getOrDefault(prix.getProduitId(), "Produit #" + prix.getProduitId()));
                ligne.createCell(1).setCellValue(
                        nomsMarches.getOrDefault(prix.getMarcheId(), "Marché #" + prix.getMarcheId()));
                ligne.createCell(2).setCellValue(prix.getMontant().doubleValue());
                ligne.createCell(3).setCellValue(prix.getUnite());
                ligne.createCell(4).setCellValue(prix.getDateReleve().toString());
                ligne.createCell(5).setCellValue(prix.getStatut());
            }

            for (int i = 0; i < colonnes.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception ex) {
            throw new RuntimeException("Erreur lors de la génération du fichier Excel", ex);
        }
    }
}
