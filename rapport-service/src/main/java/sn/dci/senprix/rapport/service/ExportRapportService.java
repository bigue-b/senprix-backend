package sn.dci.senprix.rapport.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.dci.senprix.rapport.dto.RapportResponse;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Service d'export des rapports en HTML (PDF via impression) et CSV (Excel).
 */
@Service
@RequiredArgsConstructor
public class ExportRapportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] exporterPDF(RapportResponse rapport) {
        String produit = rapport.getProduitNom() != null ? rapport.getProduitNom() : "Produit #" + rapport.getProduitId();
        String marche = rapport.getMarcheNom() != null ? rapport.getMarcheNom() : "Marche #" + rapport.getMarcheId();
        String periode = rapport.getPeriodeDebut().format(FMT) + " - " + rapport.getPeriodeFin().format(FMT);

        String html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Rapport SEN-PRIX</title>"
            + "<style>body{font-family:Arial,sans-serif;margin:40px;color:#1A1F29}"
            + "h1{color:#0F2A4A}table{width:100%;border-collapse:collapse;margin:16px 0}"
            + "td,th{padding:8px 12px;border:1px solid #E5E7EB;text-align:left}"
            + "th{background:#0F2A4A;color:white}.stats{display:flex;gap:16px;margin:16px 0}"
            + ".stat{flex:1;padding:16px;border-radius:8px;text-align:center}"
            + ".s1{background:#EFF6FF;color:#1565C0}.s2{background:#F0FDF4;color:#2D7A4F}.s3{background:#FEF2F2;color:#A32D2D}"
            + ".v{font-size:20px;font-weight:bold}.l{font-size:12px}"
            + "@media print{button{display:none}}</style></head><body>"
            + "<button onclick=\"window.print()\" style=\"margin-bottom:16px;padding:8px 16px;background:#0F2A4A;color:white;border:none;border-radius:6px;cursor:pointer\">Imprimer / Sauvegarder en PDF</button>"
            + "<h1>SEN-PRIX - Rapport d'analyse</h1>"
            + "<p style=\"color:#6B7280\">" + (rapport.getTitre() != null ? rapport.getTitre() : "Rapport de prix") + "</p>"
            + "<table><tr><th>Produit</th><td>" + produit + "</td></tr>"
            + "<tr><th>Marche</th><td>" + marche + "</td></tr>"
            + "<tr><th>Periode</th><td>" + periode + "</td></tr>"
            + "<tr><th>Releves analyses</th><td>" + (rapport.getNombreReleves() != null ? rapport.getNombreReleves() : 0) + "</td></tr></table>"
            + "<div class=\"stats\">"
            + "<div class=\"stat s1\"><div class=\"l\">Prix moyen</div><div class=\"v\">" + (rapport.getPrixMoyen() != null ? rapport.getPrixMoyen().toPlainString() : "0") + " FCFA</div></div>"
            + "<div class=\"stat s2\"><div class=\"l\">Prix minimum</div><div class=\"v\">" + (rapport.getPrixMin() != null ? rapport.getPrixMin().toPlainString() : "0") + " FCFA</div></div>"
            + "<div class=\"stat s3\"><div class=\"l\">Prix maximum</div><div class=\"v\">" + (rapport.getPrixMax() != null ? rapport.getPrixMax().toPlainString() : "0") + " FCFA</div></div>"
            + "</div>"
            + "<p style=\"color:#9CA3AF;font-size:11px\">Genere le " + rapport.getDateGeneration().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " - SEN-PRIX DCI Senegal</p>"
            + "</body></html>";

        return html.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exporterExcel(RapportResponse rapport) {
        String produit = rapport.getProduitNom() != null ? rapport.getProduitNom() : "Produit #" + rapport.getProduitId();
        String marche = rapport.getMarcheNom() != null ? rapport.getMarcheNom() : "Marche #" + rapport.getMarcheId();

        StringBuilder csv = new StringBuilder();
        csv.append("SEN-PRIX - Rapport d'analyse\n\n");
        csv.append("Produit,").append(produit).append("\n");
        csv.append("Marche,").append(marche).append("\n");
        csv.append("Periode,").append(rapport.getPeriodeDebut().format(FMT)).append(" - ").append(rapport.getPeriodeFin().format(FMT)).append("\n");
        csv.append("Date generation,").append(rapport.getDateGeneration().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");
        csv.append("Statistique,Valeur (FCFA)\n");
        csv.append("Prix moyen,").append(rapport.getPrixMoyen() != null ? rapport.getPrixMoyen().toPlainString() : "0").append("\n");
        csv.append("Prix minimum,").append(rapport.getPrixMin() != null ? rapport.getPrixMin().toPlainString() : "0").append("\n");
        csv.append("Prix maximum,").append(rapport.getPrixMax() != null ? rapport.getPrixMax().toPlainString() : "0").append("\n");
        csv.append("Nombre de releves,").append(rapport.getNombreReleves() != null ? rapport.getNombreReleves() : 0).append("\n");

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
}
