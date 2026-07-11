package sn.dci.senprix.alerte.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO reçu en entrée lors de la résolution d'une alerte par un ADMIN,
 * documentant la vérification effectuée (ex: contrôle terrain, erreur
 * de saisie corrigée, prix confirmé exact malgré l'écart).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResolutionRequest {

    @NotBlank(message = "Le commentaire de résolution est obligatoire")
    private String commentaireResolution;
}
