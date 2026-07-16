package sn.dci.senprix.alerte.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.dci.senprix.alerte.dto.AlerteResponse;
import sn.dci.senprix.alerte.dto.ResolutionRequest;
import sn.dci.senprix.alerte.service.AlerteService;

import java.util.List;

/**
 * Expose les endpoints de gestion des alertes de prix suspects.
 * - /api/public/alertes    : consultation publique
 * - /api/admin/alertes/**  : consultation et traitement, réservé à ADMIN
 *
 * La création d'alerte n'est plus exposée via un endpoint REST interne :
 * elle est déclenchée par AlerteEventListener suite à la réception d'un
 * message RabbitMQ publié par prix-service (voir event/listener).
 */
@RestController
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteService alerteService;

    // ===================== ENDPOINT PUBLIC =====================

    @GetMapping("/api/public/alertes")
    public ResponseEntity<List<AlerteResponse>> listerAlertesPubliques(
            @RequestParam(required = false) String statut) {
        List<AlerteResponse> alertes = (statut != null)
                ? alerteService.listerParStatut(statut)
                : alerteService.listerToutes();
        return ResponseEntity.ok(alertes);
    }

    // ===================== ENDPOINTS ADMIN =====================

    @GetMapping("/api/admin/alertes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AlerteResponse>> listerAlertes(
            @RequestParam(required = false) String statut) {

        List<AlerteResponse> alertes = (statut != null)
                ? alerteService.listerParStatut(statut)
                : alerteService.listerToutes();

        return ResponseEntity.ok(alertes);
    }

    @GetMapping("/api/admin/alertes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlerteResponse> obtenirAlerte(@PathVariable Long id) {
        return ResponseEntity.ok(alerteService.obtenirParId(id));
    }

    @PatchMapping("/api/admin/alertes/{id}/prendre-en-charge")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlerteResponse> prendreEnCharge(@PathVariable Long id) {
        return ResponseEntity.ok(alerteService.prendreEnCharge(id));
    }

    @PatchMapping("/api/admin/alertes/{id}/resoudre")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlerteResponse> resoudreAlerte(
            @PathVariable Long id, @Valid @RequestBody ResolutionRequest request) {
        return ResponseEntity.ok(alerteService.resoudre(id, request));
    }
}
