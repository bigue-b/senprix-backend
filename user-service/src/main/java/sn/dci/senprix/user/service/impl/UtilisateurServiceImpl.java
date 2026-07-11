package sn.dci.senprix.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dci.senprix.user.dto.UtilisateurCreationResponse;
import sn.dci.senprix.user.dto.UtilisateurRequest;
import sn.dci.senprix.user.dto.UtilisateurResponse;
import sn.dci.senprix.user.dto.UtilisateurVerificationResponse;
import sn.dci.senprix.user.entity.AgentCollecte;
import sn.dci.senprix.user.entity.KeycloakUser;
import sn.dci.senprix.user.entity.Utilisateur;
import sn.dci.senprix.user.enums.RoleEnum;
import sn.dci.senprix.user.exception.EmailDejaUtiliseException;
import sn.dci.senprix.user.exception.UtilisateurNotFoundException;
import sn.dci.senprix.user.mapper.UtilisateurMapper;
import sn.dci.senprix.user.repository.AgentCollecteRepository;
import sn.dci.senprix.user.repository.KeycloakUserRepository;
import sn.dci.senprix.user.repository.UtilisateurRepository;
import sn.dci.senprix.user.service.KeycloakAdminService;
import sn.dci.senprix.user.service.UtilisateurService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service métier de gestion des comptes utilisateurs.
 * Orchestre la création de comptes en deux temps : d'abord Keycloak
 * (source de vérité pour l'authentification), puis la persistance locale
 * synchronisée (utilisateur, agent_collecte).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final AgentCollecteRepository agentCollecteRepository;
    private final KeycloakUserRepository keycloakUserRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final KeycloakAdminService keycloakAdminService;

    @Override
    public UtilisateurCreationResponse creer(UtilisateurRequest request) {
        validerUnicite(request.getEmail());
        validerCoherenceRoleAgent(request);

        // 1. Création du compte côté Keycloak (source de vérité pour l'authentification)
        KeycloakAdminService.KeycloakAccountCreationResult resultatKeycloak =
                keycloakAdminService.creerCompte(
                        request.getEmail(), request.getNom(), request.getPrenom(), request.getRole());

        // 2. Persistance locale synchronisée, uniquement si Keycloak a réussi
        Utilisateur utilisateur = utilisateurMapper.toEntity(request);
        utilisateur.setKeycloakId(resultatKeycloak.keycloakId());
        utilisateur = utilisateurRepository.save(utilisateur);

        AgentCollecte agentCollecte = null;
        if (request.getRole() == RoleEnum.AGENT_COLLECTE) {
            agentCollecte = utilisateurMapper.toAgentCollecteEntity(utilisateur, request);
            agentCollecte = agentCollecteRepository.save(agentCollecte);
        }

        enregistrerSynchronisationKeycloak(utilisateur, resultatKeycloak);

        UtilisateurResponse utilisateurResponse = utilisateurMapper.toResponse(utilisateur, agentCollecte);

        return UtilisateurCreationResponse.builder()
                .utilisateur(utilisateurResponse)
                .credentials(resultatKeycloak.credentials())
                .build();
    }

    @Override
    public UtilisateurResponse modifier(Long id, UtilisateurRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));

        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur = utilisateurRepository.save(utilisateur);

        AgentCollecte agentCollecte = null;
        if (utilisateur.getRole() == RoleEnum.AGENT_COLLECTE) {
            agentCollecte = agentCollecteRepository.findById(id).orElse(null);
            if (agentCollecte != null) {
                agentCollecte.setZoneAffectation(request.getZoneAffectation());
                agentCollecte = agentCollecteRepository.save(agentCollecte);
            }
        }

        return utilisateurMapper.toResponse(utilisateur, agentCollecte);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponse obtenirParId(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));

        AgentCollecte agentCollecte = agentCollecteRepository.findById(id).orElse(null);
        return utilisateurMapper.toResponse(utilisateur, agentCollecte);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> listerTous() {
        return utilisateurRepository.findAll()
                .stream()
                .map(this::versResponseAvecAgentEventuel)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> listerActifs() {
        return utilisateurRepository.findByActif(true)
                .stream()
                .map(this::versResponseAvecAgentEventuel)
                .toList();
    }

    @Override
    public void desactiver(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));
        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);
    }

    @Override
    public void activer(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));
        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurVerificationResponse verifier(Long id) {
        return utilisateurRepository.findById(id)
                .map(utilisateur -> UtilisateurVerificationResponse.builder()
                        .existe(true)
                        .role(utilisateur.getRole().name())
                        .actif(utilisateur.getActif())
                        .build())
                .orElse(UtilisateurVerificationResponse.builder()
                        .existe(false)
                        .role(null)
                        .actif(false)
                        .build());
    }

    private UtilisateurResponse versResponseAvecAgentEventuel(Utilisateur utilisateur) {
        AgentCollecte agentCollecte = (utilisateur.getRole() == RoleEnum.AGENT_COLLECTE)
                ? agentCollecteRepository.findById(utilisateur.getId()).orElse(null)
                : null;
        return utilisateurMapper.toResponse(utilisateur, agentCollecte);
    }

    private void validerUnicite(String email) {
        if (utilisateurRepository.existsByEmail(email)) {
            throw new EmailDejaUtiliseException(email);
        }
    }

    /**
     * Le matricule est obligatoire uniquement lorsque le rôle demandé est
     * AGENT_COLLECTE — cette validation croisée entre deux champs ne peut
     * pas être exprimée par une simple annotation Bean Validation sur le DTO.
     */
    private void validerCoherenceRoleAgent(UtilisateurRequest request) {
        if (request.getRole() == RoleEnum.AGENT_COLLECTE
                && (request.getMatricule() == null || request.getMatricule().isBlank())) {
            throw new IllegalArgumentException(
                    "Le matricule est obligatoire pour la création d'un agent de collecte");
        }
    }

    /**
     * Enregistre la trace locale de la synchronisation effectuée avec
     * Keycloak, utile pour l'audit et le diagnostic en cas de désynchronisation
     * future entre SEN-PRIX et le Realm Keycloak.
     */
    private void enregistrerSynchronisationKeycloak(
            Utilisateur utilisateur, KeycloakAdminService.KeycloakAccountCreationResult resultat) {

        KeycloakUser keycloakUser = KeycloakUser.builder()
                .utilisateur(utilisateur)
                .keycloakId(resultat.keycloakId())
                .username(resultat.username())
                .email(utilisateur.getEmail())
                .enabled(true)
                .dateSynchronisation(LocalDateTime.now())
                .build();

        keycloakUserRepository.save(keycloakUser);
    }
}
