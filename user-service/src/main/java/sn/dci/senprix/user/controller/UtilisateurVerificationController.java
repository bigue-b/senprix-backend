package sn.dci.senprix.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.dci.senprix.user.dto.UtilisateurVerificationResponse;
import sn.dci.senprix.user.service.UtilisateurService;

/**
 * Expose un unique endpoint public minimal, destiné aux appels
 * inter-services (notamment campagne-service), permettant de vérifier
 * l'existence et le rôle d'un utilisateur sans exposer aucune donnée
 * personnelle. Distinct de UtilisateurController qui reste entièrement
 * réservé au rôle ADMIN.
 */
@RestController
@RequestMapping("/api/public/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurVerificationController {

    private final UtilisateurService utilisateurService;

    @GetMapping("/{id}/verification")
    public ResponseEntity<UtilisateurVerificationResponse> verifierUtilisateur(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.verifier(id));
    }
}
