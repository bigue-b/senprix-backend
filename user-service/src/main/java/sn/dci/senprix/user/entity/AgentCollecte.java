package sn.dci.senprix.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA représentant la table agent_collecte.
 * Étend Utilisateur via une relation 1-1 où la clé primaire de
 * agent_collecte est aussi une clé étrangère vers utilisateur(id),
 * conformément au schéma de base de données défini pour SEN-PRIX
 * (stratégie d'héritage Table Per Class implémentée manuellement).
 */
@Entity
@Table(name = "agent_collecte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentCollecte {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Utilisateur utilisateur;

    @Column(nullable = false, unique = true, length = 50)
    private String matricule;

    @Column(name = "zone_affectation", length = 200)
    private String zoneAffectation;

    @Column(name = "nbre_releves", nullable = false)
    @Builder.Default
    private Integer nbreReleves = 0;
}
