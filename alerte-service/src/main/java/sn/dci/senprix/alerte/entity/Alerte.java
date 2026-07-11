package sn.dci.senprix.alerte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import sn.dci.senprix.alerte.enums.NiveauGravite;
import sn.dci.senprix.alerte.enums.StatutAlerte;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité JPA représentant la table alerte : une alerte créée
 * automatiquement par le prix-service lorsqu'un relevé de prix est
 * détecté comme suspect (variation anormale par rapport à la moyenne
 * du marché). prixId, produitId et marcheId référencent des entités
 * d'autres microservices — aucune clé étrangère SQL n'est possible
 * entre bases de données séparées.
 */
@Entity
@Table(name = "alerte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prix_id", nullable = false)
    private Long prixId;

    @Column(name = "produit_id", nullable = false)
    private Long produitId;

    @Column(name = "marche_id", nullable = false)
    private Long marcheId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(name = "montant_moyen", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantMoyen;

    @Column(name = "ecart_pourcentage", nullable = false, precision = 6, scale = 2)
    private BigDecimal ecartPourcentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_gravite", nullable = false, length = 20)
    private NiveauGravite niveauGravite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutAlerte statut = StatutAlerte.NOUVELLE;

    @Column(name = "commentaire_resolution", columnDefinition = "TEXT")
    private String commentaireResolution;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;
}
