package sn.dci.senprix.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.dci.senprix.user.client.KeycloakAdminClient;
import sn.dci.senprix.user.dto.KeycloakCredentialsResponse;
import sn.dci.senprix.user.enums.RoleEnum;
import sn.dci.senprix.user.service.KeycloakAdminService;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Implémentation du service d'orchestration de création de comptes Keycloak.
 * Génère un mot de passe temporaire fort, construit le username à partir
 * de l'email, puis délègue les appels HTTP bruts au KeycloakAdminClient.
 */
@Service
@RequiredArgsConstructor
public class KeycloakAdminServiceImpl implements KeycloakAdminService {

    private static final int LONGUEUR_MOT_DE_PASSE_OCTETS = 12;
    private static final SecureRandom GENERATEUR_ALEATOIRE = new SecureRandom();

    private final KeycloakAdminClient keycloakAdminClient;

    @Override
    public KeycloakAccountCreationResult creerCompte(
            String email, String nom, String prenom, RoleEnum role) {

        String tokenAdmin = keycloakAdminClient.obtenirTokenAdmin();
        String username = email;
        String motDePasseTemporaire = genererMotDePasseTemporaire();

        String keycloakId = keycloakAdminClient.creerUtilisateur(
                tokenAdmin, username, email, nom, prenom, motDePasseTemporaire, role);

        keycloakAdminClient.assignerRole(tokenAdmin, keycloakId, role);

        KeycloakCredentialsResponse credentials = KeycloakCredentialsResponse.builder()
                .username(username)
                .motDePasseTemporaire(motDePasseTemporaire)
                .changementMotDePasseRequis(true)
                .build();

        return new KeycloakAccountCreationResult(keycloakId, username, credentials);
    }

    @Override
    public KeycloakAccountCreationResult creerCompteCitoyen(
            String email, String nom, String prenom, String motDePasse) {

        String tokenAdmin = keycloakAdminClient.obtenirTokenAdmin();

        String keycloakId = keycloakAdminClient.creerUtilisateur(
                tokenAdmin, email, email, nom, prenom, motDePasse, RoleEnum.CONSOMMATEUR);

        keycloakAdminClient.assignerRole(tokenAdmin, keycloakId, RoleEnum.CONSOMMATEUR);

        // Mettre le mot de passe comme NON temporaire
        keycloakAdminClient.definirMotDePassePermanent(tokenAdmin, keycloakId, motDePasse);

        KeycloakCredentialsResponse credentials = KeycloakCredentialsResponse.builder()
                .username(email)
                .motDePasseTemporaire(motDePasse)
                .changementMotDePasseRequis(false)
                .build();

        return new KeycloakAccountCreationResult(keycloakId, email, credentials);
    }

    /**
     * Génère un mot de passe temporaire aléatoire cryptographiquement sûr,
     * encodé en Base64 URL-safe pour éviter les caractères ambigus à
     * communiquer oralement ou par SMS à l'agent.
     */
    private String genererMotDePasseTemporaire() {
        byte[] octetsAleatoires = new byte[LONGUEUR_MOT_DE_PASSE_OCTETS];
        GENERATEUR_ALEATOIRE.nextBytes(octetsAleatoires);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(octetsAleatoires);
    }
}
