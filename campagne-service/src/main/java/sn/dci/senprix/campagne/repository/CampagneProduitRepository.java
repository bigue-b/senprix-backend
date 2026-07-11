package sn.dci.senprix.campagne.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.campagne.entity.CampagneProduit;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampagneProduitRepository extends JpaRepository<CampagneProduit, Long> {

    List<CampagneProduit> findByCampagneId(Long campagneId);

    Optional<CampagneProduit> findByCampagneIdAndProduitId(Long campagneId, Long produitId);

    boolean existsByCampagneIdAndProduitId(Long campagneId, Long produitId);
}
