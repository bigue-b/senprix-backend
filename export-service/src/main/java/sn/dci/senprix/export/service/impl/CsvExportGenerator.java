package sn.dci.senprix.export.service.impl;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;
import sn.dci.senprix.export.client.PrixDto;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Génère un fichier CSV à partir d'une liste de relevés de prix,
 * enrichis avec les noms réels de produit et marché. Utilise OpenCSV
 * pour garantir un échappement correct des champs (virgules, guillemets).
 */
@Component
public class CsvExportGenerator {

    public byte[] genererCsv(List<PrixDto> prixListe, Map<Long, String> nomsProduits,
                              Map<Long, String> nomsMarches) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(baos, StandardCharsets.UTF_8));
             CSVWriter csvWriter = new CSVWriter(writer)) {

            csvWriter.writeNext(new String[]{
                    "Produit", "Marché", "Montant (FCFA)", "Unité", "Date du relevé", "Statut"
            });

            for (PrixDto prix : prixListe) {
                csvWriter.writeNext(new String[]{
                        nomsProduits.getOrDefault(prix.getProduitId(), "Produit #" + prix.getProduitId()),
                        nomsMarches.getOrDefault(prix.getMarcheId(), "Marché #" + prix.getMarcheId()),
                        prix.getMontant().toPlainString(),
                        prix.getUnite(),
                        prix.getDateReleve().toString(),
                        prix.getStatut()
                });
            }

        } catch (Exception ex) {
            throw new RuntimeException("Erreur lors de la génération du fichier CSV", ex);
        }

        return baos.toByteArray();
    }
}
