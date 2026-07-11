package sn.dci.senprix.rapport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.rapport.entity.Rapport;
import sn.dci.senprix.rapport.enums.TypeRapport;

import java.util.List;

/**
 * Accès aux données de la table rapport.
 */
@Repository
public interface RapportRepository extends JpaRepository<Rapport, Long> {

    List<Rapport> findByType(TypeRapport type);

    List<Rapport> findByProduitIdAndMarcheId(Long produitId, Long marcheId);
}
