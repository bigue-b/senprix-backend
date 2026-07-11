package sn.dci.senprix.rapport.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO reçu en entrée pour générer un rapport d'évolution de prix
 * d'un produit sur un marché donné, pendant une période donnée.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerationRapportRequest {

    @NotNull(message = "L'identifiant du produit est obligatoire")
    private Long produitId;

    @NotNull(message = "L'identifiant du marché est obligatoire")
    private Long marcheId;

    @NotNull(message = "La date de début de période est obligatoire")
    private LocalDate periodeDebut;

    @NotNull(message = "La date de fin de période est obligatoire")
    private LocalDate periodeFin;
}
