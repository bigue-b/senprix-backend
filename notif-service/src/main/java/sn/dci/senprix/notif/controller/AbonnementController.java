package sn.dci.senprix.notif.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sn.dci.senprix.notif.dto.AbonnementRequest;
import sn.dci.senprix.notif.dto.AbonnementResponse;
import sn.dci.senprix.notif.service.AbonnementService;

import java.util.List;

/**
 * Expose les endpoints de gestion des abonnements citoyens aux alertes
 * de prix (souscription, consultation, désabonnement). Réservé à tout
 * utilisateur authentifié (le rôle CONSOMMATEUR comme les autres),
 * sans restriction de rôle particulière — chacun ne gère que ses
 * propres abonnements, identifiés via le token JWT.
 */
@RestController
@RequestMapping("/api/citoyen/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final AbonnementService abonnementService;

    @PostMapping
    public ResponseEntity<AbonnementResponse> creerAbonnement(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AbonnementRequest request) {

        String citoyenId = jwt.getSubject();
        String citoyenEmail = jwt.getClaimAsString("email");

        AbonnementResponse cree = abonnementService.creer(citoyenId, citoyenEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @GetMapping
    public ResponseEntity<List<AbonnementResponse>> listerMesAbonnements(
            @AuthenticationPrincipal Jwt jwt) {

        String citoyenId = jwt.getSubject();
        return ResponseEntity.ok(abonnementService.listerParCitoyen(citoyenId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerAbonnement(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        String citoyenId = jwt.getSubject();
        abonnementService.supprimer(id, citoyenId);
        return ResponseEntity.noContent().build();
    }
}
