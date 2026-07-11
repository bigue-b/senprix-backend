package sn.dci.senprix.export.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.dci.senprix.export.enums.FormatExport;
import sn.dci.senprix.export.service.ExportService;

import java.time.LocalDate;

/**
 * Expose l'endpoint de téléchargement direct des relevés de prix au
 * format CSV ou Excel. Réservé à ADMIN, l'opération impliquant des
 * appels inter-services coûteux (récupération + enrichissement) à
 * chaque demande, sans aucune mise en cache.
 */
@RestController
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/api/admin/exports/prix")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exporterPrix(
            @RequestParam Long produitId,
            @RequestParam Long marcheId,
            @RequestParam(required = false) LocalDate periodeDebut,
            @RequestParam(required = false) LocalDate periodeFin,
            @RequestParam(defaultValue = "CSV") FormatExport format) {

        byte[] contenu = exportService.exporterPrix(produitId, marcheId, periodeDebut, periodeFin, format);

        String nomFichier = "prix_produit" + produitId + "_marche" + marcheId
                + (format == FormatExport.CSV ? ".csv" : ".xlsx");

        MediaType typeContenu = (format == FormatExport.CSV)
                ? MediaType.parseMediaType("text/csv; charset=UTF-8")
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok()
                .contentType(typeContenu)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nomFichier).build().toString())
                .body(contenu);
    }

    @GetMapping("/api/public/exports/prix")
    public ResponseEntity<byte[]> exporterPrixPublic(
            @RequestParam Long produitId,
            @RequestParam Long marcheId,
            @RequestParam(required = false) LocalDate periodeDebut,
            @RequestParam(required = false) LocalDate periodeFin,
            @RequestParam(defaultValue = "CSV") FormatExport format) {

        byte[] contenu = exportService.exporterPrix(produitId, marcheId, periodeDebut, periodeFin, format);

        String nomFichier = "senprix_produit" + produitId + "_marche" + marcheId
                + (format == FormatExport.CSV ? ".csv" : ".xlsx");

        MediaType typeContenu = (format == FormatExport.CSV)
                ? MediaType.parseMediaType("text/csv; charset=UTF-8")
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok()
                .contentType(typeContenu)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nomFichier).build().toString())
                .body(contenu);
    }
}
