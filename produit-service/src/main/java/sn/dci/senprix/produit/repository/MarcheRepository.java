package sn.dci.senprix.produit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.produit.entity.Marche;
import sn.dci.senprix.produit.enums.StatutEnum;

import java.util.List;

/**
 * Accès aux données de la table marche.
 */
@Repository
public interface MarcheRepository extends JpaRepository<Marche, Long> {

    List<Marche> findByStatut(StatutEnum statut);

    List<Marche> findByRegionIgnoreCase(String region);

    List<Marche> findByVilleIgnoreCase(String ville);

    long countByStatut(StatutEnum statut);
}
