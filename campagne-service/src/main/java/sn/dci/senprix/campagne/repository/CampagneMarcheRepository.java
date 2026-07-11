package sn.dci.senprix.campagne.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.campagne.entity.CampagneMarche;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table campagne_marche.
 */
@Repository
public interface CampagneMarcheRepository extends JpaRepository<CampagneMarche, Long> {

    List<CampagneMarche> findByCampagneId(Long campagneId);

    Optional<CampagneMarche> findByCampagneIdAndMarcheId(Long campagneId, Long marcheId);

    boolean existsByCampagneIdAndMarcheId(Long campagneId, Long marcheId);
}
