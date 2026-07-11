package sn.dci.senprix.campagne.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant la table campagne_marche : table de liaison
 * many-to-many entre une campagne et les marchés ciblés par celle-ci.
 * L'identifiant du marché n'est qu'une référence (Long) vers le
 * produit-service.
 */
@Entity
@Table(name = "campagne_marche")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampagneMarche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campagne_id", nullable = false)
    private Campagne campagne;

    @Column(name = "marche_id", nullable = false)
    private Long marcheId;

    @CreationTimestamp
    @Column(name = "date_association", nullable = false, updatable = false)
    private LocalDateTime dateAssociation;
}
