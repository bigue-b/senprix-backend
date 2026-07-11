package sn.dci.senprix.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.user.entity.AgentCollecte;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table agent_collecte.
 */
@Repository
public interface AgentCollecteRepository extends JpaRepository<AgentCollecte, Long> {

    Optional<AgentCollecte> findByMatricule(String matricule);

    List<AgentCollecte> findByZoneAffectationIgnoreCase(String zoneAffectation);

    boolean existsByMatricule(String matricule);
}
