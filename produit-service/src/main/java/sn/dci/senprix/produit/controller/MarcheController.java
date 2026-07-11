package sn.dci.senprix.produit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.dci.senprix.produit.dto.MarcheRequest;
import sn.dci.senprix.produit.dto.MarcheResponse;
import sn.dci.senprix.produit.service.MarcheService;

import java.util.List;

/**
 * Expose les endpoints de gestion du référentiel marchés.
 * - /api/public/marches/**  : accès libre, sans authentification
 * - /api/admin/marches/**   : réservé au rôle ADMIN
 */
@RestController
@RequiredArgsConstructor
public class MarcheController {

    private final MarcheService marcheService;

    // ===================== ENDPOINTS PUBLICS =====================

    @GetMapping("/api/public/marches")
    public ResponseEntity<List<MarcheResponse>> listerMarchesPublics(
            @RequestParam(required = false) String region) {

        List<MarcheResponse> marches = (region != null)
                ? marcheService.listerParRegion(region)
                : marcheService.listerActifs();

        return ResponseEntity.ok(marches);
    }

    @GetMapping("/api/public/marches/{id}")
    public ResponseEntity<MarcheResponse> obtenirMarchePublic(@PathVariable Long id) {
        return ResponseEntity.ok(marcheService.obtenirParId(id));
    }

    // ===================== ENDPOINTS ADMIN =====================

    @GetMapping("/api/admin/marches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MarcheResponse>> listerTousLesMarches() {
        return ResponseEntity.ok(marcheService.listerTous());
    }

    @GetMapping("/api/admin/marches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MarcheResponse> obtenirMarche(@PathVariable Long id) {
        return ResponseEntity.ok(marcheService.obtenirParId(id));
    }

    @PostMapping("/api/admin/marches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MarcheResponse> creerMarche(@Valid @RequestBody MarcheRequest request) {
        MarcheResponse cree = marcheService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @PutMapping("/api/admin/marches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MarcheResponse> modifierMarche(
            @PathVariable Long id, @Valid @RequestBody MarcheRequest request) {
        return ResponseEntity.ok(marcheService.modifier(id, request));
    }

    @PatchMapping("/api/admin/marches/{id}/desactiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactiverMarche(@PathVariable Long id) {
        marcheService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/admin/marches/{id}/activer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activerMarche(@PathVariable Long id) {
        marcheService.activer(id);
        return ResponseEntity.noContent().build();
    }
}
