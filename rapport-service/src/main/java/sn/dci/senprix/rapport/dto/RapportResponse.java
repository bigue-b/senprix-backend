package sn.dci.senprix.rapport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO retourné par l'API pour représenter un rapport généré.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RapportResponse {

    private Long id;
    private String type;
    private String titre;
    private Long produitId;
    private String produitNom;
    private Long marcheId;
    private String marcheNom;
    private LocalDate periodeDebut;
    private LocalDate periodeFin;
    private BigDecimal prixMoyen;
    private BigDecimal prixMin;
    private BigDecimal prixMax;
    private Long nombreReleves;
    private String contenuDetailleJson;
    private LocalDateTime dateGeneration;
}
