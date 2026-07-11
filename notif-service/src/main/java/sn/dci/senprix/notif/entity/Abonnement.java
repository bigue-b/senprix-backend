package sn.dci.senprix.notif.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import sn.dci.senprix.notif.enums.CanalAbonnement;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant l'abonnement d'un citoyen aux alertes de
 * prix concernant un produit sur un marché donné. citoyenId contient
 * l'identifiant Keycloak (claim "sub" du JWT) du citoyen abonné —
 * notif-service n'a pas besoin de connaître son profil complet,
 * seulement de quoi le notifier (citoyenEmail) et l'associer à ses
 * propres abonnements.
 */
@Entity
@Table(name = "abonnement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_abonnement_citoyen_produit_marche",
                columnNames = {"citoyen_id", "produit_id", "marche_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "citoyen_id", nullable = false, length = 100)
    private String citoyenId;

    @Column(name = "citoyen_email", nullable = false, length = 200)
    private String citoyenEmail;

    @Column(name = "produit_id", nullable = false)
    private Long produitId;

    @Column(name = "marche_id", nullable = false)
    private Long marcheId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalAbonnement canal;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
}
