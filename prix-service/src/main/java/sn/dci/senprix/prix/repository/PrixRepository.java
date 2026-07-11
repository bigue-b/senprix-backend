package sn.dci.senprix.prix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.prix.entity.Prix;
import sn.dci.senprix.prix.enums.StatutPrix;

import java.util.List;

/**
 * Accès aux données de la table prix, incluant des requêtes de filtrage
 * par produit/marché/campagne et une requête d'agrégation pour les
 * statistiques (moyenne, min, max) exposées publiquement.
 */
@Repository
public interface PrixRepository extends JpaRepository<Prix, Long> {

    List<Prix> findByProduitIdAndMarcheId(Long produitId, Long marcheId);

    List<Prix> findByCampagneId(Long campagneId);

    List<Prix> findByAgentId(Long agentId);

    List<Prix> findByStatut(StatutPrix statut);

    @Query("""
            SELECT AVG(p.montant) FROM Prix p
            WHERE p.produitId = :produitId AND p.marcheId = :marcheId
            AND p.statut = 'VALIDE'
            """)
    Double calculerMoyenne(@Param("produitId") Long produitId, @Param("marcheId") Long marcheId);

    @Query("""
            SELECT MIN(p.montant) FROM Prix p
            WHERE p.produitId = :produitId AND p.marcheId = :marcheId
            AND p.statut = 'VALIDE'
            """)
    java.math.BigDecimal calculerMinimum(@Param("produitId") Long produitId, @Param("marcheId") Long marcheId);

    @Query("""
            SELECT MAX(p.montant) FROM Prix p
            WHERE p.produitId = :produitId AND p.marcheId = :marcheId
            AND p.statut = 'VALIDE'
            """)
    java.math.BigDecimal calculerMaximum(@Param("produitId") Long produitId, @Param("marcheId") Long marcheId);

    long countByProduitIdAndMarcheIdAndStatut(Long produitId, Long marcheId, StatutPrix statut);
}
