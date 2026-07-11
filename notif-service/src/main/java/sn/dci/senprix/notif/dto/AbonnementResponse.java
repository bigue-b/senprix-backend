package sn.dci.senprix.notif.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.dci.senprix.notif.enums.CanalAbonnement;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbonnementResponse {
    private Long id;
    private Long produitId;
    private Long marcheId;
    private CanalAbonnement canal;
    private LocalDateTime dateCreation;
}
