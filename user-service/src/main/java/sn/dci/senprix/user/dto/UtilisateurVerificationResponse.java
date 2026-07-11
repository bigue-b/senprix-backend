package sn.dci.senprix.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO minimal exposé publiquement pour permettre aux autres microservices
 * (notamment campagne-service) de vérifier l'existence et le rôle d'un
 * utilisateur, sans exposer aucune donnée personnelle (nom, email,
 * téléphone). Utilisé pour la validation croisée lors de l'affectation
 * d'un agent à une campagne.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurVerificationResponse {

    private boolean existe;
    private String role;
    private boolean actif;
}
