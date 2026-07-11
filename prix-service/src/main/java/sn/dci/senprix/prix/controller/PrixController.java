package sn.dci.senprix.prix.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.dci.senprix.prix.dto.PrixRequest;
import sn.dci.senprix.prix.dto.PrixResponse;
import sn.dci.senprix.prix.dto.StatistiquePrixResponse;
import sn.dci.senprix.prix.enums.StatutPrix;
import sn.dci.senprix.prix.repository.AuditLogRepository;
import sn.dci.senprix.prix.client.NotifServiceClient;
import sn.dci.senprix.prix.client.ProduitServiceClient;
import sn.dci.senprix.prix.service.PrixService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrixController {

    private final PrixService prixService;
    private final AuditLogRepository auditLogRepository;
    private final NotifServiceClient notifServiceClient;
    private final ProduitServiceClient produitServiceClient;

    // ===================== ENDPOINTS PUBLICS =====================

    @GetMapping("/api/public/prix")
    public ResponseEntity<List<PrixResponse>> listerPrixPublics(
            @RequestParam(required = false) Long produitId,
            @RequestParam(required = false) Long marcheId) {

        if (produitId != null && marcheId != null) {
            return ResponseEntity.ok(prixService.listerParProduitEtMarche(produitId, marcheId));
        }
        return ResponseEntity.ok(prixService.listerTous());
    }

    @GetMapping("/api/public/prix/{id}")
    public ResponseEntity<PrixResponse> obtenirPrixPublic(@PathVariable Long id) {
        return ResponseEntity.ok(prixService.obtenirParId(id));
    }

    @GetMapping("/api/public/prix/statistiques")
    public ResponseEntity<StatistiquePrixResponse> obtenirStatistiques(
            @RequestParam Long produitId, @RequestParam Long marcheId) {
        return ResponseEntity.ok(prixService.calculerStatistiques(produitId, marcheId));
    }

    // ===================== ENDPOINT AGENT/ADMIN — SOUMISSION =====================

    @PostMapping("/api/agent/prix")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT_COLLECTE')")
    public ResponseEntity<PrixResponse> soumettrePrix(@Valid @RequestBody PrixRequest request) {
        PrixResponse cree = prixService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @GetMapping("/api/agent/prix/campagne/{campagneId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT_COLLECTE')")
    public ResponseEntity<List<PrixResponse>> listerParCampagne(@PathVariable Long campagneId) {
        return ResponseEntity.ok(prixService.listerParCampagne(campagneId));
    }

    @GetMapping("/api/agent/prix/agent/{agentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT_COLLECTE')")
    public ResponseEntity<List<PrixResponse>> listerParAgent(@PathVariable Long agentId) {
        return ResponseEntity.ok(prixService.listerParAgent(agentId));
    }

    // ===================== ENDPOINTS ADMIN — MODÉRATION =====================

    @PatchMapping("/api/admin/prix/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrixResponse> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutPrix statut,
            @RequestParam(required = false) String motif,
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String adminEmail) {

        PrixResponse reponse = prixService.changerStatut(id, statut);

        String action = statut == StatutPrix.VALIDE ? "VALIDATION" : "REJET";
        auditLogRepository.save(
            sn.dci.senprix.prix.entity.AuditLog.builder()
                .adminId(adminId != null ? adminId : 1L)
                .releveId(id)
                .action(action)
                .motif(motif)
                .details((adminEmail != null ? adminEmail : "admin") + " → " + statut.name())
                .build()
        );

        // Envoyer notification à l'agent via notif-service
        String emailAgent = "bigue@dci.sn";
        String produit = produitServiceClient.obtenirNomProduit(reponse.getProduitId());
        String marche = produitServiceClient.obtenirNomMarche(reponse.getMarcheId());

        if (statut == StatutPrix.VALIDE) {
            notifServiceClient.envoyerNotificationValidation(emailAgent, produit, marche, reponse.getMontant().toString());
        } else if (statut == StatutPrix.REJETE) {
            notifServiceClient.envoyerNotificationRejet(emailAgent, produit, marche, motif);
        }

        return ResponseEntity.ok(reponse);
    }

    @DeleteMapping("/api/admin/prix/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerPrix(@PathVariable Long id) {
        prixService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
