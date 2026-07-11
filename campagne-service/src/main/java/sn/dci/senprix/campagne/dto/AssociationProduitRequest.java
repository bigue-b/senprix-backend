package sn.dci.senprix.campagne.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssociationProduitRequest {

    @NotNull(message = "L'identifiant du produit est obligatoire")
    private Long produitId;
}
