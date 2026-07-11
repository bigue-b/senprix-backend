package sn.dci.senprix.produit.mapper;

import org.springframework.stereotype.Component;
import sn.dci.senprix.produit.dto.MarcheRequest;
import sn.dci.senprix.produit.dto.MarcheResponse;
import sn.dci.senprix.produit.entity.Marche;
import sn.dci.senprix.produit.enums.StatutEnum;

/**
 * Assure la conversion entre l'entité JPA Marche et les DTOs
 * exposés par l'API.
 */
@Component
public class MarcheMapper {

    public Marche toEntity(MarcheRequest request) {
        return Marche.builder()
                .nom(request.getNom())
                .ville(request.getVille())
                .region(request.getRegion())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .statut(StatutEnum.ACTIF)
                .build();
    }

    public void updateEntityFromRequest(Marche entity, MarcheRequest request) {
        entity.setNom(request.getNom());
        entity.setVille(request.getVille());
        entity.setRegion(request.getRegion());
        entity.setLatitude(request.getLatitude());
        entity.setLongitude(request.getLongitude());
    }

    public MarcheResponse toResponse(Marche entity) {
        return MarcheResponse.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .ville(entity.getVille())
                .region(entity.getRegion())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .statut(entity.getStatut().name())
                .dateCreation(entity.getDateCreation())
                .build();
    }
}
