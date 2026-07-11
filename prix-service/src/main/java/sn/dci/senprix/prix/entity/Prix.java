package sn.dci.senprix.prix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import sn.dci.senprix.prix.enums.StatutPrix;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité JPA représentant la table prix : un relevé de prix d'un produit
 * sur un marché donné, dans le cadre d'une campagne de collecte, saisi
 * par un agent. Les identifiants produitId, marcheId, campagneId et
 * agentId référencent des entités d'autres microservices (produit-service,
 * campagne-service, user-service) — aucune clé étrangère SQL n'est
 * possible entre bases de données séparées, la validation d'existence
 * se fait par appel HTTP synchrone au moment de la création.
 */
@Entity
@Table(name = "prix")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "produit_id", nullable = false)
    private Long produitId;

    @Column(name = "marche_id", nullable = false)
    private Long marcheId;

    @Column(name = "campagne_id", nullable = false)
    private Long campagneId;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false, length = 20)
    private String unite;

    @Column(name = "date_releve", nullable = false)
    private LocalDate dateReleve;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutPrix statut = StatutPrix.SUSPECT;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
}
