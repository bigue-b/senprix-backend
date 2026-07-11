package sn.dci.senprix.prix.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO reçu en entrée lors de la soumission d'un relevé de prix.
 * L'existence du produit est vérifiée auprès du produit-service, et
 * l'appartenance de l'agent à la campagne auprès du campagne-service,
 * avant la persistance.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrixRequest {

    @NotNull(message = "L'identifiant du produit est obligatoire")
    private Long produitId;

    @NotNull(message = "L'identifiant du marché est obligatoire")
    private Long marcheId;

    @NotNull(message = "L'identifiant de la campagne est obligatoire")
    private Long campagneId;

    @NotNull(message = "L'identifiant de l'agent est obligatoire")
    private Long agentId;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être strictement positif")
    private BigDecimal montant;

    @NotBlank(message = "L'unité est obligatoire")
    @Size(max = 20)
    private String unite;

    @NotNull(message = "La date de relevé est obligatoire")
    @PastOrPresent(message = "La date de relevé ne peut pas être dans le futur")
    private LocalDate dateReleve;

    private String commentaire;
}
