package sn.dci.senprix.produit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.produit.entity.Produit;
import sn.dci.senprix.produit.enums.StatutEnum;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table produit.
 */
@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    Optional<Produit> findByCodeProduit(String codeProduit);

    List<Produit> findByStatut(StatutEnum statut);

    List<Produit> findByCategorieIgnoreCase(String categorie);

    boolean existsByCodeProduit(String codeProduit);

    long countByStatut(StatutEnum statut);
}
