package sn.dci.senprix.prix.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.dci.senprix.prix.dto.AuditLogResponse;
import sn.dci.senprix.prix.entity.AuditLog;
import sn.dci.senprix.prix.repository.AuditLogRepository;

import java.util.List;

/**
 * Endpoint admin pour consulter les logs d'audit des décisions sur les relevés.
 */
@RestController
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/api/admin/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> listerAudits() {
        List<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc();
        List<AuditLogResponse> reponses = logs.stream()
                .map(l -> AuditLogResponse.builder()
                        .id(l.getId())
                        .adminId(l.getAdminId())
                        .releveId(l.getReleveId())
                        .action(l.getAction())
                        .motif(l.getMotif())
                        .details(l.getDetails())
                        .timestamp(l.getTimestamp())
                        .build())
                .toList();
        return ResponseEntity.ok(reponses);
    }

    @GetMapping("/api/admin/audit/releve/{releveId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> auditParReleve(@PathVariable Long releveId) {
        List<AuditLog> logs = auditLogRepository.findByReleveId(releveId);
        List<AuditLogResponse> reponses = logs.stream()
                .map(l -> AuditLogResponse.builder()
                        .id(l.getId())
                        .adminId(l.getAdminId())
                        .releveId(l.getReleveId())
                        .action(l.getAction())
                        .motif(l.getMotif())
                        .details(l.getDetails())
                        .timestamp(l.getTimestamp())
                        .build())
                .toList();
        return ResponseEntity.ok(reponses);
    }
}
