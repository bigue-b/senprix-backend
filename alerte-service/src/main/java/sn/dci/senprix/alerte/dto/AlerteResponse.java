package sn.dci.senprix.alerte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO retourné par l'API pour représenter une alerte.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlerteResponse {

    private Long id;
    private Long prixId;
    private Long produitId;
    private Long marcheId;
    private BigDecimal montant;
    private BigDecimal montantMoyen;
    private BigDecimal ecartPourcentage;
    private String niveauGravite;
    private String statut;
    private String commentaireResolution;
    private LocalDateTime dateCreation;
    private LocalDateTime dateResolution;
}
