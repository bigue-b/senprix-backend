package sn.dci.senprix.prix.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import sn.dci.senprix.prix.exception.ServiceDistantIndisponibleException;
import sn.dci.senprix.prix.config.ServicesExternesProperties;

import java.util.Optional;

/**
 * Encapsule les appels HTTP synchrones vers l'API publique du
 * campagne-service, utilisés pour valider l'existence d'une campagne
 * et l'appartenance d'un agent à celle-ci avant l'enregistrement d'un
 * relevé de prix. Un seul appel permet de valider campagneId et agentId
 * ensemble, puisque agentsIds fait partie de la réponse de campagne.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CampagneServiceClient {

    private final RestClient restClient;
    private final ServicesExternesProperties properties;

    /**
     * Récupère la campagne identifiée par campagneId. Retourne un
     * Optional vide si la campagne n'existe pas (404).
     */
    public Optional<CampagneVerificationDto> obtenirCampagne(Long campagneId) {
        String url = properties.getCampagneServiceUrl() + "/api/public/campagnes/" + campagneId;

        try {
            CampagneVerificationDto campagne = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(CampagneVerificationDto.class);
            return Optional.ofNullable(campagne);

        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();

        } catch (Exception ex) {
            log.error("Échec de vérification de la campagne {} auprès du campagne-service : {} - {}",
                    campagneId, ex.getClass().getSimpleName(), ex.getMessage());
            throw new ServiceDistantIndisponibleException(
                    "Le campagne-service est momentanément indisponible pour valider la campagne "
                            + campagneId, ex);
        }
    }
}
