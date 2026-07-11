package sn.dci.senprix.notif.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.dci.senprix.notif.enums.CanalAbonnement;

/**
 * DTO reçu sur POST /api/citoyen/abonnements. L'identité du citoyen
 * (citoyenId, citoyenEmail) n'est jamais fournie par le client : elle
 * est extraite du token JWT côté serveur, pour qu'un citoyen ne
 * puisse jamais créer un abonnement au nom d'un autre.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbonnementRequest {

    @NotNull(message = "L'identifiant du produit est obligatoire")
    private Long produitId;

    @NotNull(message = "L'identifiant du marché est obligatoire")
    private Long marcheId;

    @NotNull(message = "Le canal de notification est obligatoire")
    private CanalAbonnement canal;
}
