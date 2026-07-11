package sn.dci.senprix.user.mapper;

import org.springframework.stereotype.Component;
import sn.dci.senprix.user.dto.UtilisateurRequest;
import sn.dci.senprix.user.dto.UtilisateurResponse;
import sn.dci.senprix.user.entity.AgentCollecte;
import sn.dci.senprix.user.entity.Utilisateur;

/**
 * Assure la conversion entre les entités JPA Utilisateur/AgentCollecte
 * et les DTOs exposés par l'API. Aucune entité n'est jamais retournée
 * directement par un contrôleur — elle passe toujours par ce mapper.
 */
@Component
public class UtilisateurMapper {

    /**
     * Convertit une requête de création en nouvelle entité Utilisateur.
     * Le keycloakId est volontairement absent ici : il n'est connu
     * qu'après la création effective du compte côté Keycloak, et sera
     * affecté séparément par le service une fois cette étape terminée.
     */
    public Utilisateur toEntity(UtilisateurRequest request) {
        return Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .role(request.getRole())
                .actif(true)
                .build();
    }

    /**
     * Construit l'entité AgentCollecte associée, uniquement pertinente
     * lorsque le rôle demandé est AGENT_COLLECTE.
     */
    public AgentCollecte toAgentCollecteEntity(Utilisateur utilisateur, UtilisateurRequest request) {
        return AgentCollecte.builder()
                .utilisateur(utilisateur)
                .matricule(request.getMatricule())
                .zoneAffectation(request.getZoneAffectation())
                .nbreReleves(0)
                .build();
    }

    /**
     * Convertit une entité Utilisateur (et son AgentCollecte associé si
     * présent) en DTO de réponse standard.
     */
    public UtilisateurResponse toResponse(Utilisateur entity) {
        return toResponse(entity, null);
    }

    public UtilisateurResponse toResponse(Utilisateur entity, AgentCollecte agentCollecte) {
        UtilisateurResponse.UtilisateurResponseBuilder builder = UtilisateurResponse.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .prenom(entity.getPrenom())
                .email(entity.getEmail())
                .telephone(entity.getTelephone())
                .role(entity.getRole().name())
                .actif(entity.getActif())
                .dateCreation(entity.getDateCreation());

        if (agentCollecte != null) {
            builder.matricule(agentCollecte.getMatricule())
                    .zoneAffectation(agentCollecte.getZoneAffectation());
        }

        return builder.build();
    }
}
