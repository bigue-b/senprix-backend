package sn.dci.senprix.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO retourné une seule fois, immédiatement après la création d'un compte,
 * contenant le mot de passe temporaire généré. Ce mot de passe n'est jamais
 * stocké en clair ni renvoyé par un autre endpoint — il doit être communiqué
 * par l'administrateur DCI à l'agent via un canal sécurisé (en main propre,
 * SMS, etc.), puis changé obligatoirement à la première connexion.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeycloakCredentialsResponse {

    private Long utilisateurId;
    private String username;
    private String motDePasseTemporaire;
    private Boolean changementMotDePasseRequis;
}
