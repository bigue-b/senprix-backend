package sn.dci.senprix.campagne.mapper;

import org.springframework.stereotype.Component;
import sn.dci.senprix.campagne.dto.CampagneRequest;
import sn.dci.senprix.campagne.dto.CampagneResponse;
import sn.dci.senprix.campagne.entity.Campagne;
import sn.dci.senprix.campagne.entity.CampagneAgent;
import sn.dci.senprix.campagne.entity.CampagneMarche;
import sn.dci.senprix.campagne.entity.CampagneProduit;

import java.util.List;

@Component
public class CampagneMapper {

    public Campagne toEntity(CampagneRequest request) {
        return Campagne.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .build();
    }

    public CampagneResponse toResponse(
            Campagne entity, List<CampagneAgent> agents,
            List<CampagneMarche> marches, List<CampagneProduit> produits) {

        return CampagneResponse.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .description(entity.getDescription())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .statut(entity.getStatut().name())
                .agentsIds(agents.stream().map(CampagneAgent::getAgentId).toList())
                .marchesIds(marches.stream().map(CampagneMarche::getMarcheId).toList())
                .produitsIds(produits.stream().map(CampagneProduit::getProduitId).toList())
                .dateCreation(entity.getDateCreation())
                .build();
    }
}
