package sn.dci.senprix.notif.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.dci.senprix.notif.dto.NotificationRequest;
import sn.dci.senprix.notif.dto.NotificationResponse;
import sn.dci.senprix.notif.service.NotificationService;

import java.util.List;

/**
 * Expose les endpoints de gestion des notifications.
 * - /api/internal/notifications : appelé par les autres microservices
 *   (alerte-service, user-service, campagne-service) pour déclencher
 *   l'envoi d'une notification, sans authentification (endpoint
 *   technique interne)
 * - /api/admin/notifications/** : consultation de l'historique des
 *   envois, réservé à ADMIN
 */
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ===================== ENDPOINT INTERNE =====================

    @PostMapping("/api/internal/notifications")
    public ResponseEntity<NotificationResponse> envoyerNotification(
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse resultat = notificationService.envoyer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultat);
    }

    // ===================== ENDPOINTS ADMIN =====================

    @GetMapping("/api/admin/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> listerNotifications(
            @RequestParam(required = false) String statut) {

        List<NotificationResponse> notifications = (statut != null)
                ? notificationService.listerParStatut(statut)
                : notificationService.listerToutes();

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/api/admin/notifications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> obtenirNotification(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.obtenirParId(id));
    }
}
