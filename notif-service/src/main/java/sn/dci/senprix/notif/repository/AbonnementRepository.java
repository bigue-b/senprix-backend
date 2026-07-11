package sn.dci.senprix.notif.repository;

import sn.dci.senprix.notif.entity.Abonnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    List<Abonnement> findByCitoyenId(String citoyenId);

    Optional<Abonnement> findByCitoyenIdAndProduitIdAndMarcheId(
            String citoyenId, Long produitId, Long marcheId);

    boolean existsByCitoyenIdAndProduitIdAndMarcheId(
            String citoyenId, Long produitId, Long marcheId);

    /**
     * Utilisé par alerte-service (via appel interne à prévoir) pour
     * déterminer quels citoyens notifier lorsqu'une alerte est créée
     * sur un produit/marché donné.
     */
    List<Abonnement> findByProduitIdAndMarcheId(Long produitId, Long marcheId);
}
