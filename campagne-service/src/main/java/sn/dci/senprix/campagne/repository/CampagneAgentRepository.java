package sn.dci.senprix.campagne.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.campagne.entity.CampagneAgent;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table campagne_agent.
 */
@Repository
public interface CampagneAgentRepository extends JpaRepository<CampagneAgent, Long> {

    List<CampagneAgent> findByCampagneId(Long campagneId);

    Optional<CampagneAgent> findByCampagneIdAndAgentId(Long campagneId, Long agentId);

    boolean existsByCampagneIdAndAgentId(Long campagneId, Long agentId);
}
