package sn.dci.senprix.prix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO retourné par l'API de statistiques, agrégeant les relevés de prix
 * VALIDE pour un produit sur un marché donné : moyenne, minimum, maximum
 * et nombre de relevés pris en compte.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatistiquePrixResponse {

    private Long produitId;
    private Long marcheId;
    private BigDecimal prixMoyen;
    private BigDecimal prixMin;
    private BigDecimal prixMax;
    private long nombreReleves;
}
