package sn.dci.senprix.campagne.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sn.dci.senprix.campagne.config.ServicesExternesProperties;
import sn.dci.senprix.campagne.exception.ServiceDistantIndisponibleException;

/**
 * Encapsule les appels HTTP synchrones vers l'API publique du
 * user-service, utilisés pour valider l'existence et le rôle d'un agent
 * de collecte avant son affectation à une campagne (flux d'intégration
 * inter-services de l'architecture fonctionnelle SEN-PRIX).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final RestClient restClient;
    private final ServicesExternesProperties properties;

    /**
     * Interroge l'endpoint public GET /api/public/utilisateurs/{id}/verification
     * du user-service afin d'obtenir l'existence, le rôle et le statut
     * actif d'un utilisateur, sans jamais accéder à ses données personnelles.
     */
    public UtilisateurVerificationDto verifierUtilisateur(Long agentId) {
        String url = properties.getUserServiceUrl()
                + "/api/public/utilisateurs/" + agentId + "/verification";

        try {
            UtilisateurVerificationDto resultat = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(UtilisateurVerificationDto.class);

            return (resultat != null) ? resultat : new UtilisateurVerificationDto(false, null, false);

        } catch (Exception ex) {
            log.error("Échec de vérification de l'agent {} auprès du user-service : {} - {}",
                    agentId, ex.getClass().getSimpleName(), ex.getMessage());
            throw new ServiceDistantIndisponibleException(
                    "Le user-service est momentanément indisponible pour valider l'agent " + agentId, ex);
        }
    }
}
