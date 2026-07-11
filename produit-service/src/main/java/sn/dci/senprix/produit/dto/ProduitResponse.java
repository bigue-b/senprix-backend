package sn.dci.senprix.produit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO retourné par l'API pour représenter un produit.
 * Ne reflète jamais directement l'entité JPA — c'est le rôle
 * du ProduitMapper d'assurer cette conversion.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduitResponse {

    private Long id;
    private String codeProduit;
    private String nom;
    private String categorie;
    private String unite;
    private String description;
    private String statut;
    private LocalDateTime dateCreation;
}
