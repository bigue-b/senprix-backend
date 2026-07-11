package sn.dci.senprix.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.user.entity.Utilisateur;
import sn.dci.senprix.user.enums.RoleEnum;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table utilisateur.
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByKeycloakId(String keycloakId);

    List<Utilisateur> findByRole(RoleEnum role);

    List<Utilisateur> findByActif(Boolean actif);

    boolean existsByEmail(String email);
}
