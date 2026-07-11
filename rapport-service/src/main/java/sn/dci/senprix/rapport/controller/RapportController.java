package sn.dci.senprix.rapport.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.dci.senprix.rapport.dto.GenerationRapportRequest;
import sn.dci.senprix.rapport.dto.RapportResponse;
import sn.dci.senprix.rapport.dto.RapportResumeResponse;
import sn.dci.senprix.rapport.service.ExportRapportService;
import sn.dci.senprix.rapport.service.RapportService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RapportController {

    private final RapportService rapportService;
    private final ExportRapportService exportRapportService;

    @PostMapping("/api/admin/rapports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RapportResponse> genererRapport(
            @Valid @RequestBody GenerationRapportRequest request) {
        RapportResponse rapport = rapportService.genererSyntheseProduitMarche(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rapport);
    }

    @GetMapping("/api/public/rapports")
    public ResponseEntity<List<RapportResumeResponse>> listerRapports() {
        return ResponseEntity.ok(rapportService.listerTous());
    }

    @GetMapping("/api/public/rapports/{id}")
    public ResponseEntity<RapportResponse> obtenirRapport(@PathVariable Long id) {
        return ResponseEntity.ok(rapportService.obtenirParId(id));
    }

    @GetMapping("/api/admin/rapports/{id}/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exporterPDF(@PathVariable Long id) {
        RapportResponse rapport = rapportService.obtenirParId(id);
        byte[] contenu = exportRapportService.exporterPDF(rapport);
        String nomFichier = "rapport_" + id + ".html";
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nomFichier).build().toString())
                .body(contenu);
    }

    @GetMapping("/api/admin/rapports/{id}/export/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exporterExcel(@PathVariable Long id) {
        RapportResponse rapport = rapportService.obtenirParId(id);
        byte[] contenu = exportRapportService.exporterExcel(rapport);
        String nomFichier = "rapport_" + id + ".csv";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nomFichier).build().toString())
                .body(contenu);
    }
}

