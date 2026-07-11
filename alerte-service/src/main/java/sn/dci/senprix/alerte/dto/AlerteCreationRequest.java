package sn.dci.senprix.alerte.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO reçu sur l'endpoint interne /api/internal/alertes, envoyé par
 * le prix-service au moment où il détecte un relevé de prix suspect.
 * Le niveau de gravité est calculé par le alerte-service lui-même à
 * partir de l'écart pourcentage, pas fourni directement par l'appelant.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlerteCreationRequest {

    @NotNull(message = "L'identifiant du prix est obligatoire")
    private Long prixId;

    @NotNull(message = "L'identifiant du produit est obligatoire")
    private Long produitId;

    @NotNull(message = "L'identifiant du marché est obligatoire")
    private Long marcheId;

    @NotNull(message = "Le montant est obligatoire")
    private BigDecimal montant;

    @NotNull(message = "Le montant moyen est obligatoire")
    private BigDecimal montantMoyen;
}
