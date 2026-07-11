package sn.dci.senprix.produit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sn.dci.senprix.produit.enums.StatutEnum;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant la table produit.
 * Référentiel officiel des produits de première nécessité
 * suivis par la Direction du Commerce Intérieur (DCI).
 */
@Entity
@Table(name = "produit", uniqueConstraints = {
        @UniqueConstraint(name = "uk_produit_code", columnNames = "code_produit")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_produit", nullable = false, length = 20)
    private String codeProduit;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(nullable = false, length = 100)
    private String categorie;

    @Column(nullable = false, length = 50)
    private String unite;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutEnum statut = StatutEnum.ACTIF;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;
}
