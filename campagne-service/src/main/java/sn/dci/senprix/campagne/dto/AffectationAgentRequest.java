package sn.dci.senprix.campagne.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO reçu en entrée lors de l'affectation d'un agent de collecte
 * à une campagne. L'existence et le rôle de l'agent sont vérifiés
 * auprès du user-service avant la persistance de l'affectation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffectationAgentRequest {

    @NotNull(message = "L'identifiant de l'agent est obligatoire")
    private Long agentId;
}
