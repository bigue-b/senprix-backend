package sn.dci.senprix.prix.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Représente la réponse JSON renvoyée par l'endpoint public
 * GET /api/public/campagnes/{id} du campagne-service.
 * Ce DTO ne circule jamais dans l'API publique du prix-service —
 * il est utilisé uniquement par CampagneServiceClient pour désérialiser
 * la réponse du campagne-service. Seuls les champs nécessaires à la
 * validation croisée sont mappés.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CampagneVerificationDto {

    private Long id;
    private String statut;
    private List<Long> agentsIds;
    private List<Long> marchesIds;
}
