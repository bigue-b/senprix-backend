package sn.dci.senprix.campagne.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "campagne_produit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampagneProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campagne_id", nullable = false)
    private Long campagneId;

    @Column(name = "produit_id", nullable = false)
    private Long produitId;

    @CreationTimestamp
    @Column(name = "date_association", nullable = false, updatable = false)
    private LocalDateTime dateAssociation;
}
