package sn.dci.senprix.prix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO retourné par l'API pour représenter un relevé de prix.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrixResponse {

    private Long id;
    private Long produitId;
    private Long marcheId;
    private Long campagneId;
    private Long agentId;
    private BigDecimal montant;
    private String unite;
    private LocalDate dateReleve;
    private String statut;
    private String commentaire;
    private LocalDateTime dateCreation;
}
