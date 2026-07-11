package sn.dci.senprix.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.user.entity.KeycloakUser;

import java.util.Optional;

/**
 * Accès aux données de la table keycloak_user, assurant la traçabilité
 * locale de la synchronisation entre SEN-PRIX et le Realm Keycloak.
 */
@Repository
public interface KeycloakUserRepository extends JpaRepository<KeycloakUser, Long> {

    Optional<KeycloakUser> findByKeycloakId(String keycloakId);

    Optional<KeycloakUser> findByUtilisateurId(Long utilisateurId);
}
