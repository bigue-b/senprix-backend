package sn.dci.senprix.produit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.dci.senprix.produit.dto.ProduitRequest;
import sn.dci.senprix.produit.dto.ProduitResponse;
import sn.dci.senprix.produit.service.ProduitService;

import java.util.List;

/**
 * Expose les endpoints de gestion du référentiel produits.
 * - /api/public/produits/**  : accès libre, sans authentification (consultation citoyen)
 * - /api/admin/produits/**   : réservé au rôle ADMIN (CRUD complet)
 */
@RestController
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;

    // ===================== ENDPOINTS PUBLICS =====================

    @GetMapping("/api/public/produits")
    public ResponseEntity<List<ProduitResponse>> listerProduitsPublics() {
        return ResponseEntity.ok(produitService.listerActifs());
    }

    @GetMapping("/api/public/produits/{id}")
    public ResponseEntity<ProduitResponse> obtenirProduitPublic(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.obtenirParId(id));
    }

    // ===================== ENDPOINTS ADMIN =====================

    @GetMapping("/api/admin/produits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProduitResponse>> listerTousLesProduits() {
        return ResponseEntity.ok(produitService.listerTous());
    }

    @GetMapping("/api/admin/produits/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProduitResponse> obtenirProduit(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.obtenirParId(id));
    }

    @PostMapping("/api/admin/produits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProduitResponse> creerProduit(@Valid @RequestBody ProduitRequest request) {
        ProduitResponse cree = produitService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @PutMapping("/api/admin/produits/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProduitResponse> modifierProduit(
            @PathVariable Long id, @Valid @RequestBody ProduitRequest request) {
        return ResponseEntity.ok(produitService.modifier(id, request));
    }

    @PatchMapping("/api/admin/produits/{id}/desactiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactiverProduit(@PathVariable Long id) {
        produitService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/admin/produits/{id}/activer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activerProduit(@PathVariable Long id) {
        produitService.activer(id);
        return ResponseEntity.noContent().build();
    }
}
