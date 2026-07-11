package sn.dci.senprix.user.service;

import sn.dci.senprix.user.dto.UtilisateurCreationResponse;
import sn.dci.senprix.user.dto.UtilisateurRequest;
import sn.dci.senprix.user.dto.UtilisateurResponse;
import sn.dci.senprix.user.dto.UtilisateurVerificationResponse;

import java.util.List;

/**
 * Contrat du service métier de gestion des comptes utilisateurs SEN-PRIX
 * (agents de collecte et administrateurs DCI), incluant la synchronisation
 * avec Keycloak.
 */
public interface UtilisateurService {

    /**
     * Crée un nouvel utilisateur : compte Keycloak avec mot de passe
     * temporaire, puis enregistrement local synchronisé (utilisateur,
     * agent_collecte si applicable, keycloak_user).
     */
    UtilisateurCreationResponse creer(UtilisateurRequest request);

    UtilisateurResponse modifier(Long id, UtilisateurRequest request);

    UtilisateurResponse obtenirParId(Long id);

    List<UtilisateurResponse> listerTous();

    List<UtilisateurResponse> listerActifs();

    void desactiver(Long id);

    void activer(Long id);

    /**
     * Vérifie l'existence, le rôle et le statut d'un utilisateur sans
     * exposer ses données personnelles. Destiné aux appels inter-services
     * (ex: campagne-service validant un agent avant affectation).
     */
    UtilisateurVerificationResponse verifier(Long id);
}
