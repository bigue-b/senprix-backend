package sn.dci.senprix.user.service;

import sn.dci.senprix.user.dto.KeycloakCredentialsResponse;
import sn.dci.senprix.user.enums.RoleEnum;

/**
 * Contrat du service orchestrant la création de comptes côté Keycloak.
 * Sépare la logique d'orchestration (génération de mot de passe,
 * construction du username) des appels HTTP bruts délégués au
 * KeycloakAdminClient.
 */
public interface KeycloakAdminService {

    /**
     * Crée un compte Keycloak complet (utilisateur + rôle assigné) et
     * retourne les informations nécessaires à communiquer au nouvel agent,
     * ainsi que l'identifiant Keycloak généré.
     */
    KeycloakAccountCreationResult creerCompte(
            String email, String nom, String prenom, RoleEnum role);

    KeycloakAccountCreationResult creerCompteCitoyen(
            String email, String nom, String prenom, String motDePasse);

    /**
     * Regroupe le résultat de création d'un compte Keycloak : son
     * identifiant unique et les informations de connexion temporaires.
     */
    record KeycloakAccountCreationResult(
            String keycloakId,
            String username,
            KeycloakCredentialsResponse credentials
    ) {}
}
