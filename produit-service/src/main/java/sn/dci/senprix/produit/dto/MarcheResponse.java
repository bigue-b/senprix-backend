package sn.dci.senprix.produit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO retourné par l'API pour représenter un marché.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarcheResponse {

    private Long id;
    private String nom;
    private String ville;
    private String region;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String statut;
    private LocalDateTime dateCreation;
}
