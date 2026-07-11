package sn.dci.senprix.campagne.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO reçu en entrée lors de l'association d'un marché à une campagne.
 * L'existence du marché est vérifiée auprès du produit-service avant
 * la persistance de l'association.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssociationMarcheRequest {

    @NotNull(message = "L'identifiant du marché est obligatoire")
    private Long marcheId;
}
