package sn.dci.senprix.rapport.mapper;

import org.springframework.stereotype.Component;
import sn.dci.senprix.rapport.dto.RapportResponse;
import sn.dci.senprix.rapport.dto.RapportResumeResponse;
import sn.dci.senprix.rapport.entity.Rapport;

/**
 * Assure la conversion entre l'entité JPA Rapport et les DTOs exposés
 * par l'API.
 */
@Component
public class RapportMapper {

    public RapportResponse toResponse(Rapport entity) {
        return RapportResponse.builder()
                .id(entity.getId())
                .type(entity.getType().name())
                .titre(entity.getTitre())
                .produitId(entity.getProduitId())
                .produitNom(entity.getProduitNom())
                .marcheId(entity.getMarcheId())
                .marcheNom(entity.getMarcheNom())
                .periodeDebut(entity.getPeriodeDebut())
                .periodeFin(entity.getPeriodeFin())
                .prixMoyen(entity.getPrixMoyen())
                .prixMin(entity.getPrixMin())
                .prixMax(entity.getPrixMax())
                .nombreReleves(entity.getNombreReleves())
                .contenuDetailleJson(entity.getContenuDetailleJson())
                .dateGeneration(entity.getDateGeneration())
                .build();
    }

    public RapportResumeResponse toResumeResponse(Rapport entity) {
        return RapportResumeResponse.builder()
                .id(entity.getId())
                .type(entity.getType().name())
                .titre(entity.getTitre())
                .produitNom(entity.getProduitNom())
                .marcheNom(entity.getMarcheNom())
                .periodeDebut(entity.getPeriodeDebut())
                .periodeFin(entity.getPeriodeFin())
                .dateGeneration(entity.getDateGeneration())
                .build();
    }
}
