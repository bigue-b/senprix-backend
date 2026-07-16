package sn.dci.senprix.prix.event.publisher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Message publié sur l'exchange RabbitMQ "prix.suspect.exchange" lorsqu'un
 * relevé de prix suspect est détecté. La forme de ce message correspond
 * volontairement à celle de AlerteCreationRequest côté alerte-service,
 * qui l'utilise directement comme type cible de désérialisation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrixSuspectMessage {

    private Long prixId;
    private Long produitId;
    private Long marcheId;
    private BigDecimal montant;
    private BigDecimal montantMoyen;
}
