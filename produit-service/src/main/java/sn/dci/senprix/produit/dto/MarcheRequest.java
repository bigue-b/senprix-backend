package sn.dci.senprix.produit.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO reçu en entrée lors de la création ou de la modification
 * d'un marché par l'administrateur DCI (écran 2.14).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarcheRequest {

    @NotBlank(message = "Le nom du marché est obligatoire")
    @Size(max = 200)
    private String nom;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100)
    private String ville;

    @NotBlank(message = "La région est obligatoire")
    @Size(max = 100)
    private String region;

    @DecimalMin(value = "-90.0", message = "Latitude invalide")
    @DecimalMax(value = "90.0", message = "Latitude invalide")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude invalide")
    @DecimalMax(value = "180.0", message = "Longitude invalide")
    private BigDecimal longitude;
}
