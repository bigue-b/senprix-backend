package sn.dci.senprix.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Fournit la clé sur laquelle le filtre RequestRateLimiter compte les
 * requêtes : un compteur distinct est tenu dans Redis par valeur de clé.
 *
 * La limite est appliquée PAR UTILISATEUR quand la requête porte un jeton
 * (identifiant Keycloak), et par adresse IP sinon — sans quoi tout le
 * trafic anonyme partagerait un seul compteur et un seul appelant pourrait
 * bloquer les autres.
 */
@Configuration
@Slf4j
public class RateLimitConfig {

    private static final String PREFIXE_BEARER = "Bearer ";

    @Bean
    public KeyResolver utilisateurKeyResolver() {
        return exchange -> {
            ServerHttpRequest requete = exchange.getRequest();

            String sujet = extraireSujetDuJeton(
                    requete.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));

            if (sujet != null) {
                return Mono.just("utilisateur:" + sujet);
            }

            // Repli sur l'adresse IP : couvre les endpoints publics et les
            // appels sans jeton.
            String ip = requete.getRemoteAddress() != null
                    ? requete.getRemoteAddress().getAddress().getHostAddress()
                    : "inconnue";

            return Mono.just("ip:" + ip);
        };
    }

    /**
     * Extrait la revendication "sub" d'un jeton JWT, ou null si l'en-tête
     * est absent ou illisible.
     *
     * La signature n'est volontairement PAS vérifiée ici : la gateway
     * n'est pas la frontière de confiance de cette architecture, chaque
     * microservice validant lui-même le jeton auprès de Keycloak
     * (spring-boot-starter-oauth2-resource-server). On ne s'en sert que
     * pour répartir des compteurs, jamais pour accorder un accès — un
     * jeton falsifié ne donnerait donc qu'un compteur à soi, ce qui est
     * exactement l'effet d'une requête anonyme depuis une autre IP.
     */
    private String extraireSujetDuJeton(String enteteAuthorization) {
        if (enteteAuthorization == null || !enteteAuthorization.startsWith(PREFIXE_BEARER)) {
            return null;
        }

        try {
            String[] parties = enteteAuthorization.substring(PREFIXE_BEARER.length()).split("\\.");
            if (parties.length < 2) {
                return null;
            }

            String charge = new String(
                    Base64.getUrlDecoder().decode(parties[1]), StandardCharsets.UTF_8);

            // Lecture volontairement minimale, sans dépendance JSON
            // supplémentaire : on ne cherche qu'une seule revendication.
            int debut = charge.indexOf("\"sub\"");
            if (debut < 0) {
                return null;
            }
            int ouvrante = charge.indexOf('"', charge.indexOf(':', debut) + 1);
            int fermante = charge.indexOf('"', ouvrante + 1);
            if (ouvrante < 0 || fermante < 0) {
                return null;
            }

            return charge.substring(ouvrante + 1, fermante);

        } catch (Exception ex) {
            log.debug("Jeton illisible pour le calcul de la clé de limitation : {}", ex.getMessage());
            return null;
        }
    }
}
