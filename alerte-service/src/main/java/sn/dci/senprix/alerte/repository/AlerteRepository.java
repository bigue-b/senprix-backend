package sn.dci.senprix.alerte.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.alerte.entity.Alerte;
import sn.dci.senprix.alerte.enums.NiveauGravite;
import sn.dci.senprix.alerte.enums.StatutAlerte;

import java.util.List;

/**
 * Accès aux données de la table alerte.
 */
@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    List<Alerte> findByStatut(StatutAlerte statut);

    List<Alerte> findByNiveauGravite(NiveauGravite niveauGravite);

    List<Alerte> findByProduitIdAndMarcheId(Long produitId, Long marcheId);

    boolean existsByPrixId(Long prixId);
}
