package sn.dci.senprix.campagne.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.dci.senprix.campagne.dto.AffectationAgentRequest;
import sn.dci.senprix.campagne.dto.AssociationMarcheRequest;
import sn.dci.senprix.campagne.dto.AssociationProduitRequest;
import sn.dci.senprix.campagne.dto.CampagneRequest;
import sn.dci.senprix.campagne.dto.CampagneResponse;
import sn.dci.senprix.campagne.service.CampagneService;

import java.util.List;

/**
 * Expose les endpoints de gestion des campagnes de collecte de prix.
 * - /api/public/campagnes/**  : consultation libre, sans authentification
 * - /api/admin/campagnes/**   : création, modification, affectations,
 *   changements de statut — réservé au rôle ADMIN
 */
@RestController
@RequiredArgsConstructor
public class CampagneController {

    private final CampagneService campagneService;

    // ===================== ENDPOINTS PUBLICS =====================

    @GetMapping("/api/public/campagnes")
    public ResponseEntity<List<CampagneResponse>> listerCampagnesPubliques() {
        return ResponseEntity.ok(campagneService.listerToutes());
    }

    @GetMapping("/api/public/campagnes/{id}")
    public ResponseEntity<CampagneResponse> obtenirCampagnePublique(@PathVariable Long id) {
        return ResponseEntity.ok(campagneService.obtenirParId(id));
    }

    // ===================== ENDPOINTS ADMIN =====================

    @PostMapping("/api/admin/campagnes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampagneResponse> creerCampagne(@Valid @RequestBody CampagneRequest request) {
        CampagneResponse cree = campagneService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @PutMapping("/api/admin/campagnes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampagneResponse> modifierCampagne(
            @PathVariable Long id, @Valid @RequestBody CampagneRequest request) {
        return ResponseEntity.ok(campagneService.modifier(id, request));
    }

    @PostMapping("/api/admin/campagnes/{id}/agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampagneResponse> affecterAgent(
            @PathVariable Long id, @Valid @RequestBody AffectationAgentRequest request) {
        return ResponseEntity.ok(campagneService.affecterAgent(id, request.getAgentId()));
    }

    @DeleteMapping("/api/admin/campagnes/{id}/agents/{agentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> retirerAgent(@PathVariable Long id, @PathVariable Long agentId) {
        campagneService.retirerAgent(id, agentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/campagnes/{id}/marches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampagneResponse> associerMarche(
            @PathVariable Long id, @Valid @RequestBody AssociationMarcheRequest request) {
        return ResponseEntity.ok(campagneService.associerMarche(id, request.getMarcheId()));
    }

    @DeleteMapping("/api/admin/campagnes/{id}/marches/{marcheId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> dissocierMarche(@PathVariable Long id, @PathVariable Long marcheId) {
        campagneService.dissocierMarche(id, marcheId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/campagnes/{id}/produits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampagneResponse> associerProduit(
            @PathVariable Long id, @Valid @RequestBody AssociationProduitRequest request) {
        return ResponseEntity.ok(campagneService.associerProduit(id, request.getProduitId()));
    }

    @DeleteMapping("/api/admin/campagnes/{id}/produits/{produitId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> dissocierProduit(@PathVariable Long id, @PathVariable Long produitId) {
        campagneService.dissocierProduit(id, produitId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/admin/campagnes/{id}/demarrer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampagneResponse> demarrerCampagne(@PathVariable Long id) {
        return ResponseEntity.ok(campagneService.demarrer(id));
    }

    @PatchMapping("/api/admin/campagnes/{id}/terminer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampagneResponse> terminerCampagne(@PathVariable Long id) {
        return ResponseEntity.ok(campagneService.terminer(id));
    }

    @PatchMapping("/api/admin/campagnes/{id}/annuler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampagneResponse> annulerCampagne(@PathVariable Long id) {
        return ResponseEntity.ok(campagneService.annuler(id));
    }
}
