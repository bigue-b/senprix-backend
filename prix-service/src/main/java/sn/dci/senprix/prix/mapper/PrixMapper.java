package sn.dci.senprix.prix.mapper;

import org.springframework.stereotype.Component;
import sn.dci.senprix.prix.dto.PrixRequest;
import sn.dci.senprix.prix.dto.PrixResponse;
import sn.dci.senprix.prix.entity.Prix;

/**
 * Assure la conversion entre l'entité JPA Prix et les DTOs exposés
 * par l'API.
 */
@Component
public class PrixMapper {

    public Prix toEntity(PrixRequest request) {
        return Prix.builder()
                .produitId(request.getProduitId())
                .marcheId(request.getMarcheId())
                .campagneId(request.getCampagneId())
                .agentId(request.getAgentId())
                .montant(request.getMontant())
                .unite(request.getUnite())
                .dateReleve(request.getDateReleve())
                .commentaire(request.getCommentaire())
                .statut(sn.dci.senprix.prix.enums.StatutPrix.VALIDE)
                .build();
    }

    public PrixResponse toResponse(Prix entity) {
        return PrixResponse.builder()
                .id(entity.getId())
                .produitId(entity.getProduitId())
                .marcheId(entity.getMarcheId())
                .campagneId(entity.getCampagneId())
                .agentId(entity.getAgentId())
                .montant(entity.getMontant())
                .unite(entity.getUnite())
                .dateReleve(entity.getDateReleve())
                .statut(entity.getStatut().name())
                .commentaire(entity.getCommentaire())
                .dateCreation(entity.getDateCreation())
                .build();
    }
}
