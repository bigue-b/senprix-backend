package sn.dci.senprix.produit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO reçu en entrée lors de la création ou de la modification
 * d'un produit par l'administrateur DCI (écran 2.12).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProduitRequest {

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
    private String nom;

    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 100)
    private String categorie;

    @NotBlank(message = "L'unité de mesure est obligatoire")
    @Size(max = 50)
    private String unite;

    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    private String description;
}
