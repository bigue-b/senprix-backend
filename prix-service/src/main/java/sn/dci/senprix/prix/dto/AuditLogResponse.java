package sn.dci.senprix.prix.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private Long adminId;
    private Long releveId;
    private String action;
    private String motif;
    private String details;
    private LocalDateTime timestamp;
}
