package sn.dci.senprix.rapport.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import sn.dci.senprix.rapport.enums.TypeRapport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité JPA représentant la table rapport : une snapshot figée d'une
 * agrégation de données de prix, générée à un instant donné et
 * conservée pour consultation ultérieure sans recalcul. Enrichie avec
 * les noms réels (produit, marché) obtenus auprès du produit-service
 * au moment de la génération.
 */
@Entity
@Table(name = "rapport")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeRapport type;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(name = "produit_id")
    private Long produitId;

    @Column(name = "produit_nom", length = 200)
    private String produitNom;

    @Column(name = "marche_id")
    private Long marcheId;

    @Column(name = "marche_nom", length = 200)
    private String marcheNom;

    @Column(name = "campagne_id")
    private Long campagneId;

    @Column(name = "periode_debut", nullable = false)
    private LocalDate periodeDebut;

    @Column(name = "periode_fin", nullable = false)
    private LocalDate periodeFin;

    @Column(name = "prix_moyen", precision = 12, scale = 2)
    private BigDecimal prixMoyen;

    @Column(name = "prix_min", precision = 12, scale = 2)
    private BigDecimal prixMin;

    @Column(name = "prix_max", precision = 12, scale = 2)
    private BigDecimal prixMax;

    @Column(name = "nombre_releves")
    private Long nombreReleves;

    /**
     * Détail structuré du rapport (ex: série de points pour un graphique
     * d'évolution), stocké en JSON brut. Le contenu exact dépend du
     * type de rapport — la structure n'est pas modélisée en colonnes
     * séparées pour rester flexible entre les différents TypeRapport.
     */
    @Column(columnDefinition = "TEXT")
    private String contenuDetailleJson;

    @CreationTimestamp
    @Column(name = "date_generation", nullable = false, updatable = false)
    private LocalDateTime dateGeneration;
}
