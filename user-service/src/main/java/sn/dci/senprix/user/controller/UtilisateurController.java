package sn.dci.senprix.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.dci.senprix.user.dto.UtilisateurCreationResponse;
import sn.dci.senprix.user.dto.UtilisateurRequest;
import sn.dci.senprix.user.dto.UtilisateurResponse;
import sn.dci.senprix.user.service.UtilisateurService;

import java.util.List;

/**
 * Expose les endpoints de gestion des comptes agents et administrateurs DCI.
 * Entièrement réservé au rôle ADMIN — aucun endpoint public ici, à la
 * différence des autres microservices, car la gestion des comptes
 * utilisateurs est une opération sensible relevant exclusivement de
 * l'administration de la plateforme.
 */
@RestController
@RequestMapping("/api/admin/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @PostMapping
    public ResponseEntity<UtilisateurCreationResponse> creerUtilisateur(
            @Valid @RequestBody UtilisateurRequest request) {
        UtilisateurCreationResponse cree = utilisateurService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @GetMapping
    public ResponseEntity<List<UtilisateurResponse>> listerUtilisateurs(
            @RequestParam(required = false, defaultValue = "false") boolean actifsUniquement) {

        List<UtilisateurResponse> utilisateurs = actifsUniquement
                ? utilisateurService.listerActifs()
                : utilisateurService.listerTous();

        return ResponseEntity.ok(utilisateurs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> obtenirUtilisateur(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.obtenirParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> modifierUtilisateur(
            @PathVariable Long id, @Valid @RequestBody UtilisateurRequest request) {
        return ResponseEntity.ok(utilisateurService.modifier(id, request));
    }

    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverUtilisateur(@PathVariable Long id) {
        utilisateurService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activer")
    public ResponseEntity<Void> activerUtilisateur(@PathVariable Long id) {
        utilisateurService.activer(id);
        return ResponseEntity.noContent().build();
    }
}
