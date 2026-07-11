package sn.dci.senprix.notif.mapper;

import org.springframework.stereotype.Component;
import sn.dci.senprix.notif.dto.AbonnementResponse;
import sn.dci.senprix.notif.entity.Abonnement;

@Component
public class AbonnementMapper {

    public AbonnementResponse toResponse(Abonnement entity) {
        return AbonnementResponse.builder()
                .id(entity.getId())
                .produitId(entity.getProduitId())
                .marcheId(entity.getMarcheId())
                .canal(entity.getCanal())
                .dateCreation(entity.getDateCreation())
                .build();
    }
}
