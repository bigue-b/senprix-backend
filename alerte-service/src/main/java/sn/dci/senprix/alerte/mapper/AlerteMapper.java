package sn.dci.senprix.alerte.mapper;

import org.springframework.stereotype.Component;
import sn.dci.senprix.alerte.dto.AlerteResponse;
import sn.dci.senprix.alerte.entity.Alerte;

/**
 * Assure la conversion entre l'entité JPA Alerte et les DTOs exposés
 * par l'API. La conversion AlerteCreationRequest → Alerte n'est pas
 * une simple copie de champs (calcul de l'écart et du niveau de
 * gravité requis) et reste donc dans AlerteServiceImpl plutôt qu'ici.
 */
@Component
public class AlerteMapper {

    public AlerteResponse toResponse(Alerte entity) {
        return AlerteResponse.builder()
                .id(entity.getId())
                .prixId(entity.getPrixId())
                .produitId(entity.getProduitId())
                .marcheId(entity.getMarcheId())
                .montant(entity.getMontant())
                .montantMoyen(entity.getMontantMoyen())
                .ecartPourcentage(entity.getEcartPourcentage())
                .niveauGravite(entity.getNiveauGravite().name())
                .statut(entity.getStatut().name())
                .commentaireResolution(entity.getCommentaireResolution())
                .dateCreation(entity.getDateCreation())
                .dateResolution(entity.getDateResolution())
                .build();
    }
}
