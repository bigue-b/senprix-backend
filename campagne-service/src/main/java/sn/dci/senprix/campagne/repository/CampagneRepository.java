package sn.dci.senprix.campagne.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.campagne.entity.Campagne;
import sn.dci.senprix.campagne.enums.StatutCampagne;

import java.util.List;

/**
 * Accès aux données de la table campagne.
 */
@Repository
public interface CampagneRepository extends JpaRepository<Campagne, Long> {

    List<Campagne> findByStatut(StatutCampagne statut);
}
