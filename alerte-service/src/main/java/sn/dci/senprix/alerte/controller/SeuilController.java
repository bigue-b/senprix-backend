package sn.dci.senprix.alerte.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.dci.senprix.alerte.config.SeuilAlertesProperties;

import java.util.Map;

/**
 * Endpoint admin pour lire et modifier les seuils d'alerte dynamiquement.
 */
@RestController
@RequiredArgsConstructor
public class SeuilController {

    private final SeuilAlertesProperties seuilProperties;

    @GetMapping("/api/admin/alertes/seuils")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Double>> obtenirSeuils() {
        return ResponseEntity.ok(Map.of(
            "seuilMoyenne", seuilProperties.getSeuilMoyenne(),
            "seuilEleve", seuilProperties.getSeuilEleve()
        ));
    }

    @PutMapping("/api/admin/alertes/seuils")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Double>> modifierSeuils(
            @RequestBody Map<String, Double> seuils) {

        if (seuils.containsKey("seuilMoyenne")) {
            seuilProperties.setSeuilMoyenne(seuils.get("seuilMoyenne"));
        }
        if (seuils.containsKey("seuilEleve")) {
            seuilProperties.setSeuilEleve(seuils.get("seuilEleve"));
        }

        return ResponseEntity.ok(Map.of(
            "seuilMoyenne", seuilProperties.getSeuilMoyenne(),
            "seuilEleve", seuilProperties.getSeuilEleve()
        ));
    }
}
