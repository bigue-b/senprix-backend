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
 * Entité JPA représentant la table campagne_agent : table de liaison
 * many-to-many entre une campagne et les agents de collecte qui y sont
 * affectés. L'identifiant de l'agent n'est qu'une référence (Long) vers
 * le user-service — aucune clé étrangère SQL n'est possible puisque
 * chaque microservice possède sa propre base de données.
 */
@Entity
@Table(name = "campagne_agent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampagneAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campagne_id", nullable = false)
    private Campagne campagne;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @CreationTimestamp
    @Column(name = "date_affectation", nullable = false, updatable = false)
    private LocalDateTime dateAffectation;
}
