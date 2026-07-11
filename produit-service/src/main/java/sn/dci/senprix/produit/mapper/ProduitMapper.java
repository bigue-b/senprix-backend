package sn.dci.senprix.produit.mapper;

import org.springframework.stereotype.Component;
import sn.dci.senprix.produit.dto.ProduitRequest;
import sn.dci.senprix.produit.dto.ProduitResponse;
import sn.dci.senprix.produit.entity.Produit;
import sn.dci.senprix.produit.enums.StatutEnum;

/**
 * Assure la conversion entre l'entité JPA Produit et les DTOs
 * exposés par l'API. Aucune entité n'est jamais retournée
 * directement par un contrôleur — elle passe toujours par ce mapper.
 */
@Component
public class ProduitMapper {

    /**
     * Convertit une requête de création en nouvelle entité.
     * Le code produit est généré séparément par le service
     * (séquence métier PRD-XXX), pas par le mapper.
     */
    public Produit toEntity(ProduitRequest request) {
        return Produit.builder()
                .nom(request.getNom())
                .categorie(request.getCategorie())
                .unite(request.getUnite())
                .description(request.getDescription())
                .statut(StatutEnum.ACTIF)
                .build();
    }

    /**
     * Met à jour une entité existante à partir d'une requête de modification,
     * sans toucher aux champs non présents dans le DTO (code, statut, dates).
     */
    public void updateEntityFromRequest(Produit entity, ProduitRequest request) {
        entity.setNom(request.getNom());
        entity.setCategorie(request.getCategorie());
        entity.setUnite(request.getUnite());
        entity.setDescription(request.getDescription());
    }

    public ProduitResponse toResponse(Produit entity) {
        return ProduitResponse.builder()
                .id(entity.getId())
                .codeProduit(entity.getCodeProduit())
                .nom(entity.getNom())
                .categorie(entity.getCategorie())
                .unite(entity.getUnite())
                .description(entity.getDescription())
                .statut(entity.getStatut().name())
                .dateCreation(entity.getDateCreation())
                .build();
    }
}
